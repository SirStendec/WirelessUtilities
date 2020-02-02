package com.lordmau5.wirelessutils.entity.base;

import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.block.slime.BlockAngledSlime;
import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.item.base.IEnhancedItem;
import com.lordmau5.wirelessutils.utils.location.RayTracing;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public abstract class EntityBaseThrowable extends EntityThrowable {

    public enum HitReactionType {
        NONE,
        BOUNCE,
        IGNORE,
        SPEED,
        GLOW,
        SLIDE;
    }

    public static final Map<Block, HitReaction> REACTIONS = new Object2ObjectOpenHashMap<>();

    public static void initReactions() {
        REACTIONS.put(Blocks.TRIPWIRE, HitReaction.IGNORE);
        REACTIONS.put(Blocks.WEB, HitReaction.SLOW);
        REACTIONS.put(Blocks.SLIME_BLOCK, HitReaction.BOUNCE);
        REACTIONS.put(Blocks.ICE, HitReaction.SLIDE);
        REACTIONS.put(Blocks.PACKED_ICE, HitReaction.SLIDE);
        REACTIONS.put(Blocks.GLOWSTONE, HitReaction.GLOW);
    }

    public static void addReaction(Block block, HitReactionType type) {
        addReaction(block, new HitReaction(type));
    }

    public static void addReaction(Block block, HitReactionType type, double scale) {
        addReaction(block, new HitReaction(type, scale));
    }

    public static void addReaction(Block block, HitReaction reaction) {
        REACTIONS.put(block, reaction);
    }

    public static void clearReactions() {
        REACTIONS.clear();
    }

    public static void removeReaction(Block block) {
        REACTIONS.remove(block);
    }

    public static class HitReaction {
        public final HitReactionType type;
        public final double scale;

        public HitReaction(HitReactionType type) {
            this.type = type;
            scale = 1;
        }

        public HitReaction(HitReactionType type, double scale) {
            this.type = type;
            this.scale = scale;
        }

        public static final HitReaction IGNORE = new HitReaction(HitReactionType.IGNORE);
        public static final HitReaction SLOW = new HitReaction(HitReactionType.SPEED, 0.8);
        public static final HitReaction FAST = new HitReaction(HitReactionType.SPEED, 1.2);
        public static final HitReaction NONE = new HitReaction(HitReactionType.NONE);
        public static final HitReaction BOUNCE = new HitReaction(HitReactionType.BOUNCE, 0.8);
        public static final HitReaction SLIDE = new HitReaction(HitReactionType.SLIDE, 0);
        public static final HitReaction GLOW = new HitReaction(HitReactionType.GLOW, 200);
        //public static final HitReaction SLIDE_FAST = new HitReaction(HitReactionType.SLIDE, 0.92);
    }


    /* Class */

    private static final DataParameter<ItemStack> STACK = EntityDataManager.createKey(EntityBaseThrowable.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Boolean> DRAGLESS = EntityDataManager.createKey(EntityBaseThrowable.class, DataSerializers.BOOLEAN);

    private BlockPos speedModPos;
    private Predicate<? super Entity> entityPredicate;
    private Predicate<? super Entity> oldPredicate;

    private EnumFacing slideFace;
    private BlockPos slidePos;
    private float slideFactor;

    private boolean triggeredRoundabout = false;
    private boolean triggeredDistance = false;
    private boolean triggeredBounce = false;
    private short bounces = 0;
    private double distance = 0;

    private short glowTicks = 0;

    private boolean started = false;
    private double startX;
    private double startY;
    private double startZ;

    private byte skippedTrailFrames = 0;

    public EntityBaseThrowable(World world) {
        super(world);
    }

    public EntityBaseThrowable(World world, ItemStack stack) {
        super(world);
        setStack(stack);
    }

    public EntityBaseThrowable(World world, EntityLivingBase thrower) {
        super(world, thrower);
        this.thrower = thrower;
    }

    public EntityBaseThrowable(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower);
        setStack(stack);
    }

    public EntityBaseThrowable(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityBaseThrowable(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z);
        setStack(stack);
    }

    public void shootAngle(float rotationPitch, float rotationYaw, float pitchOffset, float velocity, float inaccuracy) {
        final float x = -MathHelper.sin(rotationYaw * 0.017453292F) * MathHelper.cos(rotationPitch * 0.017453292F);
        final float y = -MathHelper.sin((rotationPitch + pitchOffset) * 0.017453292F);
        final float z = MathHelper.cos(rotationYaw * 0.017453292F) * MathHelper.cos(rotationPitch * 0.017453292F);
        this.shoot((double) x, (double) y, (double) z, velocity, inaccuracy);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        getDataManager().register(STACK, ItemStack.EMPTY);
        getDataManager().register(DRAGLESS, false);
    }

    public boolean isDragless() {
        return getDataManager().get(DRAGLESS);
    }

    public void setDragless(boolean dragless) {
        if ( dragless == isDragless() )
            return;

        getDataManager().set(DRAGLESS, dragless);
        getDataManager().setDirty(DRAGLESS);
    }

    @Nonnull
    public ItemStack getStack() {
        return getDataManager().get(STACK);
    }

    @Nonnull
    public ItemStack getRenderStack() {
        return getStack();
    }

    public void setStack(@Nonnull ItemStack stack) {
        getDataManager().set(STACK, stack);
        getDataManager().setDirty(STACK);
    }

    public void dropThis() {
        dropItemStack(getStack());
    }

    public void dropItemStack(@Nonnull ItemStack stack) {
        dropItemStack(stack, false);
    }

    public void dropItemStack(@Nonnull ItemStack stack, boolean suppressVelocity) {
        EntityItem entity;
        Item item = stack.getItem();
        if ( item instanceof IEnhancedItem )
            entity = new EntityItemEnhanced(world, posX, posY, posZ, stack);
        else
            entity = new EntityItem(world, posX, posY, posZ, stack);

        if ( suppressVelocity )
            entity.motionX = entity.motionY = entity.motionZ = 0;

        EntityLivingBase thrower = getThrower();
        if ( thrower instanceof EntityPlayer )
            entity.setThrower(thrower.getName());

        entity.setDefaultPickupDelay();
        world.spawnEntity(entity);
    }

    public void dropItemWithMeta(Item item, int count) {
        ItemStack stack = getStack();
        dropItemStack(new ItemStack(item, count, stack.isEmpty() ? 0 : stack.getMetadata()));
    }

    public HitReaction hitBlock(IBlockState state, RayTraceResult result) {
        Block block = state.getBlock();
        HitReaction reaction = REACTIONS.get(block);
        if ( reaction == null ) {
            Material material = state.getMaterial();
            if ( material == Material.AIR || material == Material.WATER )
                return HitReaction.IGNORE;

            return HitReaction.NONE;
        }

        if ( !world.isRemote && reaction.type == HitReactionType.BOUNCE ) {
            SoundType soundType = block.getSoundType(state, world, result.getBlockPos(), this);
            SoundEvent fallSound = soundType == null ? null : soundType.getFallSound();
            if ( fallSound != null ) {
                float volume = Math.min(0.5F, soundType.getVolume());
                playSound(fallSound, volume, soundType.getPitch());
            }
        }

        return reaction;
    }

    public boolean shouldHitLiquids() {
        return false;
    }

    public boolean shouldHitEntities() {
        return false;
    }

    @Nullable
    public Predicate<? super Entity> getEntityPredicate() {
        return null;
    }

    @Nullable
    protected RayTraceResult findEntityOnPath(@Nonnull Vec3d start, @Nonnull Vec3d end) {
        Predicate<? super Entity> predicate = getEntityPredicate();
        if ( predicate == null ) {
            entityPredicate = oldPredicate = null;
            return null;
        }

        if ( entityPredicate == null || predicate != oldPredicate ) {
            oldPredicate = predicate;
            entityPredicate = entity -> {
                if ( entity == this )
                    return false;

                if ( !predicate.apply(entity) )
                    return false;

                if ( thrower != null && ticksExisted < 2 && ignoreEntity == null )
                    ignoreEntity = entity;

                return entity != ignoreEntity || ticksExisted >= 5;
            };
        }

        return RayTracing.findEntityOnPath(world, start, end, entityPredicate);
    }

    public void renderTrail() {

    }

    @Override
    public void onUpdate() {
        if ( !started ) {
            started = true;
            startX = posX;
            startY = posY;
            startZ = posZ;
        }

        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;

        if ( glowTicks > 0 ) {
            glowTicks--;
            if ( glowTicks == 0 )
                setGlowing(false);
        }

        // From Entity, to avoid super.onUpdate()
        if ( !world.isRemote )
            setFlag(6, isGlowing());

        onEntityUpdate();

        // Back in action
        if ( throwableShake > 0 )
            --throwableShake;

        double remainingX = motionX;
        double remainingY = motionY;
        double remainingZ = motionZ;

        onGround = false;

        int steps = 0;

        boolean isSlowed = false;

        while ( steps < 10 && !isDead ) {
            Vec3d currentPos = new Vec3d(posX, posY, posZ);
            Vec3d targetPos = new Vec3d(posX + remainingX, posY + remainingY, posZ + remainingZ);

            if ( remainingY > 0 )
                onGround = false;

            RayTraceResult ray = world.rayTraceBlocks(currentPos, targetPos, shouldHitLiquids(), false, false);

            if ( shouldHitEntities() ) {
                RayTraceResult entityHit = findEntityOnPath(currentPos, ray == null ? targetPos : ray.hitVec);
                if ( entityHit != null ) {
                    distance += new Vec3d(entityHit.hitVec.x - posX, 0, entityHit.hitVec.z - posZ).length();

                    remainingX -= entityHit.hitVec.x - posX;
                    remainingY -= entityHit.hitVec.y - posY;
                    remainingZ -= entityHit.hitVec.z - posZ;

                    posX = entityHit.hitVec.x;
                    posY = entityHit.hitVec.y;
                    posZ = entityHit.hitVec.z;

                    onImpact(entityHit);

                    if ( remainingX != 0 || remainingY != 0 || remainingZ != 0 ) {
                        ++steps;
                        continue;
                    }
                }
            }

            if ( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK ) {
                IBlockState blockState = world.getBlockState(ray.getBlockPos());
                if ( remainingY <= 0 )
                    onGround = ray.sideHit == EnumFacing.UP;

                Block block = blockState.getBlock();
                if ( block == Blocks.PORTAL ) {
                    setPortal(ray.getBlockPos());

                } else if ( block == ModBlocks.blockAngledSlime ) {
                    blockState = ModBlocks.blockAngledSlime.getActualState(blockState, world, ray.getBlockPos());
                    Vec3d position = getPositionVector();
                    AxisAlignedBB boundingBox = getEntityBoundingBox();
                    BlockAngledSlime.AngledBounceResult result = ModBlocks.blockAngledSlime.tryBounce(position, boundingBox, new Vec3d(remainingX, remainingY, remainingZ), ray, blockState);
                    if ( result != null ) {
                        // Do the real bounce, with our full velocity.
                        BlockAngledSlime.AngledBounceResult realResult = ModBlocks.blockAngledSlime.tryBounce(position, boundingBox, new Vec3d(motionX, motionY, motionZ), ray, blockState);
                        if ( realResult != null ) {
                            posX = result.hitPos.x;
                            posY = result.hitPos.y;
                            posZ = result.hitPos.z;

                            remainingX = result.velocity.x;
                            remainingY = result.velocity.y;
                            remainingZ = result.velocity.z;

                            motionX = realResult.velocity.x;
                            motionY = realResult.velocity.y;
                            motionZ = realResult.velocity.z;

                            if ( !world.isRemote ) {
                                SoundType soundType = block.getSoundType(blockState, world, ray.getBlockPos(), this);
                                SoundEvent fallSound = soundType == null ? null : soundType.getFallSound();
                                if ( fallSound != null )
                                    playSound(fallSound, 0.5F, soundType.getPitch());
                            }

                            bounces++;
                            if ( !world.isRemote && !triggeredBounce && bounces >= 10 ) {
                                triggeredBounce = true;
                                EntityLivingBase thrower = getThrower();
                                if ( thrower instanceof EntityPlayerMP )
                                    ModAdvancements.REPULSION.trigger((EntityPlayerMP) thrower);
                            }

                            if ( remainingX != 0 || remainingY != 0 || remainingZ != 0 ) {
                                ++steps;
                                continue;
                            }
                        }
                    } else if ( !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, ray) ) {
                        posX = result.hitPos.x;
                        posY = result.hitPos.y;
                        posZ = result.hitPos.z;

                        AxisAlignedBB box = getEntityBoundingBox();
                        EnumFacing.Axis axis = ray.sideHit.getAxis();
                        int direction = ray.sideHit.getAxisDirection().getOffset();

                        if ( axis == EnumFacing.Axis.X )
                            posX += direction * (box.maxX - box.minX);
                        else if ( axis == EnumFacing.Axis.Y )
                            posY += direction * (box.maxY - box.minY);
                        else
                            posZ += direction * (box.maxZ - box.minZ);

                        onImpact(ray);
                    }

                } else {
                    HitReaction reaction = hitBlock(blockState, ray);
                    if ( reaction == null )
                        reaction = HitReaction.NONE;

                    isSlowed = reaction.type == HitReactionType.SPEED;
                    if ( isSlowed ) {
                        if ( speedModPos == null || !speedModPos.equals(ray.getBlockPos()) ) {
                            speedModPos = ray.getBlockPos();
                            remainingX *= reaction.scale;
                            motionX *= reaction.scale;
                            remainingY *= reaction.scale;
                            motionY *= reaction.scale;
                            remainingZ *= reaction.scale;
                            motionZ *= reaction.scale;
                        }
                    } else {
                        speedModPos = null;
                    }

                    if ( reaction.type == HitReactionType.BOUNCE ) {
                        // Enforce a minimum velocity for bouncing.
                        double velocity = Math.abs(motionX) + Math.abs(motionY) + Math.abs(motionZ);
                        if ( velocity < 0.2D && motionY <= 0 )
                            reaction = HitReaction.NONE;
                    } else if ( reaction.type == HitReactionType.SLIDE ) {
                        // Slightly different minimum velocity for sliding.
                        EnumFacing.Axis axis = ray.sideHit.getAxis();
                        double velocity;
                        switch (axis) {
                            case X:
                                velocity = Math.abs(motionY) + Math.abs(motionZ);
                                break;
                            case Y:
                                velocity = Math.abs(motionX) + Math.abs(motionZ);
                                break;
                            default:
                                velocity = Math.abs(motionX) + Math.abs(motionY);
                        }

                        if ( velocity < 0.1D )
                            reaction = HitReaction.NONE;
                    }

                    // If we aren't sliding, clear sliding state.
                    if ( reaction.type != HitReactionType.SLIDE ) {
                        slidePos = null;
                        slideFace = null;
                        slideFactor = 1;
                    }

                    if ( reaction.type == HitReactionType.SLIDE ) {
                        slideFactor = (float) reaction.scale;
                        if ( slideFactor == 0 )
                            slideFactor = blockState.getBlock().getSlipperiness(blockState, world, ray.getBlockPos(), this);

                        double offsetX = 0;
                        double offsetY = 0;
                        double offsetZ = 0;

                        double travelX = ray.hitVec.x - posX;
                        double travelY = ray.hitVec.y - posY;
                        double travelZ = ray.hitVec.z - posZ;

                        distance += new Vec3d(travelX, travelY, travelZ).length();

                        remainingX -= travelX;
                        remainingY -= travelY;
                        remainingZ -= travelZ;

                        AxisAlignedBB box = getEntityBoundingBox();
                        EnumFacing.Axis axis = ray.sideHit.getAxis();
                        int direction = ray.sideHit.getAxisDirection().getOffset();

                        if ( axis == EnumFacing.Axis.X ) {
                            offsetX = direction * (box.maxX - box.minX);
                            remainingX = motionY = 0;
                        } else if ( axis == EnumFacing.Axis.Y ) {
                            offsetY = direction * (box.maxY - box.minY);
                            remainingY = motionY = 0;
                            if ( ray.sideHit == EnumFacing.UP )
                                onGround = true;

                        } else {
                            offsetZ = direction * (box.maxZ - box.minZ);
                            remainingZ = motionZ = 0;
                        }

                        posX = ray.hitVec.x + offsetX;
                        posY = ray.hitVec.y + offsetY;
                        posZ = ray.hitVec.z + offsetZ;

                        if ( axis == EnumFacing.Axis.Y ) {
                            EnumFacing face = ray.sideHit.getOpposite();
                            final BlockPos bp = new BlockPos(posX, posY, posZ);
                            if ( !bp.equals(slidePos) || slideFace != face ) {
                                slidePos = bp;
                                slideFace = face;

                                if ( slideFactor != 1 ) {
                                    remainingX *= slideFactor;
                                    remainingY *= slideFactor;
                                    remainingZ *= slideFactor;
                                    motionX *= slideFactor;
                                    motionY *= slideFactor;
                                    motionZ *= slideFactor;
                                }
                            }
                        }

                        if ( remainingX != 0 || remainingY != 0 || remainingZ != 0 ) {
                            ++steps;
                            continue;
                        }


                    } else if ( reaction.type == HitReactionType.BOUNCE ) {

                        double offsetX = 0;
                        double offsetY = 0;
                        double offsetZ = 0;

                        distance += new Vec3d(ray.hitVec.x - posX, ray.hitVec.y - posY, ray.hitVec.z - posZ).length();

                        remainingX -= ray.hitVec.x - posX;
                        remainingY -= ray.hitVec.y - posY;
                        remainingZ -= ray.hitVec.z - posZ;

                        AxisAlignedBB box = getEntityBoundingBox();
                        EnumFacing.Axis axis = ray.sideHit.getAxis();
                        int direction = ray.sideHit.getAxisDirection().getOffset();

                        if ( axis == EnumFacing.Axis.X ) {
                            offsetX = direction * (box.maxX - box.minX);
                            remainingX *= -reaction.scale;
                            motionX *= -reaction.scale;
                        } else if ( axis == EnumFacing.Axis.Y ) {
                            offsetY = direction * (box.maxY - box.minY);
                            remainingY *= -reaction.scale;
                            motionY *= -reaction.scale;
                        } else {
                            offsetZ = direction * (box.maxZ - box.minZ);
                            remainingZ *= -reaction.scale;
                            motionZ *= -reaction.scale;
                        }

                        posX = ray.hitVec.x + offsetX;
                        posY = ray.hitVec.y + offsetY;
                        posZ = ray.hitVec.z + offsetZ;

                        bounces++;
                        if ( !world.isRemote && !triggeredBounce && bounces >= 10 ) {
                            triggeredBounce = true;
                            EntityLivingBase thrower = getThrower();
                            if ( thrower instanceof EntityPlayerMP )
                                ModAdvancements.REPULSION.trigger((EntityPlayerMP) thrower);
                        }

                        if ( remainingX != 0 || remainingY != 0 || remainingZ != 0 ) {
                            ++steps;
                            continue;
                        }

                    } else if ( isSlowed ) {
                        distance += new Vec3d(ray.hitVec.x - posX, 0, ray.hitVec.z - posZ).length();

                        remainingX -= ray.hitVec.x - posX;
                        remainingY -= ray.hitVec.y - posY;
                        remainingZ -= ray.hitVec.z - posZ;

                        posX = ray.hitVec.x;
                        posY = ray.hitVec.y;
                        posZ = ray.hitVec.z;

                        if ( remainingX != 0 || remainingY != 0 || remainingZ != 0 ) {
                            ++steps;
                            continue;
                        }

                    } else if ( reaction.type == HitReactionType.GLOW ) {
                        glowTicks = (short) Math.floor(reaction.scale);
                        setGlowing(glowTicks > 0);

                    } else if ( reaction.type == HitReactionType.IGNORE ) {
                        /* ~ Nothing ~ */

                    } else if ( !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, ray) ) {
                        posX = ray.hitVec.x;
                        posY = ray.hitVec.y;
                        posZ = ray.hitVec.z;

                        AxisAlignedBB box = getEntityBoundingBox();
                        EnumFacing.Axis axis = ray.sideHit.getAxis();
                        int direction = ray.sideHit.getAxisDirection().getOffset();

                        if ( axis == EnumFacing.Axis.X )
                            posX += direction * (box.maxX - box.minX);
                        else if ( axis == EnumFacing.Axis.Y )
                            posY += direction * (box.maxY - box.minY);
                        else
                            posZ += direction * (box.maxZ - box.minZ);

                        onImpact(ray);
                    }
                }
            }

            break;
        }

        if ( !isDead ) {
            if ( steps > 0 )
                markVelocityChanged();

            distance += new Vec3d(remainingX, 0, remainingZ).length();

            posX += remainingX;
            posY += remainingY;
            posZ += remainingZ;
        }

        if ( !world.isRemote ) {
            if ( !triggeredDistance && distance >= 300 ) {
                triggeredDistance = true;
                EntityLivingBase thrower = getThrower();
                if ( thrower instanceof EntityPlayerMP )
                    ModAdvancements.LONG_DISTANCE.trigger((EntityPlayerMP) thrower);
            }

            if ( started && isDead && !triggeredRoundabout && distance >= 200 ) {
                double d0 = startX - posX;
                double d1 = startY - posY;
                double d2 = startZ - posZ;
                double dist = d0 * d0 + d1 * d1 + d2 * d2;

                if ( dist <= 100 ) {
                    triggeredRoundabout = true;
                    EntityLivingBase thrower = getThrower();
                    if ( thrower instanceof EntityPlayerMP )
                        ModAdvancements.ROUNDABOUT.trigger((EntityPlayerMP) thrower);
                }
            }
        }

        if ( !isDead ) {
            float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

            for (
                    rotationPitch = (float) (MathHelper.atan2(motionY, (double) f) * (180D / Math.PI));
                    rotationPitch - prevRotationPitch < -180F;
                    prevRotationPitch -= 360F
            ) {
            }

            while ( rotationPitch - prevRotationPitch >= 180F )
                prevRotationPitch += 360F;

            while ( rotationYaw - prevRotationYaw < -180F )
                prevRotationYaw -= 360F;

            while ( rotationYaw - prevRotationYaw >= 180F )
                prevRotationYaw += 360F;

            rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
            rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

            final float drag;

            if ( slideFace != null ) {
                double velocity = Math.abs(motionX) + Math.abs(motionZ);
                if ( velocity < 0.1D ) {
                    slideFace = null;
                    slidePos = null;
                    slideFactor = 1;
                } else {
                    final BlockPos bp = new BlockPos(posX, posY, posZ);
                    if ( !bp.equals(slidePos) ) {
                        BlockPos ps = bp.offset(slideFace);
                        IBlockState bs = world.getBlockState(ps);
                        HitReaction reaction = hitBlock(bs, null);
                        if ( reaction == null || reaction.type != HitReactionType.SLIDE ) {
                            slideFace = null;
                            slidePos = null;
                            slideFactor = 1;
                        } else {
                            slidePos = bp;
                            slideFactor = (float) reaction.scale;
                            if ( slideFactor == 0 )
                                slideFactor = bs.getBlock().getSlipperiness(bs, world, ps, this);

                            motionZ *= slideFactor;
                            motionY *= slideFactor;
                            motionX *= slideFactor;
                        }
                    }

                    if ( slideFace != null && motionY <= 0 )
                        onGround = true;
                }
            }

            if ( isInWater() ) {
                for (int j = 0; j < 4; ++j) {
                    float f3 = 0.25F;
                    world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25D, posY - motionY * 0.25D, posZ - motionZ * 0.25D, motionX, motionY, motionZ);
                }

                drag = 0.8F;
            } else if ( slideFace != null )
                drag = 1F;
            else
                drag = 1F; //0.99F; // TODO: Turn drag back on!

            if ( drag != 1 && !isDragless() ) {
                motionX *= (double) drag;
                motionY *= (double) drag;
                motionZ *= (double) drag;
            }

            if ( !onGround && !hasNoGravity() ) {
                float gravity = getGravityVelocity();
                if ( isSlowed )
                    gravity *= 0.5D;
                if ( slideFace != null )
                    gravity *= 1D - slideFactor;

                motionY -= (double) gravity;
            }

            setPosition(posX, posY, posZ);

            if ( world.isRemote ) {
                int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
                if ( setting != 2 ) {
                    final byte skip = setting == 0 ? (byte) 0 : (byte) 2;
                    if ( skippedTrailFrames >= skip ) {
                        renderTrail();
                        skippedTrailFrames = 0;
                    } else
                        skippedTrailFrames++;
                }
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if ( started ) {
            compound.setDouble("startX", startX);
            compound.setDouble("startY", startY);
            compound.setDouble("startZ", startZ);
        }

        compound.setBoolean("Dragless", isDragless());
        compound.setShort("bounces", bounces);
        compound.setDouble("distance", distance);

        if ( speedModPos != null )
            compound.setLong("slowedBy", speedModPos.toLong());

        if ( slideFace != null )
            compound.setByte("SlideFace", (byte) slideFace.ordinal());

        if ( slidePos != null )
            compound.setLong("SlidePos", slidePos.toLong());

        if ( glowTicks != 0 )
            compound.setShort("Glow", glowTicks);

        ItemStack stack = getStack();
        if ( !stack.isEmpty() )
            compound.setTag("Item", stack.serializeNBT());

        compound.setFloat("SlideFactor", slideFactor);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if ( compound.hasKey("SlideFace", Constants.NBT.TAG_BYTE) )
            slideFace = EnumFacing.byIndex(compound.getByte("SlideFace"));
        else
            slideFace = null;

        if ( compound.hasKey("SlidePos", Constants.NBT.TAG_LONG) )
            slidePos = BlockPos.fromLong(compound.getLong("SlidePos"));
        else
            slidePos = null;

        if ( compound.hasKey("SlideFactor", Constants.NBT.TAG_FLOAT) )
            slideFactor = compound.getFloat("SlideFactor");
        else
            slideFactor = 1;

        glowTicks = compound.getShort("Glow");
        setGlowing(glowTicks > 0);

        setDragless(compound.getBoolean("Dragless"));

        distance = compound.getDouble("distance");
        bounces = compound.getShort("bounces");
        if ( compound.hasKey("startX") ) {
            started = true;
            startX = compound.getDouble("startX");
            startY = compound.getDouble("startY");
            startZ = compound.getDouble("startZ");
        } else {
            started = false;
            startX = startY = startZ = 0;
        }

        if ( compound.hasKey("Item", Constants.NBT.TAG_COMPOUND) )
            setStack(new ItemStack(compound.getCompoundTag("Item")));
        else
            setStack(ItemStack.EMPTY);

        if ( compound.hasKey("slowedBy") )
            speedModPos = BlockPos.fromLong(compound.getLong("slowedBy"));
        else
            speedModPos = null;
    }
}
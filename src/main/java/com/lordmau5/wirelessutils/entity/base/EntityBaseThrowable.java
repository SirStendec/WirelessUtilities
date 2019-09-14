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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public abstract class EntityBaseThrowable extends EntityThrowable {

    public enum HitReactionType {
        NONE,
        BOUNCE,
        IGNORE,
        SPEED
    }

    public static final Map<Block, HitReaction> REACTIONS = new Object2ObjectOpenHashMap<>();

    public static void initReactions() {
        REACTIONS.put(Blocks.TRIPWIRE, HitReaction.IGNORE);
        REACTIONS.put(Blocks.WEB, HitReaction.SLOW);
        REACTIONS.put(Blocks.SLIME_BLOCK, HitReaction.BOUNCE);
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
    }


    /* Class */

    private BlockPos slowedBy;
    private ItemStack stack = ItemStack.EMPTY;

    private boolean triggeredRoundabout = false;
    private boolean triggeredDistance = false;
    private boolean triggeredBounce = false;
    private short bounces = 0;
    private double distance = 0;

    private boolean started = false;
    private double startX;
    private double startY;
    private double startZ;


    public EntityBaseThrowable(World world) {
        super(world);
    }

    public EntityBaseThrowable(World world, ItemStack stack) {
        super(world);
        this.stack = stack.copy();
    }

    public EntityBaseThrowable(World world, EntityLivingBase thrower) {
        super(world, thrower);
        this.thrower = thrower;
    }

    public EntityBaseThrowable(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower);
        this.stack = stack.copy();
    }

    public EntityBaseThrowable(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityBaseThrowable(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z);
        this.stack = stack.copy();
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(@Nonnull ItemStack stack) {
        this.stack = stack;
    }

    public void dropThis() {
        dropItemStack(getStack());
    }

    public void dropItemStack(@Nonnull ItemStack stack) {
        EntityItem entity;
        Item item = stack.getItem();
        if ( item instanceof IEnhancedItem )
            entity = new EntityItemEnhanced(world, posX, posY, posZ, stack);
        else
            entity = new EntityItem(world, posX, posY, posZ, stack);

        EntityLivingBase thrower = getThrower();
        if ( thrower instanceof EntityPlayer )
            entity.setThrower(thrower.getName());

        entity.setDefaultPickupDelay();
        world.spawnEntity(entity);
    }

    public void dropItemWithMeta(Item item, int count) {
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
        if ( predicate == null )
            return null;

        Predicate<? super Entity> realPred = entity -> {
            if ( entity == this )
                return false;

            if ( !predicate.apply(entity) )
                return false;

            if ( thrower != null && ticksExisted < 2 && ignoreEntity == null )
                ignoreEntity = entity;

            return entity != ignoreEntity || ticksExisted >= 5;
        };

        return RayTracing.findEntityOnPath(world, start, end, realPred);
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

        int steps = 0;

        boolean isSlowed = false;

        while ( steps < 10 && !isDead ) {
            Vec3d currentPos = new Vec3d(posX, posY, posZ);
            Vec3d targetPos = new Vec3d(posX + remainingX, posY + remainingY, posZ + remainingZ);

            RayTraceResult ray = world.rayTraceBlocks(currentPos, targetPos, false, false, false);

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

                    if ( remainingX > 0 || remainingY > 0 || remainingZ > 0 ) {
                        ++steps;
                        continue;
                    }
                }
            }

            if ( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK ) {
                IBlockState blockState = world.getBlockState(ray.getBlockPos());
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

                            if ( remainingX > 0 || remainingY > 0 || remainingZ > 0 ) {
                                ++steps;
                                continue;
                            }
                        }
                    } else if ( !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, ray) )
                        onImpact(ray);

                } else {
                    HitReaction reaction = hitBlock(blockState, ray);
                    if ( reaction == null )
                        reaction = HitReaction.NONE;

                    isSlowed = reaction.type == HitReactionType.SPEED;
                    if ( isSlowed ) {
                        if ( slowedBy == null || !slowedBy.equals(ray.getBlockPos()) ) {
                            slowedBy = ray.getBlockPos();
                            remainingX *= reaction.scale;
                            motionX *= reaction.scale;
                            remainingY *= reaction.scale;
                            motionY *= reaction.scale;
                            remainingZ *= reaction.scale;
                            motionZ *= reaction.scale;
                        }
                    } else {
                        slowedBy = null;
                    }

                    if ( reaction.type == HitReactionType.BOUNCE ) {
                        // Enforce a minimum velocity for bouncing.
                        double velocity = Math.abs(motionX) + Math.abs(motionY) + Math.abs(motionZ);
                        if ( velocity < 0.2D && motionY <= 0 )
                            reaction = HitReaction.NONE;
                    }

                    if ( reaction.type == HitReactionType.BOUNCE ) {

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

                        if ( remainingX > 0 || remainingY > 0 || remainingZ > 0 ) {
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

                        if ( remainingX > 0 || remainingY > 0 || remainingZ > 0 ) {
                            ++steps;
                            continue;
                        }

                    } else if ( reaction == HitReaction.IGNORE ) {
                        /* ~ Nothing ~ */

                    } else if ( !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, ray) )
                        onImpact(ray);
                }
            }

            break;
        }

        if ( steps > 0 )
            markVelocityChanged();

        distance += new Vec3d(remainingX, 0, remainingZ).length();

        posX += remainingX;
        posY += remainingY;
        posZ += remainingZ;

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

        float f1 = 0.99F;
        float f2 = getGravityVelocity();

        if ( isInWater() ) {
            for (int j = 0; j < 4; ++j) {
                float f3 = 0.25F;
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25D, posY - motionY * 0.25D, posZ - motionZ * 0.25D, motionX, motionY, motionZ);
            }

            f1 = 0.8F;
        }

        motionX *= (double) f1;
        motionY *= (double) f1;
        motionZ *= (double) f1;

        if ( !hasNoGravity() ) {
            if ( isSlowed )
                f2 *= 0.5D;

            motionY -= (double) f2;
        }

        setPosition(posX, posY, posZ);

    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if ( started ) {
            compound.setDouble("startX", startX);
            compound.setDouble("startY", startY);
            compound.setDouble("startZ", startZ);
        }

        compound.setShort("bounces", bounces);
        compound.setDouble("distance", distance);

        if ( slowedBy != null )
            compound.setLong("slowedBy", slowedBy.toLong());

        if ( !stack.isEmpty() ) {
            NBTTagCompound item = new NBTTagCompound();
            stack.writeToNBT(item);
            compound.setTag("item", item);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        distance = compound.getDouble("distance");
        bounces = compound.getShort("bounces");
        if ( compound.hasKey("startX") ) {
            started = true;
            startX = compound.getDouble("startX");
            startY = compound.getDouble("startY");
            startZ = compound.getDouble("startZ");
        }

        if ( compound.hasKey("item") )
            stack = new ItemStack(compound.getCompoundTag("item"));

        if ( compound.hasKey("slowedBy") )
            slowedBy = BlockPos.fromLong(compound.getLong("slowedBy"));
    }
}
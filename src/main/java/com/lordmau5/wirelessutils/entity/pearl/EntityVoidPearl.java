package com.lordmau5.wirelessutils.entity.pearl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderPearl;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import com.lordmau5.wirelessutils.utils.mod.ModStats;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityVoidPearl extends EntityBaseThrowable {

    public static final Predicate<Entity> HIT_PREDICATE = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, Entity::canBeCollidedWith);

    public EntityVoidPearl(World world) {
        super(world);
    }

    public EntityVoidPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityVoidPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityVoidPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityVoidPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityVoidPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if ( world.isRemote && !isDead )
            world.spawnParticle(EnumParticleTypes.END_ROD, posX, posY, posZ, 0.2 * motionX, 0.2 * motionY, 0.2 * motionZ);
    }

    @Override
    public boolean shouldHitEntities() {
        return true;
    }

    @Nullable
    @Override
    public Predicate<? super Entity> getEntityPredicate() {
        return HIT_PREDICATE;
    }

    @Override
    public HitReaction hitBlock(IBlockState state, RayTraceResult result) {
        Block block = state.getBlock();
        if ( block == Blocks.END_GATEWAY || block == Blocks.END_PORTAL ) {
            if ( !world.isRemote )
                playSound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, 0.5F, 0.1F);
            return HitReaction.BOUNCE;
        }

        return super.hitBlock(state, result);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if ( world.isRemote )
            return;

        ItemStack stack = getStack();
        if ( ModItems.itemVoidPearl.isFilledBall(stack) ) {
            ItemStack released = ModItems.itemVoidPearl.releaseEntity(stack, world, result.hitVec);
            if ( !released.isEmpty() ) {
                stack.shrink(1);
                setStack(released);
            }

        } else if ( result.entityHit != null ) {
            ItemStack captured = ModItems.itemVoidPearl.saveEntity(stack, result.entityHit);
            if ( !captured.isEmpty() ) {
                EntityLivingBase thrower = getThrower();
                if ( thrower instanceof EntityPlayerMP ) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) thrower;
                    ModAdvancements.FOR_THEE.trigger(playerMP);
                    ModStats.CapturedEntities.addToPlayer(playerMP);
                }

                playSound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, 0.2F, 0.1F);

                if ( world instanceof WorldServer ) {
                    WorldServer ws = (WorldServer) world;
                    AxisAlignedBB box = result.entityHit.getRenderBoundingBox();

                    double sizeX = (box.maxX - box.minX) / 2;
                    double sizeY = (box.maxY - box.minY) / 2;
                    double sizeZ = (box.maxZ - box.minZ) / 2;

                    double centerX = result.entityHit.posX + sizeX;
                    double centerY = result.entityHit.posY + sizeY;
                    double centerZ = result.entityHit.posZ + sizeZ;

                    ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, centerX, centerY, centerZ, 5, sizeX, sizeY, sizeZ, 0D);
                }

                result.entityHit.setDead();
                stack.shrink(1);
                setStack(captured);
            }
        }

        dropThis();
        setDead();
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityVoidPearl> {
        @Override
        public Render<? super EntityVoidPearl> createRenderFor(RenderManager manager) {
            return new RenderPearl<EntityVoidPearl>(manager, ModItems.itemVoidPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}

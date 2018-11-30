package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderPearl;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityStabilizedEnderPearl extends EntityBaseThrowable {

    public EntityStabilizedEnderPearl(World world) {
        super(world);
    }

    public EntityStabilizedEnderPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityStabilizedEnderPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityStabilizedEnderPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityStabilizedEnderPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityStabilizedEnderPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        EntityLivingBase thrower = getThrower();
        if ( !world.isRemote && thrower == null ) {
            int radius = ModConfig.items.stabilizedEnderPearl.radius;
            if ( radius > 0 ) {
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(
                        posX - radius, posY - radius, posZ - radius,
                        posX + radius, posY + radius, posZ + radius
                ));

                double dist = Double.MAX_VALUE;

                if ( entities != null && !entities.isEmpty() )
                    for (EntityLivingBase entity : entities) {
                        double entityDist = entity.getDistanceSq(this);
                        if ( entityDist < dist ) {
                            thrower = entity;
                            dist = entityDist;
                        }
                    }
            }
        }

        if ( result.typeOfHit == RayTraceResult.Type.BLOCK ) {
            BlockPos pos = result.getBlockPos();
            TileEntity tile = world.getTileEntity(pos);
            if ( tile instanceof TileEntityEndGateway ) {
                TileEntityEndGateway gateway = (TileEntityEndGateway) tile;
                if ( thrower != null ) {
                    if ( thrower instanceof EntityPlayerMP )
                        CriteriaTriggers.ENTER_BLOCK.trigger((EntityPlayerMP) thrower, world.getBlockState(pos));

                    gateway.teleportEntity(thrower);
                    setDead();
                    return;
                }

                gateway.teleportEntity(this);
                return;
            }
        }

        for (int i = 0; i < 32; i++)
            world.spawnParticle(EnumParticleTypes.PORTAL, posX, posY + rand.nextDouble(), posZ, rand.nextGaussian(), 0, rand.nextGaussian());

        if ( !world.isRemote ) {
            if ( thrower instanceof EntityPlayerMP ) {
                EntityPlayerMP player = (EntityPlayerMP) thrower;
                if ( !player.connection.getNetworkManager().isChannelOpen() || player.world != world || player.isPlayerSleeping() ) {
                    setDead();
                    return;
                }
            }

            if ( thrower != null && thrower.getEntityWorld() == world ) {
                float maxDamage = thrower.getHealth() - 1F;
                if ( maxDamage < 0F )
                    maxDamage = 0F;

                EnderTeleportEvent event = new EnderTeleportEvent(thrower, posX, posY, posZ, Math.min(3F, maxDamage));
                if ( !MinecraftForge.EVENT_BUS.post(event) ) {
                    if ( thrower.isRiding() )
                        thrower.dismountRidingEntity();

                    thrower.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                    thrower.fallDistance = 0F;
                    thrower.attackEntityFrom(DamageSource.FALL, event.getAttackDamage());
                    thrower.world.playSound(null, thrower.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 0.2F, 1.0F);
                }
            }
        }

        setDead();
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityStabilizedEnderPearl> {
        @Override
        public Render<? super EntityStabilizedEnderPearl> createRenderFor(RenderManager manager) {
            return new RenderPearl<EntityStabilizedEnderPearl>(manager, ModItems.itemStabilizedEnderPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}

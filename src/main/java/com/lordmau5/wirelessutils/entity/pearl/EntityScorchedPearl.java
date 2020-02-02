package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderAsItem;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityScorchedPearl extends EntityBaseThrowable {

    public EntityScorchedPearl(World world) {
        super(world);
    }

    public EntityScorchedPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityScorchedPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityScorchedPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityScorchedPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityScorchedPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    public void renderTrail() {
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.2 * motionX, 0.2 * motionY, 0.2 * motionZ);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if ( !world.isRemote ) {
            if ( result.typeOfHit == RayTraceResult.Type.BLOCK && ModConfig.items.voidPearl.enableVoiding ) {
                BlockPos pos = result.getBlockPos();
                TileEntity tile = world.getTileEntity(pos);
                if ( tile instanceof TileEntityEndPortal ) {
                    ItemStack pearl = new ItemStack(ModItems.itemVoidPearl, 1);
                    EntityVoidPearl entity;
                    if ( thrower != null )
                        entity = new EntityVoidPearl(world, thrower, pearl);
                    else
                        entity = new EntityVoidPearl(world, pearl);

                    entity.setPosition(posX, posY, posZ);

                    EnumFacing.Axis axis = result.sideHit.getAxis();

                    double newX = motionX * .2D;
                    double newY = motionY * .2D;
                    double newZ = motionZ * .2D;

                    if ( axis == EnumFacing.Axis.X )
                        newX *= -1;
                    else if ( axis == EnumFacing.Axis.Y )
                        newY *= -1;
                    else
                        newZ *= -1;

                    if ( Math.sqrt(Math.pow(newX, 2) + Math.pow(newY, 2) + Math.pow(newZ, 2)) < .2 ) {
                        newX = newX > 0 ? .2D : -.2D;
                        newY = .2D;
                        newZ = newZ > 0 ? .2D : -.2D;
                    }

                    entity.motionX = newX;
                    entity.motionY = newY;
                    entity.motionZ = newZ;

                    world.spawnEntity(entity);

                    if ( world instanceof WorldServer ) {
                        WorldServer ws = (WorldServer) world;
                        ws.spawnParticle(EnumParticleTypes.PORTAL, posX, posY, posZ, 1, 0D, 0D, 0D, 0D);
                        ws.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.NEUTRAL, 1, 0.1F);
                    }

                    EntityLivingBase thrower = getThrower();
                    if ( thrower instanceof EntityPlayerMP )
                        ModAdvancements.THE_VOID_TOLLS.trigger((EntityPlayerMP) thrower);

                    setDead();
                    return;
                }
            }

            if ( ModConfig.items.scorchedPearl.fireOnImpact ) {
                BlockPos pos = result.getBlockPos().offset(result.sideHit);
                if ( world.isAirBlock(pos) )
                    world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }

            dropItemWithMeta(ModItems.itemScorchedPearl, 1);
            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityScorchedPearl> {
        @Override
        public Render<? super EntityScorchedPearl> createRenderFor(RenderManager manager) {
            return new RenderAsItem<EntityScorchedPearl>(manager, ModItems.itemScorchedPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }

}

package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderPearl;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
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
    public void onUpdate() {
        super.onUpdate();

        if ( world.isRemote && !isDead )
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.2 * motionX, 0.2 * motionY, 0.2 * motionZ);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if ( !world.isRemote ) {
            BlockPos pos = result.getBlockPos().offset(result.sideHit);
            if ( world.isAirBlock(pos) )
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());

            dropItemWithMeta(ModItems.itemScorchedPearl, 1);
            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityScorchedPearl> {
        @Override
        public Render<? super EntityScorchedPearl> createRenderFor(RenderManager manager) {
            return new RenderPearl<EntityScorchedPearl>(manager, ModItems.itemScorchedPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }

}

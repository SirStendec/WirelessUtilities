package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderPearl;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityQuenchedPearl extends EntityBaseThrowable {

    public EntityQuenchedPearl(World world) {
        super(world);
    }

    public EntityQuenchedPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityQuenchedPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityQuenchedPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityQuenchedPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityQuenchedPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if ( !world.isRemote ) {
            dropItemWithMeta(ModItems.itemQuenchedPearl, 1);
            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityQuenchedPearl> {
        @Override
        public Render<? super EntityQuenchedPearl> createRenderFor(RenderManager manager) {
            return new RenderPearl<EntityQuenchedPearl>(manager, ModItems.itemQuenchedPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}

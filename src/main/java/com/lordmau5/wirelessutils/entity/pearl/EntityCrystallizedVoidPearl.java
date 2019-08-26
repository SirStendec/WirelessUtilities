package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderPearl;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCrystallizedVoidPearl extends EntityBaseThrowable {

    public EntityCrystallizedVoidPearl(World world) {
        super(world);
    }

    public EntityCrystallizedVoidPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityCrystallizedVoidPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityCrystallizedVoidPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityCrystallizedVoidPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityCrystallizedVoidPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
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
        if ( !world.isRemote ) {
            dropThis();
            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityCrystallizedVoidPearl> {
        @Override
        public Render<? super EntityCrystallizedVoidPearl> createRenderFor(RenderManager manager) {
            return new RenderPearl<EntityCrystallizedVoidPearl>(manager, ModItems.itemCrystallizedVoidPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}

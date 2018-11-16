package com.lordmau5.wirelessutils.item;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class BehaviorProjectileAccurateDispense extends BehaviorDefaultDispenseItem {

    @Override
    protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
        World world = source.getWorld();
        IPosition position = BlockDispenser.getDispensePosition(source);
        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);

        IProjectile projectile = getProjectileEntity(world, position, stack);
        projectile.shoot(facing.getXOffset(), facing.getYOffset() + 0.1F, facing.getZOffset(), getProjectileVelocity(stack), getProjectileInaccuracy(stack));
        world.spawnEntity((Entity) projectile);
        stack.shrink(1);
        return stack;
    }

    @Override
    protected void playDispenseSound(IBlockSource source) {
        source.getWorld().playEvent(1002, source.getBlockPos(), 0);
    }

    protected abstract IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stack);

    public float getProjectileInaccuracy(ItemStack stack) {
        return 6.0F;
    }

    public float getProjectileVelocity(ItemStack stack) {
        return 1.1F;
    }
}

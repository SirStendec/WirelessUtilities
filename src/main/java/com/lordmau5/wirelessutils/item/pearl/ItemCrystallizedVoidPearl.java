package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.pearl.EntityCrystallizedVoidPearl;
import com.lordmau5.wirelessutils.item.base.ItemBaseVoidPearl;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCrystallizedVoidPearl extends ItemBaseVoidPearl {

    public ItemCrystallizedVoidPearl() {
        super();
        setName("crystallized_void_pearl");
    }

    @Nonnull
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityCrystallizedVoidPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityCrystallizedVoidPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityCrystallizedVoidPearl(worldIn, stack);
    }

    @Nonnull
    @Override
    public ItemStack saveEntity(@Nonnull ItemStack stack, @Nonnull Entity entity, @Nullable EntityPlayer player) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack removeEntity(@Nonnull ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFillBall(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEmptyBall(@Nonnull ItemStack stack) {
        return false;
    }
}

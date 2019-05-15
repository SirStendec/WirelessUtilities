package com.lordmau5.wirelessutils.item.base;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IDimensionallyStableItem {

    default boolean allowDimensionalTravel() {
        return false;
    }

    void onPortalImpact(@Nonnull ItemStack stack, @Nonnull EntityItem entity, @Nonnull IBlockState state);

}

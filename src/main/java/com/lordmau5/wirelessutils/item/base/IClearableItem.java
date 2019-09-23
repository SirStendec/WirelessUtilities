package com.lordmau5.wirelessutils.item.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IClearableItem {

    /**
     * Check if the provided ItemStack can be cleared by the provided EntityPlayer.
     *
     * @param stack  The ItemStack to check.
     * @param player An EntityPlayer responsible for clearing the item. May be null.
     * @return True if the provided player can clear the provided ItemStack.
     */
    boolean canClearItem(@Nonnull ItemStack stack, @Nullable EntityPlayer player);

    /**
     * Attempt to clear the configuration of the provided ItemStack.
     *
     * @param stack  The ItemStack to clear configuration from. Should not be modified.
     * @param player An EntityPlayer responsible for clearing the item. May be null.
     * @return If the item cannot be cleared or has no configuration TO clear,
     * ItemStack.EMPTY should be returned, otherwise a clone of the input ItemStack
     * with its configuration cleared should be returned.
     */
    @Nonnull
    ItemStack clearItem(@Nonnull ItemStack stack, @Nullable EntityPlayer player);

}

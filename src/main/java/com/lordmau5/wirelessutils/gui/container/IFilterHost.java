package com.lordmau5.wirelessutils.gui.container;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IFilterHost {

    int getSlots();

    @Nonnull
    ItemStack getStackInSlot(int slot);

    boolean setStackInSlot(int slot, @Nonnull ItemStack stack);

    void onSlotChanged(int slot);
}

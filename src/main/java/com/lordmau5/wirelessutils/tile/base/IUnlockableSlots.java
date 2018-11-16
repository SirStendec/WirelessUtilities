package com.lordmau5.wirelessutils.tile.base;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IUnlockableSlots {

    boolean isSlotUnlocked(int slotIndex);

    int getStackLimit(int slotIndex);

    int getStackLimit(int slotIndex, @Nonnull ItemStack stack);

}

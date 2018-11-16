package com.lordmau5.wirelessutils.item.base;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public interface ISlotContextTooltip {

    void addTooltipContext(List<String> tooltip, TileEntity tile, Slot slot, ItemStack stack);

}

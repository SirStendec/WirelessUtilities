package com.lordmau5.wirelessutils.utils;

import net.minecraft.nbt.NBTTagCompound;

public class ItemStackHandler extends net.minecraftforge.items.ItemStackHandler {
    public ItemStackHandler(int size) {
        super(size);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        nbt.removeTag("Size");
        super.deserializeNBT(nbt);
    }
}

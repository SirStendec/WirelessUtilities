package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2;

import appeng.api.util.AEColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class AEColorHelpers {

    @Nonnull
    public static AEColor fromItemStack(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("AEColor", Constants.NBT.TAG_BYTE) )
                return fromByte(tag.getByte("AEColor"));
        }

        return AEColor.TRANSPARENT;
    }

    @Nonnull
    public static AEColor fromByte(byte index) {
        AEColor[] colors = AEColor.values();
        if ( index < 0 )
            return colors[colors.length - 1];
        else if ( index >= colors.length )
            return colors[0];

        return colors[index];
    }
}

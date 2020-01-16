package com.lordmau5.wirelessutils.tile.base;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ITileInfoProvider {

    void getInfoTooltip(@Nonnull List<String> tooltip, @Nullable NBTTagCompound tag);

    @Nonnull
    default NBTTagCompound getInfoNBT(@Nonnull NBTTagCompound tag, @Nullable EntityPlayerMP player) {
        return tag;
    }

    default boolean skipWorkInfo() {
        return false;
    }

}

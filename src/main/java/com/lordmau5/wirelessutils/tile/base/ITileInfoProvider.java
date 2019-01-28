package com.lordmau5.wirelessutils.tile.base;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

public interface ITileInfoProvider {

    List<String> getInfoTooltips(@Nullable NBTTagCompound tag);

    NBTTagCompound getInfoNBT(NBTTagCompound tag);

}

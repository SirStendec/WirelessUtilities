package com.lordmau5.wirelessutils.tile.base;

import net.minecraft.item.ItemStack;

public interface IUpgradeable {

    boolean canUpgrade(ItemStack upgrade);

    boolean installUpgrade(ItemStack upgrade);

}

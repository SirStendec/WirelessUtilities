package com.lordmau5.wirelessutils.tile.base.augmentable;

import com.google.common.base.Predicate;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IFilterAugmentable {

    void setItemFilter(@Nullable Predicate<ItemStack> filter);

    void setVoidingItems(boolean voiding);

}

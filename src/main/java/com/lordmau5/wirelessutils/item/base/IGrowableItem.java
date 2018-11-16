package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IGrowableItem {

    void growthUpdate(@Nonnull ItemStack stack, @Nonnull EntityItemEnhanced entity);

}

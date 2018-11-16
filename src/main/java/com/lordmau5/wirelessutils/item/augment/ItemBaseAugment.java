package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBaseAugment extends ItemAugment {

    public ItemBaseAugment() {
        super();
        setName("base_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {

    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return false;
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return false;
    }
}

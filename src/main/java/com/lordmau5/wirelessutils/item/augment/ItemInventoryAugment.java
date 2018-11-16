package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInventoryAugmentable;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemInventoryAugment extends ItemAugment {
    public ItemInventoryAugment() {
        super();
        setName("inventory_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IInventoryAugmentable )
            ((IInventoryAugmentable) augmentable).setProcessItems(!stack.isEmpty());
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IInventoryAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IInventoryAugmentable;
    }
}

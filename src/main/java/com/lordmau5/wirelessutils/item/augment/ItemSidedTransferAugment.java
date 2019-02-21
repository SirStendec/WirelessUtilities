package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemSidedTransferAugment extends ItemAugment {

    public ItemSidedTransferAugment() {
        super();
        setName("auto_transfer_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ISidedTransferAugmentable )
            ((ISidedTransferAugmentable) augmentable).setSidedTransferAugmented(!ModConfig.augments.requireAutoTransferAugment || !stack.isEmpty());
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ISidedTransferAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ISidedTransferAugmentable;
    }
}

package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ITransferAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemTransferAugment extends ItemAugment {
    public ItemTransferAugment() {
        super();
        setName("transfer_augment");
    }

    @Override
    public int getTiers() {
        return Math.min(ModConfig.augments.transfer.availableTiers, Level.values().length);
    }

    public int getTransferFactor(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        return 1 + stack.getMetadata();
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ITransferAugmentable )
            ((ITransferAugmentable) augmentable).setTransferFactor(getTransferFactor(stack));
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ITransferAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ITransferAugmentable;
    }
}

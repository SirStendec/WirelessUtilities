package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemInvertAugment extends ItemAugment {
    public ItemInvertAugment() {
        super();
        setName("invert_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IInvertAugmentable )
            ((IInvertAugmentable) augmentable).setInvertAugmented(!stack.isEmpty());
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IInvertAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IInvertAugmentable;
    }
}

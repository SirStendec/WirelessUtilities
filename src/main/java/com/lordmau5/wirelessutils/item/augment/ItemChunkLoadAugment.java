package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IChunkLoadAugmentable;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemChunkLoadAugment extends ItemAugment {

    public ItemChunkLoadAugment() {
        super();
        setName("chunk_load_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IChunkLoadAugmentable )
            ((IChunkLoadAugmentable) augmentable).setChunkLoadAugmented(!stack.isEmpty());
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IChunkLoadAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IChunkLoadAugmentable;
    }
}

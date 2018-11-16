package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICapacityAugmentable;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemCapacityAugment extends ItemAugment {
    public ItemCapacityAugment() {
        super();
        setName("capacity_augment");
    }

    @Override
    public int getTiers() {
        return Math.min(ModConfig.augments.capacity.availableTiers, Level.values().length);
    }

    public int getCapacityFactor(ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        return 1 + stack.getMetadata();
    }

    @Override
    public void addSlotLockExplanation(List<String> tooltip, TileEntity tile, Slot slot, ItemStack stack) {
        super.addSlotLockExplanation(tooltip, tile, slot, stack);

        if ( tile instanceof TileEntityBaseCondenser )
            addLocalizedLines(tooltip, getTranslationKey() + ".lock.condenser");

        if ( tile instanceof TileBaseDesublimator )
            addLocalizedLines(tooltip, getTranslationKey() + ".lock.desublimator");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ICapacityAugmentable )
            ((ICapacityAugmentable) augmentable).setCapacityFactor(getCapacityFactor(stack));
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ICapacityAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ICapacityAugmentable;
    }
}

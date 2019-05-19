package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ITransferAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTransferAugment extends ItemAugment {
    public ItemTransferAugment() {
        super();
        setName("transfer_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        double[] multipliers = ModConfig.augments.transfer.energyMultiplier;
        if ( multipliers.length == 0 )
            return 1;

        int idx = getLevel(stack).toInt();
        if ( idx >= multipliers.length )
            idx = multipliers.length - 1;

        return multipliers[idx];
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] additions = ModConfig.augments.transfer.energyAddition;
        if ( additions.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= additions.length )
            idx = additions.length - 1;

        return additions[idx];
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] drain = ModConfig.augments.transfer.energyDrain;
        if ( drain.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= drain.length )
            idx = drain.length - 1;

        return drain[idx];
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
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ITransferAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ITransferAugmentable;
    }
}

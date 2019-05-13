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
import javax.annotation.Nullable;
import java.util.List;

public class ItemCapacityAugment extends ItemAugment {
    public ItemCapacityAugment() {
        super();
        setName("capacity_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        double[] multipliers = ModConfig.augments.capacity.energyMultiplier;
        if ( multipliers.length == 0 )
            return 1;

        int idx = getLevel(stack).toInt();
        if ( idx >= multipliers.length )
            idx = multipliers.length - 1;

        return multipliers[idx];
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] additions = ModConfig.augments.capacity.energyAddition;
        if ( additions.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= additions.length )
            idx = additions.length - 1;

        return additions[idx];
    }

    @Override
    public int getTiers() {
        return Math.min(ModConfig.augments.capacity.availableTiers, Level.values().length);
    }

    public int getCapacityFactor(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        return 1 + stack.getMetadata();
    }

    @Override
    public void addSlotLockExplanation(@Nonnull List<String> tooltip, @Nonnull TileEntity tile, @Nonnull Slot slot, @Nonnull ItemStack stack) {
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

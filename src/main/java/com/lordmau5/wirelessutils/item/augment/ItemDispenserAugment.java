package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBlockAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IDispenserAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemDispenserAugment extends ItemAugment {

    public ItemDispenserAugment() {
        super();
        setName("dispenser_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.dispenser.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.dispenser.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.dispenser.energyDrain;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.augments.dispenser.requiredLevel);
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.dispenser.budgetAddition;
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.dispenser.budgetMultiplier;
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IDispenserAugmentable )
            ((IDispenserAugmentable) augmentable).setDispenserAugmented(!stack.isEmpty());
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IDispenserAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ICropAugmentable && ((ICropAugmentable) augmentable).isCropAugmented() )
            return false;

        if ( augmentable instanceof IBlockAugmentable && ((IBlockAugmentable) augmentable).isBlockAugmented() )
            return false;

        if ( augmentable instanceof IWorldAugmentable && ((IWorldAugmentable) augmentable).isWorldAugmented() )
            return false;

        return augmentable instanceof IDispenserAugmentable;
    }
}

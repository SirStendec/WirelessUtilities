package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBlockAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IDispenserAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemWorldAugment extends ItemAugment {
    public ItemWorldAugment() {
        super();
        setName("world_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.world.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.world.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.world.energyDrain;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.augments.world.requiredLevel);
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IWorldAugmentable )
            ((IWorldAugmentable) augmentable).setWorldAugmented(!stack.isEmpty());
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        if ( TileBaseDesublimator.class.isAssignableFrom(klass) ) {
            if ( !ModConfig.desublimators.allowWorldAugment )
                return false;
        } else if ( TileEntityBaseCondenser.class.isAssignableFrom(klass) ) {
            if ( !ModConfig.condensers.allowWorldAugment )
                return false;
        }

        return IWorldAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ICropAugmentable && ((ICropAugmentable) augmentable).isCropAugmented() )
            return false;

        if ( augmentable instanceof IBlockAugmentable && ((IBlockAugmentable) augmentable).isBlockAugmented() )
            return false;

        if ( augmentable instanceof IDispenserAugmentable && ((IDispenserAugmentable) augmentable).isDispenserAugmented() )
            return false;

        return augmentable instanceof IWorldAugmentable;
    }
}

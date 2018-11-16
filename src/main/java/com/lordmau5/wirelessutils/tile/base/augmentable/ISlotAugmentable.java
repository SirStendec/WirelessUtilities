package com.lordmau5.wirelessutils.tile.base.augmentable;

import com.lordmau5.wirelessutils.utils.mod.ModConfig;

public interface ISlotAugmentable {

    void setUnlockedSlots(int slots, int tier);

    default int getBaseUnlockedSlots() {
        return 1;
    }

    default int getUnlockedSlotsPerTier() {
        return ModConfig.augments.slot.slotsPerTier;
    }
}

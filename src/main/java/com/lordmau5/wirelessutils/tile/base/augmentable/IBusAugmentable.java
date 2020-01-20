package com.lordmau5.wirelessutils.tile.base.augmentable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface IBusAugmentable {

    enum TickPhase {
        PRE,
        MAIN,
        POST
    }

    /* Energy */

    default boolean getBusShouldExtractEnergy(TickPhase phase) {
        return false;
    }

    default boolean getBusShouldInsertEnergy(TickPhase phase) {
        return false;
    }

    /* Items */

    /**
     * Items from this handler should be submitted to the digital storage system.
     *
     * @return An IItemHandler. May be null if no items should be submitted.
     */
    @Nullable
    IItemHandler getBusItemOutputHandler(TickPhase phase);

    /**
     * Items should be pulled from the digital storage system into this IItemHandler.
     * This is paired with getInputRequest() to determine which items we actually want.
     *
     * @return An IItemHandler to receive items.
     */
    @Nullable
    IItemHandler getBusItemInputHandler(TickPhase phase);

    /**
     * Do the item indices in the getInputRequest() array match the slots for the IItemHandler
     * or should we shove the items into any slots?
     *
     * @return
     */
    default boolean doesBusItemInputRequestMatchSlots(TickPhase phase) {
        return true;
    }

    /**
     * We want these items from the digital storage system. They should be inserted into
     * the input IItemHandler.
     *
     * @return An array of items we want.
     */
    @Nullable
    ItemStack[] getBusItemInputRequest(TickPhase phase);

    /* Fluids */

    @Nullable
    IFluidHandler getBusFluidOutputHandler(TickPhase phase);

    @Nullable
    IFluidHandler getBusFluidInputHandler(TickPhase phase);

    @Nullable
    FluidStack[] getBusFluidInputRequest(TickPhase phase);

}

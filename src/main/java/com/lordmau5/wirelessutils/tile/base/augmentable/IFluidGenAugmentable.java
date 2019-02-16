package com.lordmau5.wirelessutils.tile.base.augmentable;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidGenAugmentable {

    /**
     * Set the fluid generation of the machine.
     *
     * @param enabled    Whether or not fluid should be generated.
     * @param fluidStack The fluid to add to the tank every tick.
     * @param energy     The amount of energy it costs to add that fluid every tick.
     */
    void setFluidGenAugmented(boolean enabled, FluidStack fluidStack, int energy);

}

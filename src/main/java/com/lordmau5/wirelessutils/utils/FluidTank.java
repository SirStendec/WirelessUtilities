package com.lordmau5.wirelessutils.utils;

import cofh.core.fluid.FluidTankCore;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidTank extends FluidTankCore {

    boolean infinite;

    public FluidTank(int capacity) {
        super(capacity);
    }

    public FluidTank(FluidStack stack, int capacity) {
        super(stack, capacity);
    }

    public FluidTank(Fluid fluid, int amount, int capacity) {
        super(fluid, amount, capacity);
    }

    public FluidTank setInfinite() {
        return setInfinite(true);
    }

    public FluidTank setInfinite(boolean infinite) {
        this.infinite = infinite;
        if ( infinite && fluid != null )
            fluid.amount = capacity;

        return this;
    }

    @Override
    public void setCapacity(int capacity) {
        super.setCapacity(capacity);
        if ( infinite && fluid != null )
            fluid.amount = this.capacity;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if ( infinite ) {
            if ( resource != null && fluid == null )
                fluid = new FluidStack(resource, capacity);

            return 0;
        }

        return super.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if ( infinite ) {
            if ( fluid == null )
                return null;

            return new FluidStack(fluid, maxDrain);
        }

        return super.drain(maxDrain, doDrain);
    }
}

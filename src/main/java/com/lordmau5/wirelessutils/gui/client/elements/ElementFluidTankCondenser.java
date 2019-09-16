package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementFluidTank;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ElementFluidTankCondenser extends ElementFluidTank {
    private final TileEntityBaseCondenser condenser;

    public ElementFluidTankCondenser(GuiContainerCore gui, int posX, int posY, TileEntityBaseCondenser condenser) {
        super(gui, posX, posY, condenser.getTank());

        this.condenser = condenser;
    }

    public FluidStack getFluid() {
        return condenser.getTankFluid();
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( condenser.isCreative() && condenser.isInverted() )
            list.add(StringHelper.localize("info.wirelessutils.voiding_fluids"));
        else
            super.addTooltip(list);
    }
}

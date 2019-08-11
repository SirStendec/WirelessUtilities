package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementFluidTank;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;

import java.io.IOException;

public class ElementFluidTankVaporizer extends ElementFluidTank {

    private final TileBaseVaporizer vaporizer;

    public ElementFluidTankVaporizer(GuiContainerCore gui, int posX, int posY, TileBaseVaporizer vaporizer) {
        super(gui, posX, posY, vaporizer.getTank());
        this.vaporizer = vaporizer;
    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {


        return super.onMousePressed(mouseX, mouseY, mouseButton);
    }
}

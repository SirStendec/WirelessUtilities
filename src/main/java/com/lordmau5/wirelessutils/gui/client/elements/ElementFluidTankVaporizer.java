package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementFluidTank;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;

public class ElementFluidTankVaporizer extends ElementFluidTank {

    private final TileBaseVaporizer vaporizer;

    public ElementFluidTankVaporizer(GuiContainerCore gui, int posX, int posY, TileBaseVaporizer vaporizer) {
        super(gui, posX, posY, vaporizer.getTank());
        this.vaporizer = vaporizer;
    }

    public FluidStack getFluid() {
        return vaporizer.getTankFluid();
    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        if ( !ModConfig.vaporizers.allowConversion || vaporizer.hasSpecialFluid() )
            return super.onMousePressed(mouseX, mouseY, mouseButton);

        vaporizer.setFluidSwap((byte) (mouseButton == 1 ? -1 : 1));
        vaporizer.sendModePacket();
        BaseGuiContainer.playClickSound(mouseButton == 1 ? 1F : 0.7F);
        return true;
    }
}

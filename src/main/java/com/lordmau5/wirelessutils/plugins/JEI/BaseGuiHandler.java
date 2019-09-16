package com.lordmau5.wirelessutils.plugins.JEI;

import cofh.core.gui.element.ElementBase;
import com.google.common.collect.Lists;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementFluidLock;
import com.lordmau5.wirelessutils.gui.client.elements.ElementFluidTankCondenser;
import com.lordmau5.wirelessutils.gui.client.elements.ElementFluidTankVaporizer;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class BaseGuiHandler implements IAdvancedGuiHandler<BaseGuiContainer> {

    public Class<BaseGuiContainer> getGuiContainerClass() {
        return BaseGuiContainer.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(BaseGuiContainer gui) {
        Rectangle pages = gui.getPageTabArea();
        if ( pages == null )
            return null;

        return Lists.newArrayList(pages);
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(BaseGuiContainer gui, int mouseX, int mouseY) {
        mouseX -= gui.getGuiLeft();
        mouseY -= gui.getGuiTop();

        ElementBase element = gui.getElementAtPosition(mouseX, mouseY);
        if ( element instanceof ElementFluidTankCondenser ) {
            ElementFluidTankCondenser tank = (ElementFluidTankCondenser) element;
            return tank.getFluid();
        }

        if ( element instanceof ElementFluidTankVaporizer ) {
            ElementFluidTankVaporizer tank = (ElementFluidTankVaporizer) element;
            return tank.getFluid();
        }

        if ( element instanceof ElementFluidLock ) {
            ElementFluidLock lock = (ElementFluidLock) element;
            return lock.getFluidStack();
        }

        return null;
    }
}

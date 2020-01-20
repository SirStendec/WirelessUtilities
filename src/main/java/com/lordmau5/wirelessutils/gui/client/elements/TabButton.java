package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.tab.TabBase;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;

import java.io.IOException;

public abstract class TabButton extends TabBase {

    public TabButton(GuiContainerCore gui, int side) {
        super(gui, side);

        tabExpandSpeed = 1000;
        open = false;
    }

    public abstract void onClick(int mouseX, int mouseY, int mouseButton);

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        onClick(mouseX, mouseY, mouseButton);
        BaseGuiContainer.playClickSound(mouseButton == 1 ? 1F : 0.7F);
        return true;
    }

    @Override
    public void update() {
        open = false;
        super.update();
    }
}

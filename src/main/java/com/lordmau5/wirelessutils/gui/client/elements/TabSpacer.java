package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.tab.TabBase;

public class TabSpacer extends TabBase {

    public TabSpacer(GuiContainerCore gui, int side, int height) {
        super(gui, side);
        minHeight = height;
        maxHeight = height;
        currentHeight = height;
    }

    @Override
    protected void drawForeground() {
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
    }

    @Override
    public void toggleOpen() {

    }
}

package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;

public interface IContainsButtons {
    GuiContainerCore getGui();

    void handleElementButtonClick(String name, int button);
}

package com.lordmau5.wirelessutils.gui.client.elements;

import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemCaptureModule;

public class ElementCaptureModule extends ElementFilterableModule {

    public ElementCaptureModule(GuiBaseVaporizer gui, ItemCaptureModule.CaptureBehavior behavior) {
        super(gui, behavior);
    }

    int getContentHeight() {
        return 8;
    }
}

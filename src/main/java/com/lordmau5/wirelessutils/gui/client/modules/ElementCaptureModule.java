package com.lordmau5.wirelessutils.gui.client.modules;

import com.lordmau5.wirelessutils.gui.client.modules.base.ElementFilterableModule;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemCaptureModule;

public class ElementCaptureModule extends ElementFilterableModule {

    public ElementCaptureModule(GuiBaseVaporizer gui, ItemCaptureModule.CaptureBehavior behavior) {
        super(gui, behavior);
    }

    public int getContentHeight() {
        return 8;
    }
}

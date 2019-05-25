package com.lordmau5.wirelessutils.gui.client.elements;

import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemTeleportModule;

public class ElementTeleportModule extends ElementFilterableModule {

    public ElementTeleportModule(GuiBaseVaporizer gui, ItemTeleportModule.TeleportBehavior behavior) {
        super(gui, behavior);
    }

    int getContentHeight() {
        return 8;
    }
}

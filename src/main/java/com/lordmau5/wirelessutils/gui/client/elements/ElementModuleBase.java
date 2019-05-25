package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import net.minecraft.client.gui.FontRenderer;

public class ElementModuleBase extends ElementContainer {

    protected final GuiBaseVaporizer gui;

    protected boolean drawTitle = true;
    protected String name = "btn." + WirelessUtils.MODID + ".module";

    public ElementModuleBase(GuiBaseVaporizer gui) {
        super(gui, 0, 20, 175, 145);
        this.gui = gui;
    }

    public void drawTab(boolean focused) {
        FontRenderer fr = getFontRenderer();

        if ( drawTitle ) {
            String title = StringHelper.localize(name);
            fr.drawString(title, gui.getCenteredOffset(title, 25), 8, 0x404040);
        }
    }
}

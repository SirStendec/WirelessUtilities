package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.init.CoreTextures;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiAdminAugment;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;

import java.util.List;

public class TabOpenGui extends TabButton {

    public static final int defaultSide = 0;
    public static final int defaultBackgroundColor = 0x0a76d0;

    private final GuiAdminAugment gui;

    public TabOpenGui(GuiAdminAugment gui) {
        this(gui, defaultSide);
    }

    public TabOpenGui(GuiAdminAugment gui, int side) {
        super(gui, side);
        this.gui = gui;

        setVisible(gui.hasNormalGui());
        backgroundColor = defaultBackgroundColor;
    }

    public void onClick(int mouseX, int mouseY, int mouseButton) {
        gui.openNormalGui();
    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);
        TextHelpers.addLocalizedLines(list, "tab." + WirelessUtils.MODID + ".open_gui", null);
    }

    @Override
    protected void drawForeground() {
        drawTabIcon(CoreTextures.ICON_CONFIG);
    }
}

package com.lordmau5.wirelessutils.gui.client.modules.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.ElementContainer;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class ElementModuleBase extends ElementContainer {

    protected final GuiBaseVaporizer gui;

    protected boolean drawTitle = true;
    protected String name = "btn." + WirelessUtils.MODID + ".module";

    public ElementModuleBase(GuiBaseVaporizer gui) {
        super(gui, 0, 20, 176, 222);
        this.gui = gui;
    }

    public void drawTab(boolean focused) {
        FontRenderer fr = getFontRenderer();

        if ( drawTitle ) {
            String title = StringHelper.localize(name);
            fr.drawString(title, gui.getCenteredOffset(title, 25), 8, 0x404040);
        }
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        gui.bindTexture(GuiBaseVaporizer.TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        gui.drawSizedTexturedModalRect(7, 159, 7, 27, 162, 76, 256, 256);

        super.drawBackground(mouseX, mouseY, gameTicks);
    }
}

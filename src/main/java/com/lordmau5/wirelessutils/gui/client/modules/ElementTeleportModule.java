package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementFilterableModule;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemTeleportModule;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class ElementTeleportModule extends ElementFilterableModule {

    private final ItemTeleportModule.TeleportBehavior behavior;

    public ElementTeleportModule(GuiBaseVaporizer gui, ItemTeleportModule.TeleportBehavior behavior) {
        super(gui, behavior);
        this.behavior = behavior;
    }

    public int getContentHeight() {
        return behavior.wantsFluid() ? 33 : 22;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        FontRenderer fontRenderer = getFontRenderer();

        String range = StringHelper.formatNumber(behavior.getRange());
        if ( behavior.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        gui.drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 90, posY + 9, 0x404040);
        fontRenderer.drawString(range, 94, posY + 9, 0);

        if ( behavior.wantsFluid() ) {
            int cost = behavior.getFuel();
            String sCost = StringHelper.formatNumber(cost);

            if ( gui.getVaporizer().hasFluid() )
                sCost = new TextComponentTranslation(
                        "btn." + WirelessUtils.MODID + ".cost_fluid",
                        sCost,
                        StringHelper.formatNumber(cost * gui.getVaporizer().getFluidRate())
                ).getFormattedText();

            gui.drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".cost"), 90, posY + 20, 0x404040);
            fontRenderer.drawString(sCost, 94, posY + 20, 0);
        }
    }
}

package com.lordmau5.wirelessutils.gui.client;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.container.items.ContainerPlayerCard;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GuiPlayerCard extends BaseGuiItem {

    private final ContainerPlayerCard container;

    public GuiPlayerCard(ContainerPlayerCard container) {
        super(container);
        this.container = container;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTick, x, y);

        int posX = guiLeft;
        int posY = guiTop;

        GlStateManager.color(1F, 1F, 1F, 1F);

        bindTexture(TEXTURE);
        drawTexturedModalRect(29, 8, 199, 0, 51, 72);

        GuiInventory.drawEntityOnScreen(
                posX + 55,
                posY + 75,
                30,
                (float) 55 - mouseX,
                (float) 25 - mouseY,
                mc.player
        );
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String name = container.getPlayerName();
        if ( name == null )
            return;

        fontRenderer.drawString(new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".player_positional_card.player",
                new TextComponentString(name).setStyle(new Style().setColor(TextFormatting.BLACK))
        ).getFormattedText(), 84, 18, 0x404040);
    }
}

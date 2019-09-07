package com.lordmau5.wirelessutils.gui.client.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import net.minecraft.util.ResourceLocation;

public class BaseGuiItem extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/player_card.png");

    protected boolean drawName = true;
    protected boolean drawOwnInventory = true;

    public BaseGuiItem(BaseContainerItem container, ResourceLocation texture) {
        super(container, texture);

        xSize = 198;
        drawTitle = false;
        drawInventory = false;
    }

    public BaseGuiItem(BaseContainerItem container) {
        super(container, TEXTURE);

        xSize = 198;
        drawTitle = false;
        drawInventory = false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        if ( drawName && name != null ) {
            String localized = StringHelper.localize(name);
            fontRenderer.drawString(localized, getCenteredOffset(localized, (xSize - 22) / 2 + 22), 6, 0x404040);
        }

        if ( drawOwnInventory )
            fontRenderer.drawString(StringHelper.localize("container.inventory"), 30, ySize - 96 + 3, 0x404040);
    }
}

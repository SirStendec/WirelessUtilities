package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile.TileAENetworkBase;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class TabChannels extends TabBase {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");
    public static final String INTL_KEY = "info." + WirelessUtils.MODID + ".ae2.tab";

    public static final int defaultSide = 0;
    public static final int defaultHeaderColor = 0xE1C92F;
    public static final int defaultSubHeaderColor = 0xAAAFB8;
    public static final int defaultTextColor = 0x000000;
    public static final int defaultBackgroundColor = 0x089e4c;

    private final ItemStack item;

    private final BaseGuiContainer gui;
    private final TileAENetworkBase provider;
    private final boolean hasChannels;

    public TabChannels(BaseGuiContainer gui, TileAENetworkBase provider) {
        this(gui, defaultSide, provider);
    }

    public TabChannels(BaseGuiContainer gui, int side, TileAENetworkBase provider) {
        super(gui, side);
        this.gui = gui;
        this.provider = provider;

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        if ( provider.getMaxChannels() > 8 )
            item = AEApi.instance().definitions().parts().cableDenseSmart().stack(AEColor.TRANSPARENT, 1);
        else
            item = AEApi.instance().definitions().parts().cableSmart().stack(AEColor.TRANSPARENT, 1);

        hasChannels = AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS);

        maxHeight = 72 + (hasChannels ? 24 : 0);
        maxWidth = 100;
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( isFullyOpened() )
            return;

        if ( hasChannels )
            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".ae2.channels",
                    TextHelpers.getComponent(provider.getUsedChannels()).setStyle(TextHelpers.YELLOW),
                    TextHelpers.getComponent(provider.getMaxChannels()).setStyle(TextHelpers.YELLOW)
            ).getFormattedText());

        list.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".work_info.tooltip.targets",
                TextHelpers.getComponent(provider.getUsedConnections()).setStyle(TextHelpers.YELLOW),
                TextHelpers.getComponent(provider.getMaxTargets()).setStyle(TextHelpers.YELLOW)
        ).getFormattedText());
    }

    @Override
    protected void drawForeground() {
        RenderHelper.enableGUIStandardItemLighting();
        gui.getItemRenderer().renderItemAndEffectIntoGUI(item, sideOffset(), 3);
        GlStateManager.disableLighting();

        if ( !isFullyOpened() )
            return;

        FontRenderer fontRenderer = getFontRenderer();

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".name"), sideOffset() + 20, 8, headerColor);

        if ( hasChannels ) {
            fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".channels"), sideOffset() + 6, 22, subheaderColor);
            fontRenderer.drawString(StringHelper.localizeFormat(
                    INTL_KEY + ".info",
                    StringHelper.formatNumber(provider.getUsedChannels()),
                    StringHelper.formatNumber(provider.getMaxChannels())
            ), sideOffset() + 14, 34, textColor);
        }

        final int yOffset = hasChannels ? 24 : 0;

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".targets"), sideOffset() + 6, 22 + yOffset, subheaderColor);
        fontRenderer.drawString(StringHelper.localizeFormat(
                INTL_KEY + ".info",
                StringHelper.formatNumber(provider.getUsedConnections()),
                StringHelper.formatNumber(provider.getMaxTargets())
        ), sideOffset() + 14, 34 + yOffset, textColor);

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".power"), sideOffset() + 6, 46 + yOffset, subheaderColor);
        fontRenderer.drawString(StringHelper.formatNumber(provider.getPowerDraw()) + " AE", sideOffset() + 14, 58 + yOffset, textColor);
    }
}

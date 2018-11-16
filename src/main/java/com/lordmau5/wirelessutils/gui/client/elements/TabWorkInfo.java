package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class TabWorkInfo extends TabBase {

    public static ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");
    public static String INTL_KEY = "info." + WirelessUtils.MODID + ".work_info";

    public static int defaultSide = 0;
    public static int defaultHeaderColor = 0xE1C92F;
    public static int defaultSubHeaderColor = 0xAAAFB8;
    public static int defaultTextColor = 0x000000;
    public static int defaultBackgroundColor = 0x089e4c;

    private final BaseGuiContainer gui;
    private final IWorkInfoProvider provider;

    private TextureAtlasSprite icon;
    private ItemStack item;

    public TabWorkInfo(BaseGuiContainer gui, IWorkInfoProvider provider) {
        this(gui, defaultSide, provider);
    }

    public TabWorkInfo(BaseGuiContainer gui, int side, IWorkInfoProvider provider) {
        super(gui, side);
        this.gui = gui;
        this.provider = provider;

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        maxHeight = 96;
        maxWidth = 100;
    }

    public TabWorkInfo setIcon(TextureAtlasSprite icon) {
        this.icon = icon;
        return this;
    }

    public TabWorkInfo setItem(ItemStack stack) {
        this.item = stack;
        return this;
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !isFullyOpened() ) {
            list.add(new TextComponentTranslation(
                    INTL_KEY + ".tooltip.rate",
                    TextHelpers.getComponent(provider.formatWorkUnit(provider.getWorkLastTick())).setStyle(TextHelpers.YELLOW)
            ).getFormattedText());
            list.add(new TextComponentTranslation(
                    INTL_KEY + ".tooltip.targets",
                    TextHelpers.getComponent(provider.getActiveTargetCount()).setStyle(TextHelpers.YELLOW),
                    TextHelpers.getComponent(provider.getValidTargetCount()).setStyle(TextHelpers.YELLOW)
            ).getFormattedText());
            return;
        }

        int mouseX = gui.getMouseX() - posX();
        int mouseY = gui.getMouseY() - posY;

        for (int i = 0; i < this.elements.size(); i++) {
            ElementBase c = elements.get(i);
            if ( !c.isVisible() || !c.isEnabled() || !c.intersectsWith(mouseX, mouseY) )
                continue;

            c.addTooltip(list);
        }
    }

    @Override
    protected void drawForeground() {
        if ( icon != null )
            drawTabIcon(icon);
        if ( item != null ) {
            RenderHelper.enableGUIStandardItemLighting();
            gui.getItemRenderer().renderItemAndEffectIntoGUI(item, sideOffset(), 3);
            GlStateManager.disableLighting();
        }
        if ( !isFullyOpened() )
            return;

        FontRenderer fontRenderer = getFontRenderer();
        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".name"), sideOffset() + 20, 8, headerColor);

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".current"), sideOffset() + 6, 22, subheaderColor);
        fontRenderer.drawString(provider.formatWorkUnit(provider.getWorkLastTick()), sideOffset() + 14, 34, textColor);

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".maximum"), sideOffset() + 6, 46, subheaderColor);
        fontRenderer.drawString(provider.formatWorkUnit(provider.getWorkMaxRate()), sideOffset() + 14, 58, textColor);

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".targets.name"), sideOffset() + 6, 70, subheaderColor);
        fontRenderer.drawString(StringHelper.localizeFormat(
                INTL_KEY + ".targets.info",
                StringHelper.formatNumber(provider.getActiveTargetCount()),
                StringHelper.formatNumber(provider.getValidTargetCount())
        ), sideOffset() + 14, 82, textColor);
    }
}

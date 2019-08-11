package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.utils.Textures;
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

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");
    public static final String INTL_KEY = "info." + WirelessUtils.MODID + ".work_info";

    public static final int defaultSide = 0;
    public static final int defaultHeaderColor = 0xE1C92F;
    public static final int defaultSubHeaderColor = 0xAAAFB8;
    public static final int defaultTextColor = 0x000000;
    public static final int defaultBackgroundColor = 0x089e4c;

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
            if ( !provider.getWorkConfigured() ) {
                list.add(new TextComponentTranslation(
                        INTL_KEY + ".unconfigured.tooltip"
                ).getFormattedText());
                return;
            }

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
        if ( !provider.getWorkConfigured() )
            drawTabIcon(Textures.ERROR);
        else if ( icon != null )
            drawTabIcon(icon);
        else if ( item != null ) {
            RenderHelper.enableGUIStandardItemLighting();
            gui.getItemRenderer().renderItemAndEffectIntoGUI(item, sideOffset(), 3);
            GlStateManager.disableLighting();
        }
        if ( !isFullyOpened() )
            return;

        FontRenderer fontRenderer = getFontRenderer();

        if ( !provider.getWorkConfigured() ) {
            fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".unconfigured.title"), sideOffset() + 20, 8, headerColor);
            String explanation = provider.getWorkUnconfiguredExplanation();
            if ( explanation == null ) {
                String[] lines = TextHelpers.getLocalizedLines(INTL_KEY + ".unconfigured.explanation");
                if ( lines != null )
                    explanation = String.join("\n\n", lines);
            } else if ( StringHelper.canLocalize(explanation + ".0") ) {
                String[] lines = TextHelpers.getLocalizedLines(explanation);
                if ( lines != null )
                    explanation = String.join("\n\n", lines);
            } else if ( StringHelper.canLocalize(explanation) )
                explanation = StringHelper.localize(explanation);

            if ( explanation != null ) {
                List<String> lines = fontRenderer.listFormattedStringToWidth(explanation, maxWidth - 16);
                int length = lines.size();
                for (int i = 0; i < length; i++) {
                    fontRenderer.drawString(lines.get(i), sideOffset() + 2, 22 + (12 * i), textColor);
                }
            }

            return;
        }

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

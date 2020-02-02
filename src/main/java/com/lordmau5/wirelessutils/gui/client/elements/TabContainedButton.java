package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.RenderHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class TabContainedButton extends TabButton {

    private final IContainsButtons container;
    private boolean managedClicks;

    private String tooltip;
    private String tooltipExtra;
    private Style tooltipStyle;
    private Style tooltipExtraStyle;
    private boolean tooltipLocalized = false;
    private boolean tooltipLines = false;

    private TextureAtlasSprite icon;
    private ItemStack item = ItemStack.EMPTY;

    private int backgroundHoverColor = 0;

    private int foregroundColor = 0xFFFFFFFF;
    private int foregroundHoverColor = 0;

    public TabContainedButton(IContainsButtons container, int side) {
        super(container.getGui(), side);
        this.container = container;
        setGuiManagedClicks(false);
    }

    public TabContainedButton(IContainsButtons container, int side, TextureAtlasSprite icon) {
        super(container.getGui(), side);
        this.container = container;
        this.icon = icon;
        setGuiManagedClicks(false);
    }

    public TabContainedButton(IContainsButtons container, int side, @Nonnull ItemStack stack) {
        super(container.getGui(), side);
        this.container = container;
        this.item = stack;
        setGuiManagedClicks(false);
    }

    public TabContainedButton(IContainsButtons container, String name, int side) {
        super(container.getGui(), side);
        this.container = container;

        setName(name);
        setGuiManagedClicks(true);
    }

    public TabContainedButton(IContainsButtons container, String name, int side, TextureAtlasSprite icon) {
        super(container.getGui(), side);
        this.container = container;
        this.icon = icon;

        setName(name);
        setGuiManagedClicks(true);
    }

    public TabContainedButton(IContainsButtons container, String name, int side, @Nonnull ItemStack stack) {
        super(container.getGui(), side);
        this.container = container;
        this.item = stack;

        setName(name);
        setGuiManagedClicks(true);
    }

    public TabContainedButton setBackgroundColor(int color) {
        backgroundColor = color;
        return this;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public TabContainedButton setBackgroundHoverColor(int color) {
        backgroundHoverColor = color;
        return this;
    }

    public int getBackgroundHoverColor() {
        return backgroundHoverColor;
    }

    public TabContainedButton setForegroundColor(int color) {
        foregroundColor = color;
        return this;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public TabContainedButton setForegroundHoverColor(int color) {
        foregroundHoverColor = color;
        return this;
    }

    public int getForegroundHoverColor() {
        return foregroundHoverColor;
    }

    public TabContainedButton setIcon(TextureAtlasSprite icon) {
        this.icon = icon;
        if ( icon != null )
            item = ItemStack.EMPTY;
        return this;
    }

    public TextureAtlasSprite getIcon() {
        return icon;
    }

    public TabContainedButton setItem(@Nonnull ItemStack stack) {
        item = stack;
        if ( !stack.isEmpty() )
            icon = null;
        return this;
    }

    @Nonnull
    public ItemStack getItem() {
        return item;
    }


    public TabContainedButton setGuiManagedClicks(boolean managed) {
        managedClicks = managed;
        return this;
    }

    public TabContainedButton clearToolTip() {
        tooltip = null;
        tooltipExtra = null;
        tooltipStyle = null;
        tooltipExtraStyle = null;
        return this;
    }

    public TabContainedButton setToolTip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public TabContainedButton setToolTip(String tooltip, Style style) {
        this.tooltip = tooltip;
        this.tooltipStyle = style;
        return this;
    }

    public TabContainedButton setToolTipStyle(Style style) {
        this.tooltipStyle = style;
        return this;
    }

    public TabContainedButton setTooltipExtra(String extra) {
        tooltipExtra = extra;
        return this;
    }

    public TabContainedButton setTooltipExtra(String extra, Style style) {
        tooltipExtra = extra;
        tooltipExtraStyle = style;
        return this;
    }

    public TabContainedButton setTooltipExtraStyle(Style style) {
        tooltipExtraStyle = style;
        return this;
    }

    public TabContainedButton setToolTipLocalized(boolean localized) {
        tooltipLocalized = localized;
        return this;
    }

    public TabContainedButton setToolTipLines(String tooltip) {
        this.tooltip = tooltip;
        tooltipLines = true;
        return this;
    }

    public TabContainedButton setToolTipLines(boolean lines) {
        tooltipLines = lines;
        return this;
    }

    public TabContainedButton setActive() {
        setEnabled(true);
        return this;
    }

    public TabContainedButton setDisabled() {
        setEnabled(false);
        return this;
    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);

        if ( tooltip != null ) {
            if ( tooltipLines )
                TextHelpers.addLocalizedLines(list, tooltip, tooltipStyle);
            else if ( tooltipStyle != null ) {
                if ( !tooltipLocalized && StringHelper.canLocalize(tooltip) )
                    list.add(new TextComponentTranslation(tooltip).setStyle(tooltipStyle).getFormattedText());
                else
                    list.add(new TextComponentString(tooltip).setStyle(tooltipStyle).getFormattedText());
            } else if ( tooltipLocalized )
                list.add(tooltip);
            else
                list.add(StringHelper.localize(tooltip));
        }

        if ( tooltipExtra != null )
            TextHelpers.addLocalizedLines(list, tooltipExtra, tooltipExtraStyle);
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        if ( !managedClicks )
            return super.onMousePressed(mouseX, mouseY, mouseButton);
        if ( isEnabled() ) {
            container.handleElementButtonClick(getName(), mouseButton);
            return true;
        }

        return false;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        final int color = foregroundColor;
        if ( intersectsWith(mouseX, mouseY) && foregroundHoverColor != 0 )
            foregroundColor = foregroundHoverColor;

        super.drawForeground(mouseX, mouseY);

        foregroundColor = color;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        final int color = backgroundColor;
        if ( intersectsWith(mouseX, mouseY) && backgroundHoverColor != 0 )
            backgroundColor = backgroundHoverColor;

        super.drawBackground(mouseX, mouseY, gameTicks);

        backgroundColor = color;
    }

    @Override
    protected void drawForeground() {
        if ( foregroundColor != 0xFFFFFFFF ) {
            float cA = (foregroundColor >> 24 & 0xFF) / 255f;
            float cR = (foregroundColor >> 16 & 0xFF) / 255f;
            float cG = (foregroundColor >> 8 & 0xFF) / 255f;
            float cB = (foregroundColor & 0xFF) / 255f;

            GlStateManager.color(cR, cG, cB, cA);
        } else
            GlStateManager.color(1F, 1F, 1F, 1F);

        if ( icon != null ) {
            RenderHelper.setBlockTextureSheet();
            gui.drawColorIcon(icon, sideOffset(), 3);

        } else if ( !item.isEmpty() ) {
            RenderHelper.enableGUIStandardItemLighting();
            gui.drawItemStack(item, sideOffset(), 3, true, "");
            GlStateManager.disableLighting();
        }

        GlStateManager.color(1, 1, 1, 1);
    }
}

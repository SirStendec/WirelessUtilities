package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementButtonManaged;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class ElementDynamicButton extends ElementButtonManaged {

    private String label;
    private TextureAtlasSprite icon;

    public ElementDynamicButton(GuiContainerCore container, int posX, int posY, int sizeX, int sizeY) {
        super(container, posX, posY, sizeX, sizeY, null);
    }

    public ElementDynamicButton(GuiContainerCore container, int posX, int posY, int sizeX, int sizeY, String label) {
        this(container, posX, posY, sizeX, sizeY);
        this.label = label;
    }

    public ElementDynamicButton(GuiContainerCore container, int posX, int posY, int sizeX, int sizeY, TextureAtlasSprite icon) {
        this(container, posX, posY, sizeX, sizeY);
        this.icon = icon;
    }

    @Override
    public void setText(String text) {
        label = text;
    }

    @Override
    public String getText() {
        return label;
    }

    public void setIcon(TextureAtlasSprite icon) {
        this.icon = icon;
    }

    public TextureAtlasSprite getIcon() {
        return icon;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        int width = sizeX - 4;

        if ( icon != null )
            width -= icon.getIconWidth();

        FontRenderer fontRenderer = getFontRenderer();
        String text = null;
        if ( label != null ) {
            if ( icon != null )
                width -= 2;

            text = fontRenderer.trimStringToWidth(label, width);
            width -= fontRenderer.getStringWidth(text);
        }

        int left = 2 + (width / 2);

        if ( icon != null ) {
            gui.drawIcon(icon, posX + left, posY + (sizeY - icon.getIconHeight()) / 2);
            left += 2 + icon.getIconWidth();
        }

        if ( text != null )
            fontRenderer.drawString(text, posX + left, posY + (sizeY - 8) / 2, getTextColor(mouseX, mouseY));
    }
}

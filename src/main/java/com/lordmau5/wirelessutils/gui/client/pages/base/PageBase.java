package com.lordmau5.wirelessutils.gui.client.pages.base;

import cofh.core.gui.element.ElementBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementContainer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;

public class PageBase extends ElementContainer implements IPageTabProvider {

    private final BaseGuiContainer gui;

    public int textColor = 0x404040;
    public int backgroundColor = 0xFFFFFF;

    private String label = null;
    private TextureAtlasSprite icon = null;
    private ItemStack item = ItemStack.EMPTY;
    private boolean tabNeedsSize = false;
    private int tabWidth = 0;
    private int tabHeight = 0;

    public PageBase(BaseGuiContainer gui, int posX, int posY) {
        super(gui, posX, posY, gui.getXSize() - posX, gui.getYSize() - posY);
        this.gui = gui;
    }

    public PageBase(BaseGuiContainer gui) {
        super(gui, 0, 0, gui.getXSize(), gui.getYSize());
        this.gui = gui;
    }

    public boolean wantsSlots() {
        return false;
    }

    public void focusPage() {
        gui.setCurrentPage(this);
    }

    public boolean isPageFocused() {
        return gui.getCurrentPage() == this;
    }

    public void onPageFocused() {

    }

    public void onPageFocusLost() {

    }

    /* Tab Stuff */

    public PageBase setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    public PageBase setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public PageBase setLabel(String label) {
        this.label = label;
        tabNeedsSize = true;
        return this;
    }

    public PageBase setIcon(TextureAtlasSprite icon) {
        this.icon = icon;
        tabNeedsSize = true;
        return this;
    }

    public PageBase setItem(ItemStack item) {
        if ( item == null )
            item = ItemStack.EMPTY;

        this.item = item;
        tabNeedsSize = true;
        return this;
    }

    /* Tab Drawing */

    public void updateTabSize() {
        tabNeedsSize = false;
        tabWidth = 0;
        tabHeight = 0;

        if ( label != null && !label.isEmpty() ) {
            FontRenderer fontRenderer = getFontRenderer();
            if ( fontRenderer == null )
                tabNeedsSize = true;
            else {
                String tl = StringHelper.localize(label);
                tabWidth = fontRenderer.getStringWidth(tl);
                tabHeight = 8;
            }
        }

        if ( icon != null ) {
            tabHeight = Math.max(icon.getIconHeight(), tabHeight);
            if ( tabWidth != 0 )
                tabWidth += 2;
            tabWidth += icon.getIconWidth();
        }

        if ( !item.isEmpty() ) {
            tabHeight = Math.max(10, tabHeight);
            if ( tabWidth != 0 )
                tabWidth += 2;
            tabWidth += 16;
        }

        if ( tabHeight != 0 && tabWidth != 0 ) {
            tabWidth += 8;
            tabHeight += 8;
        }
    }

    public int getPageTabWidth() {
        if ( tabNeedsSize )
            updateTabSize();

        return tabWidth;
    }

    public int getPageTabHeight() {
        if ( tabNeedsSize )
            updateTabSize();

        return tabHeight;
    }


    public int drawPageTabBackground(int x, int y) {
        int width = getPageTabWidth();
        float colorR = (backgroundColor >> 16 & 255) / 255.0F;
        float colorG = (backgroundColor >> 8 & 255) / 255.0F;
        float colorB = (backgroundColor & 255) / 255.0F;

        GlStateManager.color(colorR, colorG, colorB, 1.0F);
        IPageTabProvider.drawBackground(gui, isPageFocused(), x, y, width, getPageTabHeight());
        GlStateManager.color(1, 1, 1, 1);
        return width;
    }

    public int drawPageTabForeground(int x, int y) {
        int width = getPageTabWidth();
        String lb = label == null ? null : StringHelper.localize(label);
        IPageTabProvider.drawForeground(gui, isPageFocused(), x, y, width, getPageTabHeight(), lb, icon, item, textColor);
        return getPageTabWidth();
    }

    /* Elements */

    public void drawElements(float partialTick, boolean foreground) {
        int mouseX = gui.getMouseX();
        int mouseY = gui.getMouseY();

        if ( foreground )
            drawForeground(mouseX, mouseY);
        else
            drawBackground(mouseX, mouseY, partialTick);
    }

    protected void updateElements() {
        int mouseX = gui.getMouseX();
        int mouseY = gui.getMouseY();

        for (ElementBase element : elements) {
            if ( element.isVisible() && element.isEnabled() )
                element.update(mouseX, mouseY);
        }
    }
}
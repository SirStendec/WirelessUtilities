package com.lordmau5.wirelessutils.gui.client.pages.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IPageTabProvider {

    ResourceLocation PAGE_TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/vaporizer.png");

    void focusPage();

    default void onPageFocusLost() {

    }

    default void onPageFocused() {

    }

    int getPageTabWidth();

    int getPageTabHeight();

    int drawPageTabBackground(int x, int y);

    int drawPageTabForeground(int x, int y);

    default boolean onPageTabMouseWheel(int mouseX, int mouseY, int wheelMovement) {
        return false;
    }

    default boolean onPageTabMousePressed(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    default void addPageTabTooltip(List<String> tooltip) {

    }

    static void drawBackground(BaseGuiContainer gui, boolean focused, int x, int y, int width, int height) {
        gui.bindTexture(PAGE_TEXTURE);
        gui.drawSizedSlicedTexturedRect(x, y - height, width, height + (focused ? 3 : 0), 176, focused ? 20 : 0, 4, 50, focused ? 23 : 20, 256, 256);
    }

    static void drawForeground(BaseGuiContainer gui, boolean focused, int x, int y, int width, int height, String label, TextureAtlasSprite icon, ItemStack item, int textColor) {
        y -= height;

        int totalWidth = 0;
        boolean hasItem = item != null && !item.isEmpty();
        FontRenderer fontRenderer = gui.getFontRenderer();
        String text = null;

        if ( icon != null )
            totalWidth += icon.getIconWidth();

        if ( hasItem ) {
            if ( totalWidth != 0 )
                totalWidth += 2;

            totalWidth += 16;
        }

        if ( label != null && !label.isEmpty() ) {
            int available = width - (totalWidth + 2);
            text = fontRenderer.trimStringToWidth(label, available);
            if ( text.isEmpty() )
                text = null;
            else {
                if ( totalWidth != 0 )
                    totalWidth += 2;
                totalWidth += fontRenderer.getStringWidth(text);
            }
        }

        int remaining = width - totalWidth;
        int left = (remaining / 2);

        if ( icon != null ) {
            gui.drawIcon(icon, x + left, y + (height - icon.getIconHeight()) / 2);
            left += 2 + icon.getIconWidth();
        }

        if ( hasItem ) {
            RenderHelper.enableGUIStandardItemLighting();
            gui.drawItemStack(item, x + left, y + (height - 16) / 2, true, "");
            GlStateManager.disableLighting();
            left += 18;
        }

        if ( text != null )
            fontRenderer.drawString(text, x + left, y + (height - 4) / 2, textColor);
    }
}

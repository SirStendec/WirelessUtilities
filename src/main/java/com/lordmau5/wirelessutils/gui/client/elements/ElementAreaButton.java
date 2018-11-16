package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementButtonManaged;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseArea;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class ElementAreaButton extends ElementButtonManaged {

    private final static ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/button_area.png");

    private TileEntityBaseArea tile;

    private boolean isActive;

    public ElementAreaButton(GuiContainerCore gui, TileEntityBaseArea tile, int posX, int posY) {
        super(gui, posX, posY, 16, 16, null);
        this.tile = tile;

        if ( !RenderManager.INSTANCE.isEnabled() ) {
            setVisible(false);
            setEnabled(false);
        }

        isActive = tile.shouldRenderAreas();
    }

    @Override
    public boolean isVisible() {
        return super.isVisible();
    }

    @Override
    public void addTooltip(List<String> list) {
        list.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + (isActive ? ".hide_area" : ".show_area")).getFormattedText());
        if ( isActive && tile.usesDefaultColor() )
            list.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + ".area_scroll").setStyle(TextHelpers.GRAY).getFormattedText());
    }

    @Override
    public void onClick() {
        tile.enableRenderAreas(!tile.shouldRenderAreas());
        isActive = tile.shouldRenderAreas();
    }

    @Override
    public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
        if ( movement == 0 )
            return false;

        tile.setDefaultColor(tile.getDefaultColor() + (movement > 0 ? 1 : -1));
        return true;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        gui.bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        gui.drawSizedTexturedModalRect(posX, posY, 0, isActive ? 16 : 0, 16, 16, 16, 32);
    }
}

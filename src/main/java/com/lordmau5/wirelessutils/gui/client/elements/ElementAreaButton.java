package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementButtonManaged;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.tile.base.IAreaVisibilityControllable;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class ElementAreaButton extends ElementButtonManaged {

    private final static ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/button_area.png");

    private final IAreaVisibilityControllable controllable;

    private boolean isActive;

    public ElementAreaButton(GuiContainerCore gui, IAreaVisibilityControllable controllable, int posX, int posY) {
        super(gui, posX, posY, 16, 16, null);
        this.controllable = controllable;

        if ( !RenderManager.INSTANCE.isEnabled() ) {
            setVisible(false);
            setEnabled(false);
        }

        isActive = controllable.shouldRenderAreas();
    }

    @Override
    public void addTooltip(List<String> list) {
        list.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + (isActive ? ".hide_area" : ".show_area")).getFormattedText());
        if ( isActive && controllable.usesDefaultColor() )
            list.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + ".area_scroll").setStyle(TextHelpers.GRAY).getFormattedText());
    }

    @Override
    public void onClick() {
        controllable.enableRenderAreas(!controllable.shouldRenderAreas());
        isActive = controllable.shouldRenderAreas();
    }

    @Override
    public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
        if ( movement == 0 )
            return false;

        controllable.setDefaultColor(controllable.getDefaultColor() + (movement > 0 ? 1 : -1));
        return true;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        gui.bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        gui.drawSizedTexturedModalRect(posX, posY, 0, isActive ? 16 : 0, 16, 16, 16, 32);
    }
}

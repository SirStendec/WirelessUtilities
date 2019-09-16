package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemTheoreticalSlaughterModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementTheoreticalSlaughterModule extends ElementModuleBase {

    private final ItemTheoreticalSlaughterModule.TheoreticalSlaughterBehavior behavior;

    private final ElementDynamicContainedButton btnExact;

    public ElementTheoreticalSlaughterModule(GuiBaseVaporizer gui, ItemTheoreticalSlaughterModule.TheoreticalSlaughterBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        btnExact = new ElementDynamicContainedButton(this, "Exact", 8, 22, 160, 16, "");
        btnExact.setVisible(behavior.canExact());
        addElement(btnExact);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        FontRenderer fontRenderer = getFontRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0);

        if ( behavior.hasEntity() ) {
            int cost = behavior.getEntityCost();
            String sCost = StringHelper.formatNumber(cost);

            fontRenderer.drawString(StringHelper.localize("btn." + WirelessUtils.MODID + ".cost"), 8, 9, 0x404040);

            if ( behavior.vaporizer.hasFluid() )
                sCost = new TextComponentTranslation(
                        "btn." + WirelessUtils.MODID + ".cost_fluid",
                        sCost,
                        StringHelper.formatNumber(cost * behavior.vaporizer.getFluidRate())
                ).getFormattedText();

            gui.drawRightAlignedText(sCost, sizeX - 7, 9, 0);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        boolean locked = isLocked();

        btnExact.setEnabled(!locked);
        btnExact.setText(StringHelper.localize("item." + WirelessUtils.MODID + ".theoretical_slaughter_module.exact" + (behavior.isExact() ? "" : ".off")));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 0.7F : 1F;

        TileBaseVaporizer vaporizer = behavior.vaporizer;
        ItemStack stack = vaporizer.getModule();

        switch (buttonName) {
            case "Exact":
                if ( ModItems.itemTheoreticalSlaughterModule.setExactCopies(stack, !behavior.isExact()).isEmpty() )
                    return;
                break;

            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
        vaporizer.setModule(stack);
        vaporizer.sendModePacket();
    }
}

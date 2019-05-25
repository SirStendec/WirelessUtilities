package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemSlaughterModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class ElementSlaughterModule extends ElementFilterableModule {

    public final ElementDynamicContainedButton btnDrops;
    public final ElementDynamicContainedButton btnExp;

    public final ItemSlaughterModule.SlaughterBehavior behavior;

    public ElementSlaughterModule(GuiBaseVaporizer gui, ItemSlaughterModule.SlaughterBehavior behavior) {
        super(gui, behavior);
        this.behavior = behavior;

        btnDrops = new ElementDynamicContainedButton(this, "Drops", 74, 8, 95, 16, "");
        btnExp = new ElementDynamicContainedButton(this, "Exp", 74, 29, 95, 16, "");

        btnExp.setVisible(behavior.vaporizer.hasFluid());

        addElement(btnDrops);
        addElement(btnExp);
    }

    int getContentHeight() {
        return 50;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0);

        gui.drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".drop.drops"), 70, 12, 0x404040);
        if ( behavior.vaporizer.hasFluid() )
            gui.drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".drop.exp"), 70, 33, 0x404040);

        GlStateManager.popMatrix();
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        btnDrops.setText(getModeLabel(behavior.getDropMode()));
        btnExp.setText(getModeLabel(behavior.getExperienceMode()));
    }

    private static String getModeLabel(int mode) {
        return StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + mode);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        TileBaseVaporizer vaporizer = behavior.vaporizer;
        ItemStack stack = vaporizer.getModule();

        int mode;

        switch (buttonName) {
            case "Drops":
                mode = ModItems.itemSlaughterModule.getDropMode(stack) + amount;
                if ( ModItems.itemSlaughterModule.setDropMode(stack, mode).isEmpty() )
                    return;

                break;

            case "Exp":
                mode = ModItems.itemSlaughterModule.getExperienceMode(stack) + amount;
                if ( ModItems.itemSlaughterModule.setExperienceMode(stack, mode).isEmpty() )
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

package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.element.ElementTextField;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemSlaughterModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class ElementSlaughterModule extends ElementModuleBase {

    public final ElementDynamicContainedButton btnDrops;
    public final ElementDynamicContainedButton btnExp;
    public final ElementDynamicContainedButton btnChild;
    public final ElementDynamicContainedButton btnBlacklist;

    public final ElementTextField txtBlacklist;

    public final ItemSlaughterModule.SlaughterBehavior behavior;

    public ElementSlaughterModule(GuiBaseVaporizer gui, ItemSlaughterModule.SlaughterBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        btnDrops = new ElementDynamicContainedButton(this, "Drops", 74, 8, 95, 16, "");
        btnExp = new ElementDynamicContainedButton(this, "Exp", 74, 29, 95, 16, "");
        btnChild = new ElementDynamicContainedButton(this, "Child", 74, 50, 95, 16, "");

        btnExp.setVisible(behavior.vaporizer.hasFluid());

        btnBlacklist = new ElementDynamicContainedButton(this, "List", 8, 50, 61, 16, "");

        txtBlacklist = new ElementTextField(gui, 9, 72, 158, 51) {
            @Override
            public ElementTextField setFocused(boolean focused) {
                if ( isFocused() && !focused ) {
                    TileBaseVaporizer vaporizer = behavior.vaporizer;
                    ItemStack module = vaporizer.getModule();

                    if ( !ModItems.itemSlaughterModule.setBlacklist(module, txtBlacklist.getText()).isEmpty() ) {
                        vaporizer.setModule(module);
                        vaporizer.sendModePacket();
                    }
                }

                return super.setFocused(focused);
            }
        };

        txtBlacklist.setMultiline(true);

        addElement(btnDrops);
        addElement(btnExp);
        addElement(btnChild);
        addElement(btnBlacklist);
        addElement(txtBlacklist);
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

        if ( !txtBlacklist.isFocused() ) {
            if ( behavior.blacklist == null || behavior.blacklist.length == 0 )
                txtBlacklist.setText("");
            else
                txtBlacklist.setText(String.join("\n", behavior.blacklist));
        }

        btnBlacklist.setText(StringHelper.localize("btn." + WirelessUtils.MODID + "." + (behavior.whitelist ? "whitelist" : "blacklist")));

        btnDrops.setText(getModeLabel(behavior.getDropMode()));
        btnExp.setText(getModeLabel(behavior.getExperienceMode()));
        btnChild.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".child_mode." + (behavior.adultsOnly ? 1 : 0)));
    }

    private static String getModeLabel(int mode) {
        return StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + mode);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {

        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        TileBaseVaporizer vaporizer = gui.getVaporizer();
        ItemStack module = vaporizer.getModule();

        int mode;

        switch (buttonName) {
            case "List":
                if ( ModItems.itemSlaughterModule.setWhitelist(module, !behavior.whitelist).isEmpty() )
                    return;

                break;

            case "Child":
                if ( ModItems.itemSlaughterModule.setAdultsOnly(module, !behavior.adultsOnly).isEmpty() )
                    return;

                break;

            case "Drops":
                mode = ModItems.itemSlaughterModule.getDropMode(module) + amount;
                if ( ModItems.itemSlaughterModule.setDropMode(module, mode).isEmpty() )
                    return;

                break;

            case "Exp":
                mode = ModItems.itemSlaughterModule.getExperienceMode(module) + amount;
                if ( ModItems.itemSlaughterModule.setExperienceMode(module, mode).isEmpty() )
                    return;

                break;

            default:
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
        vaporizer.setModule(module);
        vaporizer.sendModePacket();
    }
}

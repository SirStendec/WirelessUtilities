package com.lordmau5.wirelessutils.plugins.IndustrialForegoing.gui;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementFilterableModule;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.plugins.IndustrialForegoing.IFPlugin;
import com.lordmau5.wirelessutils.plugins.IndustrialForegoing.items.ItemAnimalSlaughterModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementAnimalSlaughterModule extends ElementFilterableModule {

    public final ElementDynamicContainedButton btnMeatMode;

    public final ItemAnimalSlaughterModule.AnimalSlaughterBehavior behavior;

    public ElementAnimalSlaughterModule(GuiBaseVaporizer gui, ItemAnimalSlaughterModule.AnimalSlaughterBehavior behavior) {
        super(gui, behavior);
        this.behavior = behavior;

        btnMeatMode = new ElementDynamicContainedButton(this, "MeatMode", 8, 8, 161, 16, "");
        addElement(btnMeatMode);
    }

    public int getContentHeight() {
        return 29;
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        btnMeatMode.setEnabled(!isLocked());
        btnMeatMode.setText(new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".animal_slaughter_module.producing",
                TextHelpers.getComponent(behavior.getSpecialFluid())
        ).getFormattedText());
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;

        TileBaseVaporizer vaporizer = behavior.vaporizer;
        ItemStack stack = vaporizer.getModule();

        switch (buttonName) {
            case "MeatMode":
                if ( IFPlugin.itemAnimalSlaughterModule.setMeatMode(stack, !behavior.getMeatMode()).isEmpty() )
                    return;
                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
        }

        BaseGuiContainer.playClickSound(pitch);
        vaporizer.setModule(stack);
        vaporizer.sendModePacket();
    }
}

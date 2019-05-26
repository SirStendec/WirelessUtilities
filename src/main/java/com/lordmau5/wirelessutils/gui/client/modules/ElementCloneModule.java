package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemCloneModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.item.ItemStack;

public class ElementCloneModule extends ElementModuleBase {

    private final ItemCloneModule.CloneBehavior behavior;

    private final ElementDynamicContainedButton btnExact;

    public ElementCloneModule(GuiBaseVaporizer gui, ItemCloneModule.CloneBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        btnExact = new ElementDynamicContainedButton(this, "Exact", 8, 8, 160, 16, "");
        btnExact.setVisible(behavior.canExact());
        addElement(btnExact);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        btnExact.setText(StringHelper.localize("item." + WirelessUtils.MODID + ".clone_module.exact" +
                (behavior.isExact() ? "" : ".off")
        ));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        if ( buttonName.equals("Exact") ) {
            TileBaseVaporizer vaporizer = behavior.vaporizer;
            ItemStack stack = vaporizer.getModule();

            if ( ModItems.itemCloneModule.setExactCopies(stack, !ModItems.itemCloneModule.getExactCopies(stack)).isEmpty() )
                return;

            BaseGuiContainer.playClickSound(1F);
            vaporizer.setModule(stack);
            vaporizer.sendModePacket();
            return;
        }

        super.handleElementButtonClick(buttonName, mouseButton);
    }
}

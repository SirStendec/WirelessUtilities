package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemCloneModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementCloneModule extends ElementModuleBase {

    private final ItemCloneModule.CloneBehavior behavior;

    private final ElementDynamicContainedButton btnExact;

    public ElementCloneModule(GuiBaseVaporizer gui, ItemCloneModule.CloneBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        btnExact = new ElementDynamicContainedButton(this, "Exact", 8, 22, 160, 16, "");
        btnExact.setVisible(behavior.canExact());
        addElement(btnExact);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        if ( !behavior.hasEntity() )
            return;

        int cost = behavior.getCost();
        String sCost = StringHelper.formatNumber(cost);

        FontRenderer render = getFontRenderer();
        render.drawString(StringHelper.localize("btn." + WirelessUtils.MODID + ".cost"), 8, posY + 9, 0x404040);

        if ( gui.getVaporizer().hasFluid() )
            sCost = new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".cost_fluid",
                    sCost,
                    StringHelper.formatNumber(cost * ModConfig.vaporizers.mbPerPoint)
            ).getFormattedText();

        gui.drawRightAlignedText(sCost, sizeX - 7, posY + 9, 0);
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

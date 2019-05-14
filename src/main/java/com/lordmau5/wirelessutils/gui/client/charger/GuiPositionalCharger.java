package com.lordmau5.wirelessutils.gui.client.charger;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.init.CoreTextures;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiPositional;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModeButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.gui.client.elements.TabChargerConfiguration;
import com.lordmau5.wirelessutils.gui.client.elements.TabRoundRobin;
import com.lordmau5.wirelessutils.gui.client.elements.TabSideControl;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorkInfo;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorldTickRate;
import com.lordmau5.wirelessutils.gui.container.charger.ContainerPositionalCharger;
import com.lordmau5.wirelessutils.tile.charger.TileEntityPositionalCharger;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiPositionalCharger extends BaseGuiPositional {

    private final TileEntityPositionalCharger charger;
    private TabSideControl sideControl;

    public GuiPositionalCharger(InventoryPlayer playerInventory, TileEntityPositionalCharger charger) {
        super(new ContainerPositionalCharger(playerInventory, charger), charger, new ResourceLocation(WirelessUtils.MODID, "textures/gui/positional_machine.png"));
        this.charger = charger;

        generateInfo("tab." + WirelessUtils.MODID + ".positional_charger");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 17, 23, charger.getEnergyStorage()).setInfinite(charger.isCreative()));
        addElement(new ElementAreaButton(this, charger, 152, 74));
        addElement(new ElementModeButton(this, charger, 134, 74));

        addTab(new TabWorkInfo(this, charger).setIcon(CoreTextures.ICON_ENERGY));
        addTab(new TabChargerConfiguration(this, charger));
        addTab(new TabWorldTickRate(this, charger));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, charger));
        addTab(new TabRoundRobin(this, charger));

        sideControl = new TabSideControl(this, charger);
        addTab(sideControl);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String range = StringHelper.formatNumber(charger.getRange());
        if ( charger.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 80, 40, 0x404040);
        fontRenderer.drawString(range, 84, 40, 0);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        sideControl.setVisible(charger.isSidedTransferAugmented());
    }
}

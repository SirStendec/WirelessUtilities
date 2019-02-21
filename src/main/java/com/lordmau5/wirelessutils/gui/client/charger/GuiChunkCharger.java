package com.lordmau5.wirelessutils.gui.client.charger;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.gui.client.elements.TabChargerConfiguration;
import com.lordmau5.wirelessutils.gui.container.charger.ContainerChunkCharger;
import com.lordmau5.wirelessutils.tile.charger.TileEntityChunkCharger;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiChunkCharger extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    private final TileEntityChunkCharger charger;

    public GuiChunkCharger(InventoryPlayer inventoryPlayer, TileEntityChunkCharger charger) {
        super(new ContainerChunkCharger(inventoryPlayer, charger), charger, TEXTURE);
        this.charger = charger;

        generateInfo("tab." + WirelessUtils.MODID + ".chunk_charger");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 17, 10, charger.getEnergyStorage()).setInfinite(charger.isCreative()));
        addElement(new ElementAreaButton(this, charger, 152, 59));

        addTab(new TabChargerConfiguration(this, charger));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, charger));
    }
}

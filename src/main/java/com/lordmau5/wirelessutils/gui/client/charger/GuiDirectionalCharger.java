package com.lordmau5.wirelessutils.gui.client.charger;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.init.CoreTextures;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.SharedState;
import com.lordmau5.wirelessutils.gui.client.elements.*;
import com.lordmau5.wirelessutils.gui.container.charger.ContainerDirectionalCharger;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import com.lordmau5.wirelessutils.utils.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiDirectionalCharger extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    private final TileEntityDirectionalCharger charger;

    private ElementDynamicContainedButton btnMode;
    private ElementRangeControls rangeControls;
    private ElementOffsetControls offsetControls;

    public GuiDirectionalCharger(InventoryPlayer playerInventory, TileEntityDirectionalCharger charger) {
        super(new ContainerDirectionalCharger(playerInventory, charger), charger, TEXTURE);
        this.charger = charger;

        generateInfo("tab." + WirelessUtils.MODID + ".directional_charger");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 17, 20, charger.getEnergyStorage()).setInfinite(charger.isCreative()));
        addElement(new ElementAreaButton(this, charger, 152, 69));

        addTab(new TabWorkInfo(this, charger).setIcon(CoreTextures.ICON_ENERGY));
        addTab(new TabChargerConfiguration(this, charger));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, charger));
        addTab(new TabRoundRobin(this, charger));

        btnMode = new ElementDynamicContainedButton(this, "Mode", 134, 69, 16, 16, Textures.SIZE);
        addElement(btnMode);

        rangeControls = new ElementRangeControls(this, charger, 138, 18);
        addElement(rangeControls);

        offsetControls = new ElementOffsetControls(this, charger, 138, 18);
        addElement(offsetControls);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        switch (buttonName) {
            case "Mode":
                SharedState.offsetMode = !SharedState.offsetMode;
                break;
            default:
                return;
        }

        playClickSound(1F);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        boolean offsetMode = SharedState.offsetMode;

        btnMode.setIcon(offsetMode ? Textures.OFFSET : Textures.SIZE);
        btnMode.setToolTip("btn." + WirelessUtils.MODID + ".mode." + (offsetMode ? "offset" : "range"));
        btnMode.setVisible(charger.getRange() > 0);

        rangeControls.setVisible(!offsetMode);
        offsetControls.setVisible(offsetMode);
    }
}

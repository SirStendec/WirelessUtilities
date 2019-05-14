package com.lordmau5.wirelessutils.gui.client.charger;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.init.CoreTextures;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.SharedState;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModeButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementOffsetControls;
import com.lordmau5.wirelessutils.gui.client.elements.ElementRangeControls;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.gui.client.elements.TabChargerConfiguration;
import com.lordmau5.wirelessutils.gui.client.elements.TabRoundRobin;
import com.lordmau5.wirelessutils.gui.client.elements.TabSideControl;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorkInfo;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorldTickRate;
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

    private TabSideControl sideControl;

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
        addElement(new ElementModeButton(this, charger, 134, 69));

        addTab(new TabWorkInfo(this, charger).setIcon(CoreTextures.ICON_ENERGY));
        addTab(new TabChargerConfiguration(this, charger));
        addTab(new TabWorldTickRate(this, charger));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, charger));
        addTab(new TabRoundRobin(this, charger));

        sideControl = new TabSideControl(this, charger);
        addTab(sideControl);

        btnMode = new ElementDynamicContainedButton(this, "Mode", 116, 69, 16, 16, Textures.SIZE);
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

        sideControl.setVisible(charger.isSidedTransferAugmented());

        rangeControls.setVisible(!offsetMode);
        offsetControls.setVisible(offsetMode);
    }
}

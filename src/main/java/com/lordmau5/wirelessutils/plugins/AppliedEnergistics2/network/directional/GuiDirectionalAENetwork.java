package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.SharedState;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementOffsetControls;
import com.lordmau5.wirelessutils.gui.client.elements.ElementRangeControls;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.utils.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiDirectionalAENetwork extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    private final TileDirectionalAENetwork tile;

    private ElementDynamicContainedButton btnMode;
    private ElementRangeControls rangeControls;
    private ElementOffsetControls offsetControls;

    public GuiDirectionalAENetwork(InventoryPlayer playerInventory, TileDirectionalAENetwork tile) {
        super(new ContainerDirectionalAENetwork(playerInventory, tile), tile, TEXTURE);
        this.tile = tile;

        generateInfo("tab." + WirelessUtils.MODID + ".directional_ae_network");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementAreaButton(this, tile, 152, 69));

        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, tile));

        btnMode = new ElementDynamicContainedButton(this, "Mode", 134, 69, 16, 16, Textures.SIZE);
        addElement(btnMode);

        rangeControls = new ElementRangeControls(this, tile, 138, 18);
        addElement(rangeControls);

        offsetControls = new ElementOffsetControls(this, tile, 138, 18);
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
        btnMode.setVisible(tile.getRange() > 0);

        rangeControls.setVisible(!offsetMode);
        offsetControls.setVisible(offsetMode);
    }
}

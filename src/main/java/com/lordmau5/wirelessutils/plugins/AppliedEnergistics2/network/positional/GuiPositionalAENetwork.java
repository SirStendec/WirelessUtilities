package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiPositional;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base.ElementColorButton;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiPositionalAENetwork extends BaseGuiPositional {

    private final TilePositionalAENetwork tile;

    public GuiPositionalAENetwork(InventoryPlayer playerInventory, TilePositionalAENetwork tile) {
        super(new ContainerPositionalAENetwork(playerInventory, tile), tile, new ResourceLocation(WirelessUtils.MODID, "textures/gui/positional_machine.png"));
        this.tile = tile;

        generateInfo("tab." + WirelessUtils.MODID + ".positional_ae_network");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementAreaButton(this, tile, 152, 74));

        if ( ModConfig.plugins.appliedEnergistics.enableColor )
            addElement(new ElementColorButton(this, tile, 134, 74));

        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String range = StringHelper.formatNumber(tile.getRange());
        if ( tile.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 80, 40, 0x404040);
        fontRenderer.drawString(range, 84, 40, 0);
    }
}

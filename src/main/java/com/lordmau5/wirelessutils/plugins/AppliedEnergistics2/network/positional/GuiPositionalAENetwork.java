package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional;

import appeng.api.util.AEColor;
import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiPositional;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.utils.Textures;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GuiPositionalAENetwork extends BaseGuiPositional {

    private final TilePositionalAENetwork tile;
    private ElementDynamicContainedButton btnColor;

    public GuiPositionalAENetwork(InventoryPlayer playerInventory, TilePositionalAENetwork tile) {
        super(new ContainerPositionalAENetwork(playerInventory, tile), tile, new ResourceLocation(WirelessUtils.MODID, "textures/gui/positional_machine.png"));
        this.tile = tile;

        generateInfo("tab." + WirelessUtils.MODID + ".positional_ae_network");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementAreaButton(this, tile, 152, 74));

        btnColor = new ElementDynamicContainedButton(this, "Color", 132, 74, 16, 16, Textures.COLOR);
        if ( ModConfig.plugins.appliedEnergistics.enableColor )
            addElement(btnColor);

        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, tile));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "Color":
                tile.setAEColor(tile.getAEColor().ordinal() + amount);
                tile.sendModePacket();
                break;
            default:
                return;
        }

        playClickSound(pitch);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        AEColor color = tile.getAEColor();
        btnColor.setForegroundColor(0xFF000000 | color.getVariantByTintIndex(AEColor.TINTINDEX_MEDIUM));
        btnColor.setToolTip(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + ".ae_color",
                TextHelpers.getComponent(color.toString()).setStyle(TextHelpers.WHITE)
        ).setStyle(TextHelpers.GRAY).getFormattedText());
        btnColor.setToolTipExtra("btn." + WirelessUtils.MODID + ".ae_color." + (ModConfig.plugins.appliedEnergistics.colorsWireless ? "no_sides" : "sides"));
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

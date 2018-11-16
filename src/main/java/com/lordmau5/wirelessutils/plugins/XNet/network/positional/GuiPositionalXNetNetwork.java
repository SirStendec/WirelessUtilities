package com.lordmau5.wirelessutils.plugins.XNet.network.positional;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiPositionalXNetNetwork extends BaseGuiContainer {
    private final TilePositionalXNetNetwork te;

    public GuiPositionalXNetNetwork(InventoryPlayer playerInventory, TilePositionalXNetNetwork te) {
        super(new ContainerPositionalXNetNetwork(playerInventory, te), te, new ResourceLocation(WirelessUtils.MODID, "textures/gui/positional_machine.png"));

        generateInfo("tab." + WirelessUtils.MODID + ".positional_xnet_network");

        this.te = te;
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementAreaButton(this, te, 152, 64));

        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, te));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String range = StringHelper.formatNumber(te.getRange());
        if ( te.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 80, 30, 0x404040);
        fontRenderer.drawString(range, 84, 30, 0x404040);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        int xPos = guiLeft + 116;
        int yPos = guiTop + 8;

        int slotIndex = 0;

        boolean hasInvalidStack = false;

        ItemStack held = mc.player.inventory.getItemStack();
        if ( !held.isEmpty() ) {
            if ( te.isPositionalCardValid(held) && !te.isTargetInRange(BlockPosDimension.fromTag(held.getTagCompound())) )
                hasInvalidStack = true;
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++, slotIndex++) {
                int color = (0x99 << 24) + (NiceColors.COLORS[slotIndex % NiceColors.COLORS.length] & 0xFFFFFF);
                if ( hasInvalidStack || !te.isSlotUnlocked(slotIndex) )
                    color = 0x99444444;

                drawRect(xPos + (x * 18), yPos + (y * 18), xPos + 16 + (x * 18), yPos + 16 + (y * 18), color);
            }
        }
    }
}

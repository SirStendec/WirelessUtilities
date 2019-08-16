package com.lordmau5.wirelessutils.gui.client.desublimator;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.inventory.ComparableItemStackValidatedNBT;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiPositional;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementLockControls;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModeButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementWorkBudget;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.gui.client.elements.TabEnergyHistory;
import com.lordmau5.wirelessutils.gui.client.elements.TabRoundRobin;
import com.lordmau5.wirelessutils.gui.client.elements.TabSideControl;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorkInfo;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorldTickRate;
import com.lordmau5.wirelessutils.gui.container.desublimator.ContainerPositionalDesublimator;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class GuiPositionalDesublimator extends BaseGuiPositional {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/positional_desublimator.png");
    public static final ItemStack CHEST = new ItemStack(Blocks.CHEST);

    private final TilePositionalDesublimator desublimator;
    private TabSideControl sideControl;

    public GuiPositionalDesublimator(InventoryPlayer player, TilePositionalDesublimator desublimator) {
        super(new ContainerPositionalDesublimator(player, desublimator), desublimator, TEXTURE);
        this.desublimator = desublimator;

        ySize = 222;
        generateInfo("tab." + WirelessUtils.MODID + ".positional_desublimator");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 17, 23, desublimator.getEnergyStorage()).setInfinite(desublimator.isCreative()));

        if ( desublimator.hasSustainedRate() )
            addElement(new ElementWorkBudget(this, 33, 23, desublimator));

        addElement(new ElementAreaButton(this, desublimator, 152, 72));
        addElement(new ElementLockControls(this, desublimator, 98, 72));
        addElement(new ElementModeButton(this, desublimator, 134, 72));

        addTab(new TabEnergyHistory(this, desublimator, false));
        addTab(new TabWorkInfo(this, desublimator).setItem(CHEST));
        addTab(new TabWorldTickRate(this, desublimator));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, desublimator));
        addTab(new TabRoundRobin(this, desublimator));
        sideControl = (TabSideControl) addTab(new TabSideControl(this, desublimator));
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        sideControl.setVisible(desublimator.isSidedTransferAugmented());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        int xPos = guiLeft + 8;
        int yPos = guiTop + 91;

        int slotIndex = desublimator.getBufferOffset();
        ItemStackHandler handler = desublimator.getInventory();

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 9; x++, slotIndex++) {
                int xp = xPos + (x * 18);
                int yp = yPos + (y * 18);

                if ( desublimator.isSlotUnlocked(slotIndex) ) {
                    if ( handler.getStackInSlot(slotIndex).isEmpty() ) {
                        ComparableItemStackValidatedNBT lock = desublimator.getLock(slotIndex);
                        if ( lock != null )
                            drawGhostItem(lock.toItemStack(), xp, yp, true, true, null);
                    }

                } else
                    drawRect(xp, yp, xp + 16, yp + 16, 0x99444444);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".buffer"), 8, 80, 0x404040);

        String range = StringHelper.formatNumber(desublimator.getRange());
        if ( desublimator.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 90, 40, 0x404040);
        fontRenderer.drawString(range, 94, 40, 0);
    }
}

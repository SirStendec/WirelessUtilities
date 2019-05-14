package com.lordmau5.wirelessutils.gui.client.desublimator;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabEnergy;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.inventory.ComparableItemStackValidatedNBT;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.SharedState;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementLockControls;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModeButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementOffsetControls;
import com.lordmau5.wirelessutils.gui.client.elements.ElementRangeControls;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.gui.client.elements.TabRoundRobin;
import com.lordmau5.wirelessutils.gui.client.elements.TabSideControl;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorkInfo;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorldTickRate;
import com.lordmau5.wirelessutils.gui.container.desublimator.ContainerDirectionalDesublimator;
import com.lordmau5.wirelessutils.tile.desublimator.TileDirectionalDesublimator;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiDirectionalDesublimator extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_desublimator.png");
    public static final ItemStack CHEST = new ItemStack(Blocks.CHEST);

    private final TileDirectionalDesublimator desublimator;

    private ElementDynamicContainedButton btnMode;
    private ElementRangeControls rangeControls;
    private ElementOffsetControls offsetControls;

    private TabSideControl sideControl;

    public GuiDirectionalDesublimator(InventoryPlayer player, TileDirectionalDesublimator desublimator) {
        super(new ContainerDirectionalDesublimator(player, desublimator), desublimator, TEXTURE);
        this.desublimator = desublimator;

        ySize = 222;
        generateInfo("tab." + WirelessUtils.MODID + ".directional_desublimator");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 17, 20, desublimator.getEnergyStorage()).setInfinite(desublimator.isCreative()));
        addElement(new ElementAreaButton(this, desublimator, 152, 69));
        addElement(new ElementLockControls(this, desublimator, 80, 69));
        addElement(new ElementModeButton(this, desublimator, 134, 69));

        addTab(new TabEnergy(this, desublimator, false));
        addTab(new TabWorkInfo(this, desublimator).setItem(CHEST));
        addTab(new TabWorldTickRate(this, desublimator));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, desublimator));
        addTab(new TabRoundRobin(this, desublimator));
        sideControl = (TabSideControl) addTab(new TabSideControl(this, desublimator));

        btnMode = new ElementDynamicContainedButton(this, "Mode", 116, 69, 16, 16, Textures.SIZE);
        addElement(btnMode);

        rangeControls = new ElementRangeControls(this, desublimator, 138, 18);
        addElement(rangeControls);

        offsetControls = new ElementOffsetControls(this, desublimator, 138, 18);
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
        btnMode.setVisible(desublimator.getRange() > 0);

        sideControl.setVisible(desublimator.isSidedTransferAugmented());

        rangeControls.setVisible(!offsetMode);
        offsetControls.setVisible(offsetMode);
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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".buffer"), 8, 80, 0x404040);

    }
}

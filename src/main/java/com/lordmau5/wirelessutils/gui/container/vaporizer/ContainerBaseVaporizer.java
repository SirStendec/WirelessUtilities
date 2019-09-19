package com.lordmau5.wirelessutils.gui.container.vaporizer;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.gui.slot.SlotVisible;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerBaseVaporizer extends BaseContainerTile {

    protected final TileBaseVaporizer vaporizer;
    private final IItemHandler itemHandler;

    public final int moduleOffset;
    public final int inputOffset;
    public final int outputOffset;

    public ContainerBaseVaporizer(InventoryPlayer inventory, TileBaseVaporizer vaporizer) {
        super(inventory, vaporizer, true, true);

        this.vaporizer = vaporizer;
        itemHandler = vaporizer.getInventory();

        inputOffset = inventorySlots.size();
        addBufferSlots(vaporizer.getInputOffset(), 8);

        outputOffset = inventorySlots.size();
        addBufferSlots(vaporizer.getOutputOffset(), 98);

        moduleOffset = inventorySlots.size();
        addModuleSlots();
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {
        return 160;
    }

    public void addModuleSlots() {
        int slotIndex = vaporizer.getModuleOffset();

        int xPos = 12;
        int yPos = 8;

        for (int x = 0; x < 2; x++, slotIndex++)
            addSlotToContainer(new SlotUnlockableItemHandler(vaporizer, itemHandler, slotIndex, xPos + (x * 18), yPos));
    }

    public void addBufferSlots(int slotIndex, int xPos) {
        int yPos = 111;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 4; x++, slotIndex++)
                addSlotToContainer(new SlotUnlockableItemHandler(vaporizer, itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
        }
    }

    @Override
    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int xOffset = getPlayerInventoryHorizontalOffset();
        int yOffset = getPlayerInventoryVerticalOffset();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotVisible(inventoryPlayer, x + y * 9 + 9, xOffset + x * 18, yOffset + y * 18));
            }
        }

        for (int x = 0; x < 9; x++)
            addSlotToContainer(new SlotVisible(inventoryPlayer, x, xOffset + x * 18, yOffset + 58));
    }
}

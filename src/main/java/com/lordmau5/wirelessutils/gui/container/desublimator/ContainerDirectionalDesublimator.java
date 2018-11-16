package com.lordmau5.wirelessutils.gui.container.desublimator;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.tile.desublimator.TileDirectionalDesublimator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerDirectionalDesublimator extends BaseContainerTile {

    private final TileBaseDesublimator desublimator;
    private IItemHandler itemHandler;

    public ContainerDirectionalDesublimator(InventoryPlayer inventory, TileDirectionalDesublimator desublimator) {
        super(inventory, desublimator, true, true);

        this.desublimator = desublimator;
        itemHandler = desublimator.getInventory();
        addBufferSlots();
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {
        return 140;
    }

    public void addBufferSlots() {
        int slotIndex = desublimator.getBufferOffset();

        int xPos = 8;
        int yPos = 91;

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 9; x++, slotIndex++) {
                addSlotToContainer(new SlotUnlockableItemHandler(desublimator, itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
            }
        }
    }
}

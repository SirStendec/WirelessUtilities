package com.lordmau5.wirelessutils.gui.container.desublimator;

import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerPositionalDesublimator extends BaseContainerPositional {

    private final TileBaseDesublimator desublimator;
    private final IItemHandler itemHandler;

    public ContainerPositionalDesublimator(InventoryPlayer player, TilePositionalDesublimator desublimator) {
        super(player, desublimator);

        this.desublimator = desublimator;
        itemHandler = desublimator.getInventory();
        addBufferSlots();
        addOwnSlots();
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

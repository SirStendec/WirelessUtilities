package com.lordmau5.wirelessutils.plugins.XNet.network.positional;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerPositionalXNetNetwork extends BaseContainerTile {

    private final TilePositionalXNetNetwork tile;
    private final IItemHandler itemHandler;

    public ContainerPositionalXNetNetwork(InventoryPlayer playerInventory, TilePositionalXNetNetwork te) {
        super(playerInventory, te);
        tile = te;

        this.itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        addOwnSlots();
    }

    private void addOwnSlots() {
        int slotIndex = 0;

        int xPos = 116;
        int yPos = 8;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++, slotIndex++) {
                addSlotToContainer(new SlotUnlockableItemHandler(
                        tile, this.itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
            }
        }

    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

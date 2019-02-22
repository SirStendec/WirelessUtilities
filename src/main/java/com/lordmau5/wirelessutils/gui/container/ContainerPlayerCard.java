package com.lordmau5.wirelessutils.gui.container;

import cofh.core.gui.container.ContainerCore;
import com.lordmau5.wirelessutils.gui.slot.SlotDontTakeHeld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPlayerCard extends ContainerCore {

    public ContainerPlayerCard(InventoryPlayer inventory) {
        bindPlayerInventory(inventory);
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    protected int getPlayerInventoryVerticalOffset() {
        return 94;
    }

    @Override
    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int xOffset = getPlayerInventoryHorizontalOffset();
        int yOffset = getPlayerInventoryVerticalOffset();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new SlotDontTakeHeld(inventoryPlayer, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotDontTakeHeld(inventoryPlayer, i, xOffset + i * 18, yOffset + 58));
        }
    }
}

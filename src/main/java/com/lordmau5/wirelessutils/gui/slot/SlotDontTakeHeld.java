package com.lordmau5.wirelessutils.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotDontTakeHeld extends Slot {

    public SlotDontTakeHeld(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        if ( playerIn.inventory.currentItem == getSlotIndex() )
            return false;

        return super.canTakeStack(playerIn);
    }
}

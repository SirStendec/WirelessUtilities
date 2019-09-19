package com.lordmau5.wirelessutils.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotVisible extends Slot implements IVisibleSlot {

    private boolean visible = true;

    public SlotVisible(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isHere(IInventory inv, int slotIn) {
        if ( !visible )
            return false;

        return super.isHere(inv, slotIn);
    }

    @Override
    public boolean isEnabled() {
        if ( !visible )
            return false;

        return super.isEnabled();
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        if ( !visible )
            return false;

        return super.canTakeStack(playerIn);
    }
}

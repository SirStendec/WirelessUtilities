package com.lordmau5.wirelessutils.gui.slot;

import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotUnlockableItemHandler extends SlotItemHandler {

    private final IUnlockableSlots tile;
    private boolean visible = true;

    public SlotUnlockableItemHandler(IUnlockableSlots tile, IItemHandler handler, int slotIndex, int x, int y) {
        super(handler, slotIndex, x, y);
        this.tile = tile;
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
    public int getSlotStackLimit() {
        return tile.getStackLimit(getSlotIndex());
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return tile.getStackLimit(getSlotIndex(), stack);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return tile.isSlotUnlocked(getSlotIndex()) && super.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return tile.isSlotUnlocked(getSlotIndex()) && super.canTakeStack(playerIn);
    }
}

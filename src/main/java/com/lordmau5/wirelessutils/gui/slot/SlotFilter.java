package com.lordmau5.wirelessutils.gui.slot;

import cofh.core.gui.slot.SlotFalseCopy;
import com.lordmau5.wirelessutils.gui.container.IFilterHost;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotFilter extends SlotFalseCopy {

    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IFilterHost host;
    private final int index;

    public SlotFilter(IFilterHost host, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.host = host;
        this.index = index;
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        return host.getStackInSlot(index);
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        if ( !isItemValid(stack) || host.isSlotLocked(index) )
            return;

        if ( !stack.isEmpty() )
            stack.setCount(1);

        host.setStackInSlot(index, stack);
        onSlotChanged();
    }

    @Override
    public void onSlotChanged() {
        host.onSlotChanged(index);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if ( host.isSlotLocked(index) )
            return false;

        return host.isItemValid(index, stack);
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isHere(IInventory inv, int slotIn) {
        return false;
    }
}

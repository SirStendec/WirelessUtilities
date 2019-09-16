package com.lordmau5.wirelessutils.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemHandlerProxy implements IItemHandler {

    public final ItemStackHandler handler;
    public final int offset;
    public final int slots;

    public boolean allowInsert = false;
    public boolean allowExtract = false;

    public ItemHandlerProxy(ItemStackHandler handler, int offset, int slots) {
        this.handler = handler;
        this.offset = offset;
        this.slots = slots;
    }

    public ItemHandlerProxy(ItemStackHandler handler, int offset, int slots, boolean allowInsert, boolean allowExtract) {
        this.handler = handler;
        this.offset = offset;
        this.slots = slots;
        this.allowExtract = allowExtract;
        this.allowInsert = allowInsert;
    }

    public int getSlots() {
        if ( handler == null )
            return 0;

        return slots;
    }

    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if ( handler == null || slot < 0 || slot >= slots )
            return ItemStack.EMPTY;

        return handler.getStackInSlot(slot + offset);
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if ( handler == null || slot < 0 || slot >= slots )
            throw new RuntimeException("invalid slot");

        handler.setStackInSlot(slot + offset, stack);
    }

    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if ( !allowInsert || handler == null || slot < 0 || slot >= slots )
            return stack;

        return handler.insertItem(slot + offset, stack, simulate);
    }

    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if ( !allowExtract || handler == null || slot < 0 || slot >= slots )
            return ItemStack.EMPTY;

        return handler.extractItem(slot + offset, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        if ( handler == null || slot < 0 || slot >= slots )
            return 0;

        return handler.getSlotLimit(slot + offset);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if ( handler == null || slot < 0 || slot >= slots )
            return false;

        return handler.isItemValid(slot + offset, stack);
    }
}

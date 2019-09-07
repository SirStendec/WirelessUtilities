package com.lordmau5.wirelessutils.gui.container.items;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.gui.container.IFilterHost;
import com.lordmau5.wirelessutils.gui.slot.SlotFilter;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class ContainerFilterAugment extends BaseContainerItem implements IFilterHost {

    private ItemStack[] list;

    public ContainerFilterAugment(@Nonnull ItemStack augment, int slot, InventoryPlayer inventory) {
        super(augment, slot, inventory);

        list = new ItemStack[getSlots()];
        Arrays.fill(list, ItemStack.EMPTY);

        ItemStack[] existing = getList();
        if ( existing != null ) {
            System.arraycopy(existing, 0, list, 0, existing.length);
        }
    }

    @Override
    protected void bindOwnSlots() {
        super.bindOwnSlots();

        int slots = getSlots();
        int slotIndex = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 6; x++, slotIndex++) {
                if ( slotIndex < slots )
                    addSlotToContainer(new SlotFilter(this, slotIndex, 34 + x * 18, 22 + y * 18));
            }
        }
    }

    @Override
    public int getSlots() {
        return ModItems.itemFilterAugment.getAvailableSlots(stack);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return list[slot];
    }

    @Override
    public boolean setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if ( isLocked() )
            return false;

        if ( stack == null )
            stack = ItemStack.EMPTY;

        list[slot] = stack;
        return true;
    }

    @Override
    public boolean isSlotLocked(int slot) {
        return isLocked();
    }

    @Override
    public void onSlotChanged(int slot) {
        if ( isLocked() )
            return;

        setList(list);
    }

    public boolean canMatchMod() {
        return ModItems.itemFilterAugment.canMatchMod(stack);
    }

    public boolean getMatchMod() {
        return ModItems.itemFilterAugment.getMatchMod(stack);
    }

    public boolean setMatchMod(boolean match) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setMatchMod(stack, match);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean canIgnoreMetadata() {
        return ModItems.itemFilterAugment.canIgnoreMetadata(stack);
    }

    public boolean getIgnoreMetadata() {
        return ModItems.itemFilterAugment.getIgnoreMetadata(stack);
    }

    public boolean setIgnoreMetadata(boolean ignore) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setIgnoreMetadata(stack, ignore);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean canIgnoreNBT() {
        return ModItems.itemFilterAugment.canIgnoreNBT(stack);
    }

    public boolean getIgnoreNBT() {
        return ModItems.itemFilterAugment.getIgnoreNBT(stack);
    }

    public boolean setIgnoreNBT(boolean ignore) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setIgnoreNBT(stack, ignore);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean canWhitelist() {
        return ModItems.itemFilterAugment.canWhitelist(stack);
    }

    public boolean isWhitelist() {
        return ModItems.itemFilterAugment.isWhitelist(stack);
    }

    public boolean setWhitelist(boolean whitelist) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setWhitelist(stack, whitelist);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean canVoid() {
        return ModItems.itemFilterAugment.canVoid(stack);
    }

    public boolean isVoiding() {
        return ModItems.itemFilterAugment.isVoiding(stack);
    }

    public boolean setVoiding(boolean voiding) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setVoiding(stack, voiding);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean canOreDict() {
        return ModItems.itemFilterAugment.canOreDict(stack);
    }

    public boolean getUseOreDict() {
        return ModItems.itemFilterAugment.getUseOreDict(stack);
    }

    public boolean setUseOreDict(boolean use) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setUseOreDict(stack, use);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    @Nullable
    public ItemStack[] getList() {
        return ModItems.itemFilterAugment.getList(stack, true);
    }

    public boolean setList(ItemStack[] list) {
        if ( isLocked() )
            return false;

        ItemStack augment = ModItems.itemFilterAugment.setList(stack, list);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    @Override
    protected boolean supportsShiftClick(int slotIndex) {
        return true;
    }
}

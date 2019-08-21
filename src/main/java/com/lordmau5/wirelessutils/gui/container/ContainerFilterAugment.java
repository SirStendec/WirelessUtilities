package com.lordmau5.wirelessutils.gui.container;

import com.lordmau5.wirelessutils.gui.slot.SlotFilter;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ContainerFilterAugment extends ContainerItem implements IFilterHost {

    private ItemStack augment;
    private EntityPlayer player;
    private int slot;

    private ItemStack[] list;
    private boolean suppressUpdates = false;

    public ContainerFilterAugment(@Nonnull ItemStack augment, InventoryPlayer inventory) {
        this(augment, inventory.currentItem, inventory);

        list = new ItemStack[18];
        Arrays.fill(list, ItemStack.EMPTY);

        ItemStack[] existing = getList();
        if ( existing != null ) {
            System.arraycopy(existing, 0, list, 0, existing.length);
        }
    }

    public ContainerFilterAugment(@Nonnull ItemStack augment, int slot, InventoryPlayer inventory) {
        super(inventory);
        this.player = inventory.player;
        this.augment = augment;
        this.slot = slot;
    }

    @Override
    protected void bindOwnSlots() {
        super.bindOwnSlots();

        int slotIndex = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 6; x++, slotIndex++) {
                addSlotToContainer(new SlotFilter(this, slotIndex, 12 + x * 18, 22 + y * 18));
            }
        }
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return list[slot];
    }

    @Override
    public boolean setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if ( stack == null )
            stack = ItemStack.EMPTY;

        list[slot] = stack;
        return true;
    }

    @Override
    public void onSlotChanged(int slot) {
        setList(list);
        //sendUpdate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setAll(List<ItemStack> stacks) {
        suppressUpdates = true;
        super.setAll(stacks);
        suppressUpdates = false;
    }

    public ItemStack getAugment() {
        return augment;
    }

    public void setAugment(@Nonnull ItemStack stack) {
        augment = stack;
    }

    public void sendUpdate() {
        if ( !suppressUpdates )
            PacketUpdateItem.updateItem(player, slot, augment);
    }

    public boolean getMatchMod() {
        return ModItems.itemFilterAugment.getMatchMod(augment);
    }

    public boolean setMatchMod(boolean match) {
        ItemStack augment = ModItems.itemFilterAugment.setMatchMod(this.augment, match);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    public boolean getIgnoreMetadata() {
        return ModItems.itemFilterAugment.getIgnoreMetadata(augment);
    }

    public boolean setIgnoreMetadata(boolean ignore) {
        ItemStack augment = ModItems.itemFilterAugment.setIgnoreMetadata(this.augment, ignore);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    public boolean getIgnoreNBT() {
        return ModItems.itemFilterAugment.getIgnoreNBT(augment);
    }

    public boolean setIgnoreNBT(boolean ignore) {
        ItemStack augment = ModItems.itemFilterAugment.setIgnoreNBT(this.augment, ignore);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    public boolean isWhitelist() {
        return ModItems.itemFilterAugment.isWhitelist(augment);
    }

    public boolean setWhitelist(boolean whitelist) {
        ItemStack augment = ModItems.itemFilterAugment.setWhitelist(this.augment, whitelist);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    public boolean isVoiding() {
        return ModItems.itemFilterAugment.isVoiding(augment);
    }

    public boolean setVoiding(boolean voiding) {
        ItemStack augment = ModItems.itemFilterAugment.setVoiding(this.augment, voiding);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    public boolean getUseOreDict() {
        return ModItems.itemFilterAugment.getUseOreDict(augment);
    }

    public boolean setUseOreDict(boolean use) {
        ItemStack augment = ModItems.itemFilterAugment.setUseOreDict(this.augment, use);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    @Nullable
    public ItemStack[] getList() {
        return ModItems.itemFilterAugment.getList(augment, true);
    }

    public boolean setList(ItemStack[] list) {
        ItemStack augment = ModItems.itemFilterAugment.setList(this.augment, list);
        if ( augment.isEmpty() )
            return false;

        setAugment(augment);
        return true;
    }

    @Override
    protected boolean supportsShiftClick(int slotIndex) {
        return false;
    }
}

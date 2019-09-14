package com.lordmau5.wirelessutils.gui.container;

import cofh.core.gui.container.ContainerTileAugmentable;
import com.lordmau5.wirelessutils.gui.slot.IVisibleSlot;
import com.lordmau5.wirelessutils.gui.slot.SlotAugmentLockable;
import com.lordmau5.wirelessutils.gui.slot.SlotVisible;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.tile.base.IAugmentableTwoElectricBoogaloo;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class BaseContainerTile extends ContainerTileAugmentable implements IVisibleSlotContainer {

    protected List<IVisibleSlot> visibleSlots;
    private InventoryPlayer inventory;

    protected BaseContainerTile() {
        super();
    }

    public BaseContainerTile(TileEntity tile) {
        super(tile);
    }

    protected BaseContainerTile(InventoryPlayer inventory, TileEntity tile) {
        this(inventory, tile, true, true);
    }

    protected BaseContainerTile(InventoryPlayer inventory, TileEntity tile, boolean augSlots, boolean playerInvSlots) {
        super(tile);

        this.inventory = inventory;

        hasAugSlots = augSlots;
        hasPlayerInvSlots = playerInvSlots;

        if ( hasAugSlots )
            addAugmentSlots();

        if ( hasPlayerInvSlots )
            bindPlayerInventory(inventory);
    }

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        if ( slot instanceof IVisibleSlot ) {
            if ( visibleSlots == null )
                visibleSlots = new ArrayList<>();

            visibleSlots.add((IVisibleSlot) slot);
        }
        return super.addSlotToContainer(slot);
    }

    public void hideSlots() {
        setSlotsVisible(false);
    }

    public void showSlots() {
        setSlotsVisible(true);
    }

    public void setSlotsVisible(boolean visible) {
        if ( visibleSlots != null )
            for (IVisibleSlot slot : visibleSlots)
                slot.setVisible(visible);
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {
        return super.getPlayerInventoryVerticalOffset() + 10;
    }

    @Override
    protected void addAugmentSlots() {
        if ( baseTile instanceof IAugmentableTwoElectricBoogaloo ) {
            augmentSlots = new Slot[((IAugmentableTwoElectricBoogaloo) baseTile).getAugmentSlots().length];
            for (int i = 0; i < augmentSlots.length; i++)
                augmentSlots[i] = addSlotToContainer(new SlotAugmentLockable((IAugmentableTwoElectricBoogaloo) baseTile, inventory, null, i, 0, 0));

            return;
        }

        super.addAugmentSlots();
    }

    @Override
    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int xOffset = getPlayerInventoryHorizontalOffset();
        int yOffset = getPlayerInventoryVerticalOffset();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new SlotVisible(inventoryPlayer, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotVisible(inventoryPlayer, i, xOffset + i * 18, yOffset + 58));
        }
    }

    @Override
    protected boolean performMerge(int slotIndex, ItemStack stack) {
        int invAugment = augmentSlots.length;
        int invPlayer = invAugment + 27;
        int invFull = invPlayer + 9;
        int invTile = invFull + (baseTile == null ? 0 : baseTile.getInvSlotCount());

        if ( slotIndex < invAugment ) {
            return mergeItemStack(stack, invAugment, invFull, true);
        } else if ( slotIndex < invFull ) {
            Item item = stack.getItem();
            if ( !augmentLock && invAugment > 0 && item instanceof ItemAugment )
                return mergeItemStack(stack, 0, invAugment, false);

            return mergeItemStack(stack, invFull, invTile, false);
        }

        return mergeItemStack(stack, invAugment, invFull, true);
    }
}

package com.lordmau5.wirelessutils.gui.container;

import cofh.core.gui.container.ContainerTileAugmentable;
import com.lordmau5.wirelessutils.gui.slot.SlotAugmentLockable;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.tile.base.IAugmentableTwoElectricBoogaloo;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class BaseContainerTile extends ContainerTileAugmentable {

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

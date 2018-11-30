package com.lordmau5.wirelessutils.gui.slot;

import cofh.api.core.IAugmentable;
import cofh.core.gui.slot.SlotAugment;
import com.lordmau5.wirelessutils.tile.base.IAugmentableTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotAugmentLockable extends SlotAugment {

    private final IAugmentableTwoElectricBoogaloo tile;
    private final InventoryPlayer inventoryPlayer;

    public SlotAugmentLockable(IAugmentable tile, InventoryPlayer inventoryPlayer, IInventory inventory, int slotIndex, int x, int y) {
        super(tile, inventory, slotIndex, x, y);

        if ( tile instanceof IAugmentableTwoElectricBoogaloo )
            this.tile = (IAugmentableTwoElectricBoogaloo) tile;
        else
            this.tile = null;

        this.inventoryPlayer = inventoryPlayer;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        if ( !this.getHasStack() )
            return;

        if ( inventoryPlayer != null && inventoryPlayer.player instanceof EntityPlayerMP ) {
            ModAdvancements.AUGMENTED.trigger((EntityPlayerMP) inventoryPlayer.player);
        }
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if ( tile == null )
            return super.isItemValid(stack);

        return tile.isValidAugment(getSlotIndex(), stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        if ( tile == null )
            return super.canTakeStack(playerIn);

        return tile.canRemoveAugment(playerIn, getSlotIndex(), getStack(), ItemStack.EMPTY);
    }
}

package com.lordmau5.wirelessutils.gui.container.desublimator;

import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerPositionalDesublimator extends BaseContainerPositional {

    private final TilePositionalDesublimator desublimator;
    private final IItemHandler itemHandler;

    public ContainerPositionalDesublimator(InventoryPlayer player, TilePositionalDesublimator desublimator) {
        super(player, desublimator);

        this.desublimator = desublimator;
        itemHandler = desublimator.getInventory();
        addBufferSlots();
        addOwnSlots();
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {
        return 140;
    }

    public void addBufferSlots() {
        int slotIndex = desublimator.getBufferOffset();

        int xPos = 8;
        int yPos = 91;

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 9; x++, slotIndex++) {
                addSlotToContainer(new SlotUnlockableItemHandler(desublimator, itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
            }
        }
    }

    @Override
    protected boolean performMerge(int slotIndex, ItemStack stack) {
        int invAugment = augmentSlots.length;
        int invPlayer = invAugment + 27;
        int invFull = invPlayer + 9;
        int bufferOffset = desublimator.getBufferOffset();
        int invCards = invFull + desublimator.getInvSlotCount();
        int invTile = invCards - bufferOffset;

        if ( slotIndex < invAugment ) {
            return mergeItemStack(stack, invAugment, invFull, true);
        } else if ( slotIndex < invFull ) {
            Item item = stack.getItem();

            if ( !augmentLock && invAugment > 0 && item instanceof ItemAugment )
                return mergeItemStack(stack, 0, invAugment, false);

            if ( item instanceof ItemBasePositionalCard )
                return mergeItemStack(stack, invTile, invCards, false);

            return mergeItemStack(stack, invFull, invTile, false);
        }

        return mergeItemStack(stack, invAugment, invFull, true);
    }
}

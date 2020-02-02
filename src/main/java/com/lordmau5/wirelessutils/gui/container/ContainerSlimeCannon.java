package com.lordmau5.wirelessutils.gui.container;

import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.tile.TileSlimeCannon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.items.IItemHandler;

public class ContainerSlimeCannon extends BaseContainer {

    private final TileSlimeCannon tile;
    private final IItemHandler itemHandler;

    public ContainerSlimeCannon(TileSlimeCannon tile) {
        this(null, tile);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if ( tile == null )
            return;

        for (IContainerListener listener : listeners)
            tile.sendGuiNetworkData(this, listener);
    }

    public TileSlimeCannon getTile() {
        return tile;
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {
        return 94;
    }

    @Override
    protected int getSizeInventory() {
        if ( itemHandler != null )
            return itemHandler.getSlots();

        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    public ContainerSlimeCannon(InventoryPlayer player, TileSlimeCannon tile) {
        this.tile = tile;
        if ( tile != null )
            itemHandler = tile.getInventory();
        else
            itemHandler = null;

        bindOwnSlots();
        bindPlayerInventory(player);
    }

    protected void bindOwnSlots() {
        if ( itemHandler == null )
            return;

        int slots = itemHandler.getSlots();
        int xPos = Math.floorDiv(176 - (slots * 18), 2);

        for (int i = 0; i < slots; i++) {
            addSlotToContainer(new SlotUnlockableItemHandler(tile, itemHandler, i, xPos + (i * 18), 18));
        }
    }
}

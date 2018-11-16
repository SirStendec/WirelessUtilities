package com.lordmau5.wirelessutils.gui.container;

import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class BaseContainerPositional extends BaseContainerTile {

    private final TileEntityBaseMachine machine;
    private IItemHandler itemHandler;

    public BaseContainerPositional(InventoryPlayer player, TileEntityBaseMachine machine) {
        super(player, machine);

        this.machine = machine;
        itemHandler = machine.getInventory();
    }

    public int getPositionalSlotOffset() {
        return 0;
    }

    public void addOwnSlots() {
        if ( !(machine instanceof IUnlockableSlots) )
            return;

        IUnlockableSlots unlockable = (IUnlockableSlots) machine;

        int slotIndex = getPositionalSlotOffset();

        int xPos = 116;
        int yPos = 18;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++, slotIndex++) {
                addSlotToContainer(new SlotUnlockableItemHandler(
                        unlockable, this.itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
            }
        }
    }

}

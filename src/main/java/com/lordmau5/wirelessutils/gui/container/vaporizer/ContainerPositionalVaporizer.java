package com.lordmau5.wirelessutils.gui.container.vaporizer;

import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerPositionalVaporizer extends ContainerBaseVaporizer {

    private final IItemHandler itemHandler;

    public ContainerPositionalVaporizer(InventoryPlayer inventory, TileBaseVaporizer vaporizer) {
        super(inventory, vaporizer);
        itemHandler = vaporizer.getInventory();

        addTargetSlots();
    }

    public int getPositionalSlotOffset() {
        return 0;
    }

    protected void addTargetSlots() {
        int slotIndex = getPositionalSlotOffset();

        int xPos = 116;
        int yPos = 38;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++, slotIndex++) {
                addSlotToContainer(new SlotUnlockableItemHandler(vaporizer, itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
            }
        }
    }
}

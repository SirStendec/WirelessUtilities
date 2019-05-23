package com.lordmau5.wirelessutils.gui.container.vaporizer;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.gui.slot.SlotUnlockableItemHandler;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.tile.vaporizer.TileDirectionalVaporizer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

public class ContainerDirectionalVaporizer extends BaseContainerTile {

    private final TileBaseVaporizer vaporizer;
    private final IItemHandler itemHandler;

    public ContainerDirectionalVaporizer(InventoryPlayer inventory, TileDirectionalVaporizer vaporizer) {
        super(inventory, vaporizer, true, true);

        this.vaporizer = vaporizer;
        itemHandler = vaporizer.getInventory();

        addModuleSlots();
        addBufferSlots(vaporizer.getInputOffset(), 8);
        addBufferSlots(vaporizer.getOutputOffset(), 98);
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {
        return 160;
    }

    public void addModuleSlots() {
        int slotIndex = vaporizer.getModuleOffset();

        int xPos = 8;
        int yPos = 8;

        for (int x = 0; x < 2; x++, slotIndex++)
            addSlotToContainer(new SlotUnlockableItemHandler(vaporizer, itemHandler, slotIndex, xPos + (x * 18), yPos));
    }

    public void addBufferSlots(int slotIndex, int xPos) {
        int yPos = 111;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 4; x++, slotIndex++)
                addSlotToContainer(new SlotUnlockableItemHandler(vaporizer, itemHandler, slotIndex, xPos + (x * 18), yPos + (y * 18)));
        }
    }
}

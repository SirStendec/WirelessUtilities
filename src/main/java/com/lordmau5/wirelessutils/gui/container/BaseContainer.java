package com.lordmau5.wirelessutils.gui.container;

import cofh.core.gui.container.ContainerCore;
import com.lordmau5.wirelessutils.gui.slot.IVisibleSlot;
import com.lordmau5.wirelessutils.gui.slot.SlotVisible;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseContainer extends ContainerCore {

    protected List<IVisibleSlot> visibleSlots;

    public BaseContainer() {
        super();
    }

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
}

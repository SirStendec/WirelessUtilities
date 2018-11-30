package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.tab.TabAugment;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TabAugmentTwoElectricBoogaloo extends TabAugment {

    private final IAugmentableContainer container;
    private final int numAugments;
    private int slotsBorderX1 = 18;
    private int slotsBorderY1 = 20;

    public TabAugmentTwoElectricBoogaloo(GuiContainerCore gui, IAugmentableContainer container) {
        super(gui, container);
        this.container = container;

        numAugments = container.getAugmentSlots().length;
        updateSlotSize();
    }

    public TabAugmentTwoElectricBoogaloo(GuiContainerCore gui, int side, IAugmentableContainer container) {
        super(gui, side, container);
        this.container = container;
        numAugments = container.getAugmentSlots().length;
        updateSlotSize();
    }

    private void updateSlotSize() {
        slotsBorderX1 = 18;
        slotsBorderY1 = 20;

        switch (numAugments) {
            case 4:
                slotsBorderX1 += 9;
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                break;
            default:
                slotsBorderX1 += 9 * (3 - numAugments);
                slotsBorderY1 += 9;
        }
    }

    @Override
    protected void drawForeground() {
        super.drawForeground();

        if ( !isFullyOpened() || numAugments == 0 )
            return;

        switch (numAugments) {
            case 4:
                drawSlotLocks(0, 0, 2, 0);
                drawSlotLocks(0, 1, 2, 2);
                break;
            case 5:
                drawSlotLocks(0, 0, 3, 0);
                drawSlotLocks(1, 1, 2, 3);
                break;
            case 6:
                drawSlotLocks(0, 0, 3, 0);
                drawSlotLocks(0, 1, 3, 3);
                break;
            case 7:
                drawSlotLocks(1, 0, 2, 0);
                drawSlotLocks(0, 1, 3, 2);
                drawSlotLocks(1, 2, 2, 5);
                break;
            case 8:
                drawSlotLocks(0, 0, 3, 0);
                drawSlotLocks(0, 1, 3, 3);
                drawSlotLocks(1, 2, 2, 6);
                break;
            case 9:
                drawSlotLocks(0, 0, 3, 0);
                drawSlotLocks(0, 1, 3, 3);
                drawSlotLocks(0, 2, 3, 6);
                break;
            default:
                drawSlotLocks(0, 0, numAugments, 0);
        }
    }

    private void drawSlotLocks(int xOffset, int yOffset, int slots, int firstSlot) {
        int xPos = sideOffset() + slotsBorderX1 + 4 + 9 * xOffset;
        int yPos = slotsBorderY1 + 4 + 18 * yOffset;

        ItemStack held = getContainerScreen().mc.player.inventory.getItemStack();

        Slot[] augments = container.getAugmentSlots();
        for (int i = 0; i < slots; i++) {
            Slot slot = augments[i + firstSlot];
            if ( slot != null ) {
                int x = xPos + (i * 18);
                if ( !slot.canTakeStack(getContainerScreen().mc.player) ) {
                    GuiContainerCore.drawRect(x, yPos, x + 16, yPos + 16, 0x90600000);

                } else if ( !held.isEmpty() && !slot.isItemValid(held) ) {
                    GuiContainerCore.drawRect(x, yPos, x + 16, yPos + 16, 0x99222222);
                }
            }
        }
    }
}
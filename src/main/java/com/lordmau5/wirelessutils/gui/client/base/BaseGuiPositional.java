package com.lordmau5.wirelessutils.gui.client.base;

import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BaseGuiPositional extends BaseGuiContainer {

    private final TileEntityBaseMachine machine;

    public BaseGuiPositional(Container container, TileEntityBaseMachine machine) {
        super(container, machine);
        this.machine = machine;
    }

    protected BaseGuiPositional(Container container, TileEntityBaseMachine machine, ResourceLocation texture) {
        super(container, machine, texture);
        this.machine = machine;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        if ( !(machine instanceof IPositionalMachine) )
            return;

        IPositionalMachine pos = (IPositionalMachine) machine;
        IUnlockableSlots slots = machine instanceof IUnlockableSlots ? (IUnlockableSlots) machine : null;

        int xPos = guiLeft + 116;
        int yPos = guiTop + 18;

        int slotIndex = 0;
        if ( inventorySlots instanceof BaseContainerPositional )
            slotIndex = ((BaseContainerPositional) inventorySlots).getPositionalSlotOffset();

        boolean hasInvalidStack = false;

        ItemStack held = mc.player.inventory.getItemStack();
        if ( !held.isEmpty() && pos.isPositionalCardValid(held) ) {
            ItemBasePositionalCard card = (ItemBasePositionalCard) held.getItem();
            if ( card != null && !card.shouldIgnoreDistance(held) ) {
                BlockPosDimension target = card.getTarget(held, pos.getPosition());
                if ( target == null || !pos.isTargetInRange(target) )
                    hasInvalidStack = true;
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++, slotIndex++) {
                int color = (0x99 << 24) + (NiceColors.COLORS[slotIndex % NiceColors.COLORS.length] & 0xFFFFFF);
                if ( hasInvalidStack || (slots != null && !slots.isSlotUnlocked(slotIndex)) )
                    color = 0x99444444;

                drawRect(xPos + (x * 18), yPos + (y * 18), xPos + 16 + (x * 18), yPos + 16 + (y * 18), color);
            }
        }
    }
}

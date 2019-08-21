package com.lordmau5.wirelessutils.gui.container.items;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerPlayerCard extends BaseContainerItem {

    public ContainerPlayerCard(@Nonnull ItemStack stack, @Nonnull InventoryPlayer inventory) {
        this(stack, inventory.currentItem, inventory);
    }

    public ContainerPlayerCard(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);
    }

    @Nullable
    public String getPlayerName() {
        return ModItems.itemPlayerPositionalCard.getPlayerName(stack);
    }
}

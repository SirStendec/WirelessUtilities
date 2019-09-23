package com.lordmau5.wirelessutils.gui.container.items;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerFacingAugment extends BaseContainerItem {

    private final EntityPlayer player;

    public ContainerFacingAugment(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);
        player = inventory.player;
    }

    public boolean allowNullFacing() {
        if ( ModConfig.augments.facing.allowNull )
            return true;

        return player.isCreative();
    }

    @Nullable
    public EnumFacing getFacing() {
        return ModItems.itemFacingAugment.getFacing(stack);
    }

    public boolean setFacing(@Nullable EnumFacing facing) {
        ItemStack augment = ModItems.itemFacingAugment.setFacing(stack, facing);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }
}

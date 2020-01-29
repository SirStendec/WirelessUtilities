package com.lordmau5.wirelessutils.plugins.RefinedStorage.augment;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.utils.BusTransferMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerRSBusAugment extends BaseContainerItem {

    private final EntityPlayer player;

    public ContainerRSBusAugment(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);
        player = inventory.player;
    }

    public byte getMinTickRate() {
        return RefinedStoragePlugin.itemRSBusAugment.getMinTickRate(stack);
    }

    public byte getTickRate() {
        return RefinedStoragePlugin.itemRSBusAugment.getTickRate(stack);
    }

    public boolean setTickRate(byte rate) {
        if ( rate < 0 )
            rate = 0;

        ItemStack augment = RefinedStoragePlugin.itemRSBusAugment.setTickRate(stack, rate);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }


    public BusTransferMode getEnergyMode() {
        return RefinedStoragePlugin.itemRSBusAugment.getEnergyMode(stack);
    }

    public BusTransferMode getItemsMode() {
        return RefinedStoragePlugin.itemRSBusAugment.getItemsMode(stack);
    }

    public BusTransferMode getFluidMode() {
        return RefinedStoragePlugin.itemRSBusAugment.getFluidMode(stack);
    }

    public boolean setEnergyMode(@Nullable BusTransferMode mode) {
        ItemStack augment = RefinedStoragePlugin.itemRSBusAugment.setEnergyMode(stack, mode);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean setItemsMode(@Nullable BusTransferMode mode) {
        ItemStack augment = RefinedStoragePlugin.itemRSBusAugment.setItemsMode(stack, mode);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean setFluidMode(@Nullable BusTransferMode mode) {
        ItemStack augment = RefinedStoragePlugin.itemRSBusAugment.setFluidMode(stack, mode);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }
}

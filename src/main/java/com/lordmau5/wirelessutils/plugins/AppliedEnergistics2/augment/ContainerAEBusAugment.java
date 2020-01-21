package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.augment;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.utils.BusTransferMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerAEBusAugment extends BaseContainerItem {

    private final EntityPlayer player;

    public ContainerAEBusAugment(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);
        player = inventory.player;
    }


    public byte getMinTickRate() {
        return AppliedEnergistics2Plugin.itemAEBusAugment.getMinTickRate(stack);
    }

    public byte getTickRate() {
        return AppliedEnergistics2Plugin.itemAEBusAugment.getTickRate(stack);
    }

    public boolean setTickRate(byte rate) {
        if ( rate < 0 )
            rate = 0;

        ItemStack augment = AppliedEnergistics2Plugin.itemAEBusAugment.setTickRate(stack, rate);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }


    public BusTransferMode getEnergyMode() {
        return AppliedEnergistics2Plugin.itemAEBusAugment.getEnergyMode(stack);
    }

    public BusTransferMode getItemsMode() {
        return AppliedEnergistics2Plugin.itemAEBusAugment.getItemsMode(stack);
    }

    public BusTransferMode getFluidMode() {
        return AppliedEnergistics2Plugin.itemAEBusAugment.getFluidMode(stack);
    }

    public boolean setEnergyMode(@Nullable BusTransferMode mode) {
        ItemStack augment = AppliedEnergistics2Plugin.itemAEBusAugment.setEnergyMode(stack, mode);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean setItemsMode(@Nullable BusTransferMode mode) {
        ItemStack augment = AppliedEnergistics2Plugin.itemAEBusAugment.setItemsMode(stack, mode);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }

    public boolean setFluidMode(@Nullable BusTransferMode mode) {
        ItemStack augment = AppliedEnergistics2Plugin.itemAEBusAugment.setFluidMode(stack, mode);
        if ( augment.isEmpty() )
            return false;

        setItemStack(augment);
        return true;
    }
}

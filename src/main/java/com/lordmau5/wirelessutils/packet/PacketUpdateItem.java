package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.item.base.IAdminEditableItem;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class PacketUpdateItem extends BasePacket {

    public static void initialize() {
        PacketHandler.INSTANCE.registerPacket(PacketUpdateItem.class);
    }

    public PacketUpdateItem() {

    }

    public static PacketUpdateItem getUpdatePacket(boolean admin, int slot, @Nonnull ItemStack stack) {
        PacketUpdateItem payload = new PacketUpdateItem();

        payload.addBool(admin);
        payload.addByte(slot);
        payload.addItemStack(stack);

        return payload;
    }

    public static void updateItem(EntityPlayer player, boolean admin, int slot, @Nonnull ItemStack stack) {
        PacketHandler.sendToServer(getUpdatePacket(admin, slot, stack));
    }

    public void handlePacket(EntityPlayer player, boolean isServer) {
        if ( player == null || !isServer )
            return;

        final boolean isAdmin = getBool();
        final int slot = getByte();
        final ItemStack stack = player.inventory.getStackInSlot(slot);
        final Item item = stack.getItem();
        if ( stack.isEmpty() )
            return;

        if ( isAdmin && item instanceof IAdminEditableItem )
            ((IAdminEditableItem) item).handleAdminPacket(stack.copy(), player, slot, getItemStack(), this);

        else if ( !isAdmin && item instanceof IUpdateableItem )
            ((IUpdateableItem) item).handleUpdatePacket(stack.copy(), player, slot, getItemStack(), this);
    }
}

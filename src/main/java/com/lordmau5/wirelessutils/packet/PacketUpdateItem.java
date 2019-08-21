package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class PacketUpdateItem extends PacketBase {

    public static void initialize() {
        PacketHandler.INSTANCE.registerPacket(PacketUpdateItem.class);
    }

    public PacketUpdateItem() {

    }

    public static void updateItem(EntityPlayer player, int slot, @Nonnull ItemStack stack) {
        PacketHandler.sendToServer(new PacketUpdateItem()
                .addByte(slot)
                .addItemStack(stack)
        );
    }

    public void handlePacket(EntityPlayer player, boolean isServer) {
        if ( player == null || !isServer )
            return;

        int slot = getByte();
        ItemStack stack = player.inventory.getStackInSlot(slot);

        Item item = stack.getItem();
        if ( stack.isEmpty() || !(item instanceof IUpdateableItem) )
            return;

        ((IUpdateableItem) item).handleUpdatePacket(stack, player, slot, getItemStack(), this);
    }
}

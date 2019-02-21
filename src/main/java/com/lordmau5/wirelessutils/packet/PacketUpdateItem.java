package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PacketUpdateItem extends PacketBase {

    public static void initialize() {
        PacketHandler.INSTANCE.registerPacket(PacketUpdateItem.class);
    }

    public PacketUpdateItem() {

    }

    public void handlePacket(EntityPlayer player, boolean isServer) {
        if ( player == null )
            return;

        int slot = getInt();
        ItemStack stack = player.inventory.getStackInSlot(slot);

        Item item = stack.getItem();
        if ( stack.isEmpty() || !(item instanceof IUpdateableItem) )
            return;

        ((IUpdateableItem) item).handleUpdatePacket(stack, player, slot, isServer, this);
    }
}

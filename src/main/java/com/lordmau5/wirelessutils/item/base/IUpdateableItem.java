package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IUpdateableItem {

    void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, boolean isServer, @Nonnull PacketUpdateItem packet);

}

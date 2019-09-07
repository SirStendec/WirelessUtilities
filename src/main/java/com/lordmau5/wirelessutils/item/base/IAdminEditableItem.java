package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.NotNull;

import javax.annotation.Nonnull;

public interface IAdminEditableItem {

    default void openAdminGui(@Nonnull EntityPlayer player, int slot) {
        player.openGui(WirelessUtils.instance, WirelessUtils.GUI_ADMIN_ITEM, player.getEntityWorld(), 0, slot, 0);
    }

    default void openAdminGui(@Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        player.openGui(WirelessUtils.instance, WirelessUtils.GUI_ADMIN_ITEM, player.getEntityWorld(), hand.ordinal(), 0, 0);
    }

    void handleAdminPacket(@Nonnull ItemStack stack, EntityPlayer player, int slot, @NotNull ItemStack newStack, @Nonnull PacketUpdateItem packet);

    Object getClientAdminGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world);

    Object getServerAdminGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world);

}

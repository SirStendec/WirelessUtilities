package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public interface IGuiItem {

    Object getClientGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world);

    Object getServerGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world);

    default void openGui(@Nonnull EntityPlayer player, int slot) {
        player.openGui(WirelessUtils.instance, WirelessUtils.GUI_ITEM, player.getEntityWorld(), 0, slot, 0);
    }

    default void openGui(@Nonnull EntityPlayer player, EnumHand hand) {
        player.openGui(WirelessUtils.instance, WirelessUtils.GUI_ITEM, player.getEntityWorld(), hand.ordinal(), 0, 0);
    }

}

package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.GuiFilterAugment;
import com.lordmau5.wirelessutils.gui.client.GuiPlayerCard;
import com.lordmau5.wirelessutils.gui.container.items.ContainerFilterAugment;
import com.lordmau5.wirelessutils.gui.container.items.ContainerPlayerCard;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class GuiHandler extends cofh.core.gui.GuiHandler {

    @Nonnull
    private static ItemStack getHeldItem(EntityPlayer player, int handIndex) {
        EnumHand hand = EnumHand.values()[handIndex];
        return player.getHeldItem(hand);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_PLAYER_CARD ) {
            ItemStack stack = getHeldItem(player, x);
            if ( stack.isEmpty() || stack.getItem() != ModItems.itemPlayerPositionalCard )
                return null;

            return new GuiPlayerCard(new ContainerPlayerCard(stack, player.inventory));
        }

        if ( id == WirelessUtils.GUI_FILTER_AUGMENT ) {
            ItemStack stack = getHeldItem(player, x);
            if ( stack.isEmpty() || stack.getItem() != ModItems.itemFilterAugment )
                return null;

            return new GuiFilterAugment(new ContainerFilterAugment(stack, player.inventory));
        }

        return super.getClientGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_PLAYER_CARD ) {
            ItemStack stack = getHeldItem(player, x);
            if ( stack.isEmpty() || stack.getItem() != ModItems.itemPlayerPositionalCard )
                return null;

            return new ContainerPlayerCard(stack, player.inventory);
        }

        if ( id == WirelessUtils.GUI_FILTER_AUGMENT ) {
            ItemStack stack = getHeldItem(player, x);
            if ( stack.isEmpty() || stack.getItem() != ModItems.itemFilterAugment )
                return null;

            return new ContainerFilterAugment(stack, player.inventory);
        }

        return super.getServerGuiElement(id, player, world, x, y, z);
    }
}

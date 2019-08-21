package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.GuiFilterAugment;
import com.lordmau5.wirelessutils.gui.client.GuiPlayerCard;
import com.lordmau5.wirelessutils.gui.container.ContainerFilterAugment;
import com.lordmau5.wirelessutils.gui.container.ContainerItem;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GuiHandler extends cofh.core.gui.GuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_PLAYER_CARD ) {
            return new GuiPlayerCard(new ContainerItem(player.inventory));
        }

        if ( id == WirelessUtils.GUI_FILTER_AUGMENT ) {
            EnumHand hand = EnumHand.values()[x];
            ItemStack stack = player.getHeldItem(hand);
            if ( stack.isEmpty() || stack.getItem() != ModItems.itemFilterAugment )
                return null;

            return new GuiFilterAugment(new ContainerFilterAugment(stack, player.inventory));
        }

        return super.getClientGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_PLAYER_CARD ) {
            return new ContainerItem(player.inventory);
        }

        if ( id == WirelessUtils.GUI_FILTER_AUGMENT ) {
            EnumHand hand = EnumHand.values()[x];
            ItemStack stack = player.getHeldItem(hand);
            if ( stack.isEmpty() || stack.getItem() != ModItems.itemFilterAugment )
                return null;

            return new ContainerFilterAugment(stack, player.inventory);
        }

        return super.getServerGuiElement(id, player, world, x, y, z);
    }
}

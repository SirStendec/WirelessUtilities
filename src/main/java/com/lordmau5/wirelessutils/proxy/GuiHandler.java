package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.IAdminEditableItem;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GuiHandler extends cofh.core.gui.GuiHandler {

    private final EnumHand[] HANDS = EnumHand.values();

    private int getInventorySlot(EntityPlayer player, EnumHand hand) {
        if ( hand == EnumHand.MAIN_HAND )
            return player.inventory.currentItem;

        else if ( hand == EnumHand.OFF_HAND )
            return player.inventory.mainInventory.size() + player.inventory.armorInventory.size();

        else
            return 0;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_ITEM ) {
            EnumHand hand = HANDS[x % HANDS.length];
            int slot = getInventorySlot(player, hand);
            if ( y != 0 )
                slot = y;

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if ( stack.isEmpty() )
                return null;

            Item item = stack.getItem();
            if ( item instanceof IGuiItem )
                return ((IGuiItem) item).getClientGuiElement(stack, slot, player, world);

            else if ( item instanceof ItemBlock ) {
                Block block = ((ItemBlock) item).getBlock();
                if ( block instanceof IGuiItem )
                    return ((IGuiItem) block).getClientGuiElement(stack, slot, player, world);
            }

            return null;
        }

        if ( id == WirelessUtils.GUI_ADMIN_ITEM ) {
            EnumHand hand = HANDS[x % HANDS.length];
            int slot = getInventorySlot(player, hand);
            if ( y != 0 )
                slot = y;

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if ( stack.isEmpty() )
                return null;

            Item item = stack.getItem();
            if ( item instanceof IAdminEditableItem )
                return ((IAdminEditableItem) item).getClientAdminGuiElement(stack, slot, player, world);

            else if ( item instanceof ItemBlock ) {
                Block block = ((ItemBlock) item).getBlock();
                if ( block instanceof IAdminEditableItem )
                    return ((IAdminEditableItem) block).getClientAdminGuiElement(stack, slot, player, world);
            }

            return null;
        }

        return super.getClientGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_ITEM ) {
            EnumHand hand = HANDS[x % HANDS.length];
            int slot = getInventorySlot(player, hand);
            if ( y != 0 )
                slot = y;

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if ( stack.isEmpty() )
                return null;

            Item item = stack.getItem();
            if ( item instanceof IGuiItem )
                return ((IGuiItem) item).getServerGuiElement(stack, slot, player, world);

            else if ( item instanceof ItemBlock ) {
                Block block = ((ItemBlock) item).getBlock();
                if ( block instanceof IGuiItem )
                    return ((IGuiItem) block).getServerGuiElement(stack, slot, player, world);
            }

            return null;
        }

        if ( id == WirelessUtils.GUI_ADMIN_ITEM ) {
            EnumHand hand = HANDS[x % HANDS.length];
            int slot = getInventorySlot(player, hand);
            if ( y != 0 )
                slot = y;

            ItemStack stack = player.inventory.getStackInSlot(slot);
            if ( stack.isEmpty() )
                return null;

            Item item = stack.getItem();
            if ( item instanceof IAdminEditableItem )
                return ((IAdminEditableItem) item).getServerAdminGuiElement(stack, slot, player, world);

            else if ( item instanceof ItemBlock ) {
                Block block = ((ItemBlock) item).getBlock();
                if ( block instanceof IAdminEditableItem )
                    return ((IAdminEditableItem) block).getServerAdminGuiElement(stack, slot, player, world);
            }

            return null;
        }

        return super.getServerGuiElement(id, player, world, x, y, z);
    }
}

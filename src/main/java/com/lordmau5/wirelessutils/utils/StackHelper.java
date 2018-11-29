package com.lordmau5.wirelessutils.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class StackHelper {

    public static boolean canInsertAll(IItemHandler target, List<ItemStack> items) {
        int slots = target.getSlots();
        for (ItemStack item : items) {
            for (int i = 0; i < slots; i++) {
                item = target.insertItem(i, item, true);
                if ( item.isEmpty() )
                    break;
            }

            if ( !item.isEmpty() )
                return false;
        }

        return true;
    }

    public static int insertAll(IItemHandler target, List<ItemStack> items) {
        int slots = target.getSlots();
        int inserted = 0;
        for (ItemStack item : items) {
            for (int i = 0; i < slots; i++) {
                int count = item.getCount();
                item = target.insertItem(i, item, false);
                inserted += count - item.getCount();
                if ( item.isEmpty() )
                    break;
            }
        }

        return inserted;
    }

}

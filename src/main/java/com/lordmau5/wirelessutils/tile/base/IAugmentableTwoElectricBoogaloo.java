package com.lordmau5.wirelessutils.tile.base;

import cofh.api.core.IAugmentable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IAugmentableTwoElectricBoogaloo extends IAugmentable {

    default boolean isValidAugment(ItemStack augment) {
        return isValidAugment(-1, augment);
    }

    boolean isValidAugment(int slot, ItemStack augment);

    /**
     * Returns TRUE if it's acceptable to remove a given augment.
     * Can be given an optional ItemStack to replace the augment with.
     * It could be a downgrade. It could be empty. It could be another
     * type of augment.
     */
    boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement);

}

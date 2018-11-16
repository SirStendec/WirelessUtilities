package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Arrays;

public interface ILevellingBlock extends IAugmentableTwoElectricBoogaloo {

    /* Level */

    boolean isCreative();

    Level getLevel();

    void setLevel(Level level);

    default void setLevel(int level) {
        setLevel(Level.fromInt(level));
    }

    /**
     * This method should be called by the setLevel() implementation once
     * the level has been updated to allow other code to update state based
     * on the tile entity's level.
     */
    default void updateLevel() {
    }

    /* Augments */

    default int getNumAugmentSlots() {
        return getLevel().augmentSlots;
    }

    @Override
    default boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement) {
        if ( !ModConfig.augments.requirePreviousTiers )
            return true;

        if ( hasHigherTierAugment(augment) )
            return false;

        return true;
    }

    default boolean hasLowerTierAugment(ItemStack augment) {
        Item augmentItem = augment.getItem();
        if ( !(augmentItem instanceof ItemAugment) )
            return false;

        ItemAugment item = (ItemAugment) augmentItem;
        if ( !item.shouldRequireLowerTier(augment) )
            return true;

        Level level = item.getLevel(augment);
        if ( level == Level.getMinLevel() )
            return true;

        Level required = Level.fromInt(level.toInt() - 1);
        ItemStack[] augments = getAugmentSlots();

        for (int i = 0; i < augments.length; i++) {
            ItemStack slotted = augments[i];
            if ( slotted.isEmpty() || slotted.getItem() != augmentItem )
                continue;

            if ( item.getLevel(slotted) == required )
                return true;
        }

        return false;
    }

    default boolean hasHigherTierAugment(ItemStack augment) {
        Item augmentItem = augment.getItem();
        if ( !(augmentItem instanceof ItemAugment) )
            return false;

        ItemAugment item = (ItemAugment) augmentItem;
        Level level = item.getLevel(augment);
        if ( level == Level.getMaxLevel() )
            return true;

        int levelInt = level.toInt();
        ItemStack[] augments = getAugmentSlots();

        for (int i = 0; i < augments.length; i++) {
            ItemStack slotted = augments[i];
            if ( slotted.isEmpty() || slotted.getItem() != augmentItem || !item.shouldRequireLowerTier(slotted) )
                continue;

            if ( item.getLevel(slotted).toInt() > levelInt )
                return true;
        }

        return false;
    }

    @Override
    default boolean isValidAugment(int slot, ItemStack augment) {
        Item augmentItem = augment.getItem();
        if ( !(augmentItem instanceof ItemAugment) )
            return false;

        ItemAugment item = (ItemAugment) augmentItem;
        if ( ModConfig.augments.requirePreviousTiers && !hasLowerTierAugment(augment) )
            return false;

        if ( !item.canApplyTo(augment, this) )
            return false;

        ItemStack[] augments = getAugmentSlots();

        // Are we removing an existing augment? Check that we can.
        // Skip this check for invalid slots.
        if ( slot >= 0 && slot <= augments.length ) {
            ItemStack installed = augments[slot];
            if ( !installed.isEmpty() ) {
                // If it's the same type of augment and this would be an upgrade,
                // we want to allow installing the new augment.
                if ( installed.getItem().equals(item) ) {
                    if ( ModConfig.augments.requirePreviousTiers )
                        return false;

                    if ( item.isUpgrade(installed, augment) )
                        return true;
                }

                // If it's not an upgrade or not the same augment, see if we
                // are allowed to remove the old augment.
                if ( !canRemoveAugment(null, slot, installed, augment) )
                    return false;
            }
        }

        // Check for duplicate augments now.
        if ( !item.canInstallMultiple() ) {
            Level level = item.getLevel(augment);

            for (int i = 0; i < augments.length; i++) {
                if ( i == slot )
                    continue;

                ItemStack slotted = augments[i];
                if ( slotted.getItem() == augmentItem ) {
                    if ( ModConfig.augments.requirePreviousTiers ) {
                        if ( item.getLevel(slotted) == level )
                            return false;
                    } else
                        return false;
                }
            }
        }

        return true;
    }

    @Override
    default boolean installAugment(ItemStack augment) {
        if ( !isValidAugment(augment) )
            return false;

        ItemStack[] slots = getAugmentSlots();
        if ( slots == null || slots.length == 0 )
            return false;

        for (int i = 0; i < slots.length; i++) {
            if ( slots[i].isEmpty() ) {
                slots[i] = augment;
                return true;
            }
        }

        return false;
    }

    /* NBT Save and Load */

    default void readLevelFromNBT(NBTTagCompound tag) {
        setLevel(tag.getByte("Level"));
    }

    default void writeLevelToNBT(NBTTagCompound tag) {
        tag.setByte("Level", (byte) getLevel().ordinal());
    }

    default void readAugmentsFromNBT(NBTTagCompound tag) {
        ItemStack[] augments = getAugmentSlots();
        Arrays.fill(augments, ItemStack.EMPTY);
        if ( !tag.hasKey("Augments") )
            return;

        NBTTagList list = tag.getTagList("Augments", 10);
        int count = list.tagCount();
        for (int i = 0; i < count; i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            int slot = item.getInteger("Slot");
            if ( slot >= 0 && slot < augments.length )
                augments[slot] = new ItemStack(item);
        }
    }

    default void writeAugmentsToNBT(NBTTagCompound tag) {
        ItemStack[] augments = getAugmentSlots();
        if ( augments == null || augments.length == 0 )
            return;

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < augments.length; i++) {
            if ( augments[i].isEmpty() )
                continue;

            NBTTagCompound item = new NBTTagCompound();
            augments[i].writeToNBT(item);
            item.setInteger("Slot", i);
            list.appendTag(item);
        }

        if ( list.tagCount() > 0 )
            tag.setTag("Augments", list);
    }


}

package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IPositionalMachine {

    boolean isInterdimensional();

    int getRange();

    BlockPosDimension getPosition();

    default boolean isPositionalCardValid(ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != ModItems.itemPositionalCard || !stack.hasTagCompound() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("position") && tag.hasKey("dimension");
    }

    default boolean isTargetInRange(BlockPosDimension target) {
        return isTargetInRange(target, getRange(), isInterdimensional());
    }

    default boolean isTargetInRange(BlockPosDimension target, int range, boolean interdimensional) {
        if ( interdimensional )
            return true;

        BlockPosDimension origin = getPosition();
        if ( origin.getDimension() != target.getDimension() )
            return false;

        return Math.floor(Math.sqrt(target.distanceSq(target))) <= range;
    }

}

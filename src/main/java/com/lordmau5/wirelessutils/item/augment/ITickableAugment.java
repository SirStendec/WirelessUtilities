package com.lordmau5.wirelessutils.item.augment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public interface ITickableAugment {

    // NBT

    /**
     * Write state to NBT. Useful if the machine is loaded/unloaded while
     * the augment is still installed.
     *
     * @return NBTTagCompound instance if we have data to save, or else null.
     */
    default NBTTagCompound writeToNBT() {
        return null;
    }

    /**
     * Called when a machine reads NBT if our augment previously saved NBT data.
     *
     * @param tag The previously saved NBT tag.
     */
    default void readFromNBT(@Nonnull NBTTagCompound tag) {

    }


    // Other Stuff

    /**
     * Get the ItemStack of the augment for this ITickableAugment
     *
     * @return The ItemStack
     */
    @Nonnull
    ItemStack getItemStack();

    /**
     * Called when the augment installed into the tile entity this ITickableAugment is associated
     * with is replaced. This should be used to update the state of the ITickableAugment or to
     * tell the machine to destroy this instance and create a new one.
     *
     * @param stack The new installed augment.
     * @return True if the update was a success. False if the ITickableAugment should be recreated.
     */
    default boolean update(@Nonnull ItemStack stack) {
        return ItemStack.areItemStacksEqual(getItemStack(), stack);
    }

    /**
     * Called every TileEntity tick. This happens before a machine's initial work.
     *
     * @param active Whether or not the machine is active. For preTick this basically just checks
     *               if the machine isn't disabled with RS control.
     */
    void preTick(boolean active);

    /**
     * Called every TileEntity tick. This happens after a machine's initial work but before its
     * main work cycle.
     *
     * @param active Whether or not the machine is active and able to work.
     */
    void tick(boolean active);

    /**
     * Called TileEntity ticks when the TileEntity is active. This happens after a machine's main
     * work cycle.
     */
    void postTick();

    /**
     * Called before the ITickableAugment instance is destroyed.
     */
    void destroy();

}

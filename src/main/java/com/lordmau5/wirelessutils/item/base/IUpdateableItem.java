package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public interface IUpdateableItem {

    void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet);

    static void copyOrRemoveNBTKeys(@Nonnull NBTTagCompound source, @Nonnull NBTTagCompound dest, boolean copyTags, String... keys) {
        for (String key : keys) {
            if ( source.hasKey(key) ) {
                NBTBase tag = source.getTag(key);
                if ( copyTags )
                    tag = tag.copy();

                dest.setTag(key, tag);
            } else
                dest.removeTag(key);
        }
    }

}

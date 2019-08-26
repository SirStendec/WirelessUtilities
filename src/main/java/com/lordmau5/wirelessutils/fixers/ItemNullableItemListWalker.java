package com.lordmau5.wirelessutils.fixers;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.fixers.base.FilteredItemWalker;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemNullableItemListWalker extends FilteredItemWalker {

    private final Set<String> keys;

    public ItemNullableItemListWalker(Item item, String... keys) {
        this(ImmutableSet.of(item), keys);
    }

    public ItemNullableItemListWalker(Set<Item> items, String... keys) {
        super(items);
        this.keys = ImmutableSet.copyOf(keys);
    }

    @Nonnull
    public NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int version) {
        for (String key : keys) {
            if ( compound.hasKey(key, Constants.NBT.TAG_LIST) ) {
                NBTTagList itemList = compound.getTagList(key, Constants.NBT.TAG_COMPOUND);
                if ( itemList != null && !itemList.isEmpty() ) {
                    int length = itemList.tagCount();
                    for (int i = 0; i < length; i++) {
                        NBTTagCompound itemTag = itemList.getCompoundTagAt(i);
                        if ( itemTag != null && !itemTag.isEmpty() )
                            itemList.set(i, fixer.process(FixTypes.ITEM_INSTANCE, itemTag, version));
                    }
                }
            }
        }

        return compound;
    }
}

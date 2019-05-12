package com.lordmau5.wirelessutils.fixers;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Set;

public class NullableItemListWalker extends FilteredWalker {

    private final Set<String> keys;

    public NullableItemListWalker(Class<?> klass, String... keys) {
        this(ImmutableSet.of(klass), keys);
    }

    public NullableItemListWalker(Set<Class<?>> klasses, String... keys) {
        super(klasses);
        this.keys = ImmutableSet.copyOf(keys);
    }

    @Nonnull
    NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int version) {
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

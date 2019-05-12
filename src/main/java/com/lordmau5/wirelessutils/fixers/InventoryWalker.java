package com.lordmau5.wirelessutils.fixers;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Set;

public class InventoryWalker extends FilteredWalker {

    private final Set<String> keys;

    public InventoryWalker(Class<?> klass, String... keys) {
        this(ImmutableSet.of(klass), keys);
    }

    public InventoryWalker(Set<Class<?>> klasses, String... keys) {
        super(klasses);
        this.keys = ImmutableSet.copyOf(keys);
    }

    @Nonnull
    NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int version) {
        for (String key : keys) {
            if ( compound.hasKey(key, Constants.NBT.TAG_COMPOUND) ) {
                NBTTagCompound handler = compound.getCompoundTag(key);
                DataFixesManager.processInventory(fixer, handler, version, "Items");
            }
        }

        return compound;
    }
}

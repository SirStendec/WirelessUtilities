package com.lordmau5.wirelessutils.fixers.base;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import java.util.Set;

public abstract class FilteredWalker implements IDataWalker {

    private Set<ResourceLocation> keys;

    public FilteredWalker() {

    }

    public FilteredWalker(ResourceLocation key) {
        this(ImmutableSet.of(key));
    }

    public FilteredWalker(Set<ResourceLocation> keys) {
        setKeys(keys);
    }

    protected void setKeys(Set<ResourceLocation> keys) {
        this.keys = keys;
    }


    @Override
    public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int version) {
        if ( keys != null && keys.contains(new ResourceLocation(compound.getString("id"))) )
            return filteredProcess(fixer, compound, version);

        return compound;
    }

    public abstract NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int version);
}

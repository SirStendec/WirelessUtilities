package com.lordmau5.wirelessutils.fixers;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public abstract class FilteredWalker implements IDataWalker {

    private final Set<ResourceLocation> keys;

    public FilteredWalker(Class<?> klass) {
        this(ImmutableSet.of(klass));
    }

    public FilteredWalker(Set<Class<?>> klasses) {
        keys = new HashSet<>();

        for (Class<?> klass : klasses) {
            if ( Entity.class.isAssignableFrom(klass) )
                keys.add(EntityList.getKey((Class<Entity>) klass));
            else if ( TileEntity.class.isAssignableFrom(klass) )
                keys.add(TileEntity.getKey((Class<TileEntity>) klass));
            else
                throw new RuntimeException("invalid class type");
        }
    }

    @Nonnull
    public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int version) {
        if ( keys.contains(new ResourceLocation(compound.getString("id"))) )
            return filteredProcess(fixer, compound, version);

        return compound;
    }

    @Nonnull
    abstract NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int version);
}

package com.lordmau5.wirelessutils.fixers.base;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public abstract class FilteredBlockWalker extends FilteredWalker {

    public FilteredBlockWalker(Class<?> klass) {
        this(ImmutableSet.of(klass));
    }

    public FilteredBlockWalker(Set<Class<?>> klasses) {
        super();

        HashSet<ResourceLocation> keys = new HashSet<>();

        for (Class<?> klass : klasses) {
            if ( Entity.class.isAssignableFrom(klass) )
                keys.add(EntityList.getKey((Class<Entity>) klass));
            else if ( TileEntity.class.isAssignableFrom(klass) )
                keys.add(TileEntity.getKey((Class<TileEntity>) klass));
            else
                throw new RuntimeException("invalid class type");
        }

        setKeys(keys);
    }
}

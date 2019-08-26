package com.lordmau5.wirelessutils.fixers.base;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public abstract class FilteredItemWalker extends FilteredWalker {

    public FilteredItemWalker(Item item) {
        this(ImmutableSet.of(item));
    }

    public FilteredItemWalker(Set<Item> items) {
        super();

        Set<ResourceLocation> keys = new HashSet<>();

        for (Item item : items) {
            keys.add(item.getRegistryName());
        }

        setKeys(keys);
    }

}

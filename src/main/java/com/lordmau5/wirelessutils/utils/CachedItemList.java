package com.lordmau5.wirelessutils.utils;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class CachedItemList {

    private ItemStack[] ghosts;

    private Set<String> mods;
    private Set<ItemStack> stacks;
    private Set<Item> items;

    public CachedItemList(String... input) {
        if ( input.length > 0 )
            addInput(input);
    }

    public void clear() {
        mods = null;
        stacks = null;
        items = null;
        ghosts = null;
    }

    public void refresh() {
        if ( stacks != null ) {
            final Set<ItemStack> newStacks = new ObjectOpenHashSet<>();
            for (ItemStack stack : stacks)
                newStacks.add(stack);
            stacks = newStacks;
        }

        if ( items != null ) {
            final Set<Item> newItems = new ObjectOpenHashSet<>();
            for (Item item : items)
                newItems.add(item);
            items = newItems;
        }
    }

    @Nonnull
    public ItemStack[] getGhosts() {
        if ( ghosts != null )
            return ghosts;

        Set<ItemStack> out = new HashSet<>();

        if ( stacks != null ) {
            for (ItemStack stack : stacks)
                out.add(stack);
        }

        if ( items != null ) {
            for (Item item : items)
                out.add(new ItemStack(item));
        }

        ghosts = out.toArray(new ItemStack[0]);
        return ghosts;
    }

    public boolean matches(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() )
            return false;

        if ( stacks != null && stacks.contains(stack) )
            return true;

        Item item = stack.getItem();
        if ( items != null && items.contains(item) )
            return true;

        if ( mods == null )
            return false;

        ResourceLocation location = item.getRegistryName();
        String ns = location == null ? null : location.getNamespace();
        return ns != null && mods.contains(ns);
    }

    public void addInput(String... inputList) {
        for (String input : inputList)
            addInput(input);
    }

    public void addInput(@Nullable String input) {
        if ( input == null || input.isEmpty() )
            return;

        input = input.trim();
        if ( input.isEmpty() )
            return;

        ghosts = null;

        String namespace;
        String path;

        int idx = input.indexOf(":");
        if ( idx == -1 ) {
            namespace = "minecraft";
            path = input;
        } else {
            namespace = input.substring(0, idx);
            path = input.substring(idx + 1);
        }

        if ( path.equals("*") ) {
            if ( mods == null )
                mods = new HashSet<>();

            mods.add(namespace);
            return;
        }

        idx = path.indexOf("@");
        if ( idx == -1 ) {
            ResourceLocation location = new ResourceLocation(namespace, path);
            Item item = Item.REGISTRY.getObject(location);
            if ( item == null )
                return;

            if ( items == null )
                items = new ObjectOpenHashSet<>();

            items.add(item);
            return;
        }

        int metadata = 0;
        try {
            metadata = Integer.parseInt(path.substring(idx + 1));
        } catch (NumberFormatException ex) {
            return;
        }

        path = path.substring(0, idx);
        ResourceLocation location = new ResourceLocation(namespace, path);
        Item item = Item.REGISTRY.getObject(location);
        if ( item == null )
            return;

        if ( stacks == null )
            stacks = new ObjectOpenHashSet<>();

        stacks.add(new ItemStack(item, 1, metadata));
    }
}

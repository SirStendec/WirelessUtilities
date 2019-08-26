package com.lordmau5.wirelessutils.fixers;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.fixers.base.FilteredItemWalker;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

public class ItemVoidEntityWalker extends FilteredItemWalker {

    public ItemVoidEntityWalker(Item item) {
        this(ImmutableSet.of(item));
    }

    public ItemVoidEntityWalker(Set<Item> items) {
        super(items);
    }

    public NBTTagCompound filteredProcess(IDataFixer fixer, NBTTagCompound compound, int version) {
        if ( compound == null || !compound.hasKey("EntityID", Constants.NBT.TAG_STRING) )
            return compound;

        final String entityID = compound.getString("EntityID");
        if ( entityID == null || entityID.isEmpty() )
            return compound;

        NBTTagCompound entityData = compound.getCompoundTag("EntityData");
        entityData.setString("id", entityID);

        entityData = fixer.process(FixTypes.ENTITY, entityData, version);
        if ( entityData != null ) {
            final String newID = entityData.getString("id");
            if ( !entityID.equals(newID) )
                compound.setString("EntityID", newID);

            entityData.removeTag("id");
            compound.setTag("EntityData", entityData);

        } else {
            compound.removeTag("EntityID");
            compound.removeTag("EntityData");
        }

        return compound;
    }
}

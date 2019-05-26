package com.lordmau5.wirelessutils.plugins.ThermalExpansion;

import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TEPlugin implements IPlugin {

    @GameRegistry.ObjectHolder("thermalexpansion:morb")
    public static Item itemMorb;

    @Override
    public void init(FMLInitializationEvent event) {
        if ( ModConfig.vaporizers.compatibility.useMorbs && itemMorb != null ) {
            EntityUtilities.registerHandler(itemMorb, new EntityUtilities.IEntityBall() {
                @Nullable
                public Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData) {
                    if ( !isFilledItem(stack) )
                        return null;

                    NBTTagCompound tag = stack.getTagCompound();
                    Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(tag.getString("id")), world);
                    if ( entity != null && withData )
                        entity.readFromNBT(tag);

                    return entity;
                }

                @Nonnull
                @Override
                public ItemStack captureEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
                    if ( stack.isEmpty() || stack.getItem() != itemMorb || isFilledItem(stack) )
                        return ItemStack.EMPTY;

                    if ( !(entity instanceof EntityLiving) || !entity.isEntityAlive() || !entity.isNonBoss() )
                        return ItemStack.EMPTY;

                    ResourceLocation key = EntityList.getKey(entity);
                    // Thermal only has Morbs for entities with eggs.
                    if ( key == null || !EntityList.ENTITY_EGGS.containsKey(key) )
                        return ItemStack.EMPTY;

                    if ( EntityUtilities.isBlacklisted(key) )
                        return ItemStack.EMPTY;

                    ItemStack out = stack.copy();
                    if ( out.getCount() > 1 )
                        out.setCount(1);

                    EntityUtilities.saveBaseExperience(key, entity);
                    NBTTagCompound tag = new NBTTagCompound();

                    // This is hot garbage, just saying.
                    entity.writeToNBT(tag);

                    // At least TE filters out some tags.
                    for (String badTag : EntityUtilities.BAD_TAGS)
                        tag.removeTag(badTag);

                    tag.setString("id", key.toString());
                    out.setTagCompound(tag);
                    entity.setDead();
                    return out;
                }

                @Override
                public boolean isValidItem(@Nonnull ItemStack stack) {
                    return stack.getItem() == itemMorb;
                }

                @Override
                public boolean isFilledItem(@Nonnull ItemStack stack) {
                    if ( !isValidItem(stack) )
                        return false;

                    NBTTagCompound tag = stack.getTagCompound();
                    return tag != null && tag.hasKey("id");
                }
            });
        }
    }
}

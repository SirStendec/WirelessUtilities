package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.item.pearl.ItemVoidPearl;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EntityUtilities {

    private static Map<String, Integer> spawnCosts = new HashMap<>();

    public static void setSpawnCost(@Nonnull ResourceLocation name, int cost) {
        String key = name.toString();
        spawnCosts.put(key, cost);
    }

    public static void saveSpawnCost(@Nonnull ResourceLocation name, int cost) {
        String key = name.toString();
        if ( !spawnCosts.containsKey(key) )
            spawnCosts.put(key, cost);
    }

    public static int getSpawnCost(@Nonnull ResourceLocation name, @Nullable World world) {
        String key = name.toString();

        if ( spawnCosts.containsKey(key) )
            return spawnCosts.get(key);

        int cost = -1;
        if ( world != null && EntityList.isRegistered(name) ) {
            Entity entity = EntityList.createEntityByIDFromName(name, world);
            if ( entity instanceof EntityLiving ) {
                cost = ((EntityLiving) entity).experienceValue;
                spawnCosts.put(key, cost);
            }
        }

        return cost;
    }

    public static int getSpawnCost(@Nonnull ItemStack stack, @Nullable World world) {
        if ( !isFilledEntityBall(stack) )
            return -1;

        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return getSpawnCost(((ItemVoidPearl) item).getCapturedId(stack), world);

        return -1;
    }

    public static boolean isBlacklisted(String key) {
        if ( key == null )
            return false;

        key = key.toLowerCase();
        for (String listed : ModConfig.items.voidPearl.blacklist) {
            if ( listed.equals(key) )
                return true;
        }

        return false;
    }

    public static boolean isBlacklisted(@Nullable ResourceLocation name) {
        if ( name == null )
            return false;

        return isBlacklisted(name.toString());
    }

    public static boolean isBlacklisted(@Nonnull ItemStack stack) {
        if ( !isFilledEntityBall(stack) )
            return false;

        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return isBlacklisted(((ItemVoidPearl) item).getCapturedId(stack));

        return false;
    }


    public static boolean isEntityBall(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return true;

        return false;
    }

    public static boolean isFilledEntityBall(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl ) {
            ItemVoidPearl pearl = (ItemVoidPearl) item;
            return pearl.containsEntity(stack);
        }

        return false;
    }

    @Nonnull
    public static ItemStack captureEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return ((ItemVoidPearl) item).captureEntity(stack, entity);

        return ItemStack.EMPTY;
    }

    @Nullable
    public static Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return ((ItemVoidPearl) item).getCapturedEntity(stack, world, withData);

        return null;
    }

}

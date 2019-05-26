package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.item.pearl.ItemVoidPearl;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EntityUtilities {

    private static Map<String, Integer> baseExperience = new HashMap<>();
    private static Map<Item, IEntityBall> entityBallMap = new Object2ObjectOpenHashMap<>();

    public static void setBaseExperience(@Nonnull ResourceLocation name, int cost) {
        String key = name.toString();
        baseExperience.put(key, cost);
    }

    public static void saveBaseExperience(@Nonnull ResourceLocation name, int cost) {
        String key = name.toString();
        if ( !baseExperience.containsKey(key) )
            baseExperience.put(key, cost);
    }

    public static int saveBaseExperience(@Nonnull ResourceLocation name, @Nullable Entity entity) {
        String key = name.toString();
        if ( baseExperience.containsKey(key) )
            return baseExperience.get(key);

        int cost = getBaseFromEntity(entity);
        baseExperience.put(key, cost);
        return cost;
    }

    public static int getBaseFromEntity(@Nullable Entity entity) {
        int cost = -1;
        if ( entity instanceof EntityAnimal )
            cost = ModConfig.vaporizers.modules.clone.animalBaseExp;

        if ( entity instanceof EntityLiving ) {
            int value = ((EntityLiving) entity).experienceValue;
            if ( value > 0 )
                cost = value;
        }

        if ( cost == -1 )
            cost = 0;

        return cost;
    }

    public static int getBaseExperience(@Nonnull Entity entity) {
        ResourceLocation name = EntityList.getKey(entity);
        if ( name == null )
            return 0;

        return getBaseExperience(name, entity);
    }

    public static int getBaseExperience(@Nonnull ResourceLocation name, @Nullable Entity entity) {
        String key = name.toString();

        if ( baseExperience.containsKey(key) )
            return baseExperience.get(key);

        if ( entity != null )
            return saveBaseExperience(name, entity);

        return 0;
    }

    public static int getBaseExperience(@Nonnull ResourceLocation name, @Nullable World world) {
        String key = name.toString();

        if ( baseExperience.containsKey(key) )
            return baseExperience.get(key);

        if ( world != null && EntityList.isRegistered(name) )
            return saveBaseExperience(name, EntityList.createEntityByIDFromName(name, world));

        return 0;
    }

    public static int getBaseExperience(@Nonnull ItemStack stack, @Nullable World world) {
        if ( !isFilledEntityBall(stack) )
            return -1;

        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return getBaseExperience(((ItemVoidPearl) item).getCapturedId(stack), world);

        return -1;
    }

    public static boolean isBlacklisted(String key) {
        if ( key == null )
            return false;

        return isBlacklisted(new ResourceLocation(key));
    }

    public static boolean isBlacklisted(@Nullable ResourceLocation name) {
        if ( name == null )
            return false;

        String key = name.toString();
        String ns = name.getNamespace() + ":*";

        for (String listed : ModConfig.items.voidPearl.blacklist) {
            if ( listed.equals(key) || listed.equals(ns) )
                return true;
        }

        return false;
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

        IEntityBall handler = entityBallMap.get(item);
        if ( handler != null )
            return handler.isValidItem(stack);

        return false;
    }

    public static boolean isFilledEntityBall(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl ) {
            ItemVoidPearl pearl = (ItemVoidPearl) item;
            return pearl.containsEntity(stack);
        }

        IEntityBall handler = entityBallMap.get(item);
        if ( handler != null )
            return handler.isFilledItem(stack);

        return false;
    }

    @Nonnull
    public static ItemStack captureEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return ((ItemVoidPearl) item).captureEntity(stack, entity);

        IEntityBall handler = entityBallMap.get(item);
        if ( handler != null )
            return handler.captureEntity(stack, entity);

        return ItemStack.EMPTY;
    }

    @Nullable
    public static Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return ((ItemVoidPearl) item).getCapturedEntity(stack, world, withData);

        IEntityBall handler = entityBallMap.get(item);
        if ( handler != null )
            return handler.getEntity(stack, world, withData);

        return null;
    }

    public static void registerHandler(@Nonnull Item item, @Nonnull IEntityBall handler) {
        entityBallMap.put(item, handler);
    }

    public IEntityBall getHandler(@Nonnull Item item) {
        return entityBallMap.get(item);
    }

    public interface IEntityBall {
        @Nullable
        Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData);

        @Nonnull
        ItemStack captureEntity(@Nonnull ItemStack stack, @Nonnull Entity entity);

        boolean isValidItem(@Nonnull ItemStack stack);

        boolean isFilledItem(@Nonnull ItemStack stack);
    }

    public static String[] BAD_TAGS = {
            "UUIDMost", "UUIDLeast",
            "Rotation", "Pos", "Motion",
            "FallDistance", "OnGround", "Air",
            "Dimension", "PortalCooldown", "Leash", "Leashed"
    };

}

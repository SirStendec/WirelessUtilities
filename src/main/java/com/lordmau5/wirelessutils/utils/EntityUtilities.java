package com.lordmau5.wirelessutils.utils;

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
        ResourceLocation name = getEntityId(stack);
        if ( name == null )
            return 0;

        return getBaseExperience(name, world);
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
        ResourceLocation name = getEntityId(stack);
        if ( name == null )
            return false;

        return isBlacklisted(name);
    }


    public static boolean isEntityBall(@Nonnull ItemStack stack) {
        IEntityBall handler = entityBallMap.get(stack.getItem());
        if ( handler != null )
            return handler.isValidBall(stack);

        return false;
    }

    public static boolean isFilledEntityBall(@Nonnull ItemStack stack) {
        IEntityBall handler = entityBallMap.get(stack.getItem());
        if ( handler != null )
            return handler.isFilledBall(stack);

        return false;
    }

    @Nonnull
    public static ItemStack saveEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
        ResourceLocation name = EntityList.getKey(entity);
        if ( name != null )
            saveBaseExperience(name, entity);

        IEntityBall handler = entityBallMap.get(stack.getItem());
        if ( handler != null )
            return handler.saveEntity(stack, entity);

        return ItemStack.EMPTY;
    }

    @Nullable
    public static Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData) {
        IEntityBall handler = entityBallMap.get(stack.getItem());
        if ( handler != null )
            return handler.getEntity(stack, world, withData);

        return null;
    }

    @Nullable
    public static Class<? extends Entity> getEntityClass(@Nonnull ItemStack stack) {
        IEntityBall handler = entityBallMap.get(stack.getItem());
        if ( handler != null )
            return handler.getEntityClass(stack);

        return null;
    }

    @Nullable
    public static ResourceLocation getEntityId(@Nonnull ItemStack stack) {
        IEntityBall handler = entityBallMap.get(stack.getItem());
        if ( handler != null )
            return handler.getEntityId(stack);

        return null;
    }

    public static void registerHandler(@Nonnull Item item, @Nonnull IEntityBall handler) {
        entityBallMap.put(item, handler);
    }

    public IEntityBall getHandler(@Nonnull Item item) {
        return entityBallMap.get(item);
    }

    public interface IEntityBall {
        /**
         * Get an instance of the entity trapped within the entity ball.
         *
         * @param stack    The ItemStack to read the entity from.
         * @param world    The World to create the entity in.
         * @param withData If true, the entity should load its NBT data from the entity ball. If false, it will not.
         * @return An instance of the entity. null if no entity can be loaded.
         */
        @Nullable
        Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData);

        /**
         * Get the Class of the captured entity.
         *
         * @param stack The ItemStack to read the entity class from.
         * @return A Class, or null if there is no valid entity class.
         */
        @Nullable
        Class<? extends Entity> getEntityClass(@Nonnull ItemStack stack);

        /**
         * Get the registered name of the captured entity.
         *
         * @param stack The ItemStack to read the name from.
         * @return The ResourceLocation for the captured entity's name, or null if there is none.
         */
        @Nullable
        ResourceLocation getEntityId(@Nonnull ItemStack stack);

        /**
         * Save an entity into the entity ball, returning the filled entity ball item stack.
         * The entity should not be erased.
         *
         * @param stack  The ItemStack to insert the entity into.
         * @param entity The entity to be inserted.
         * @return An ItemStack with the entity saved. EMPTY if this failed.
         */
        @Nonnull
        ItemStack saveEntity(@Nonnull ItemStack stack, @Nonnull Entity entity);

        /**
         * Attempt to remove the captured entity data from the entity ball, returning the
         * empty entity ball item. The entity should not be spawned into any world.
         *
         * @param stack The ItemStack to remove the captured entity from.
         * @return An ItemStack with the entity removed. EMPTY if this failed.
         */
        @Nonnull
        ItemStack removeEntity(@Nonnull ItemStack stack);

        /**
         * Check if the provided item stack is a valid entity ball.
         *
         * @param stack The ItemStack to check.
         * @return True if the provided item is valid.
         */
        boolean isValidBall(@Nonnull ItemStack stack);

        /**
         * Check if the provided entity ball item stack contains an entity.
         *
         * @param stack The ItemStack to check.
         * @return True if the entity ball contains an entity.
         */
        boolean isFilledBall(@Nonnull ItemStack stack);
    }

    public static String[] BAD_TAGS = {
            "UUIDMost", "UUIDLeast",
            "Rotation", "Pos", "Motion",
            "FallDistance", "OnGround", "Air",
            "Dimension", "PortalCooldown", "Leash", "Leashed"
    };

}

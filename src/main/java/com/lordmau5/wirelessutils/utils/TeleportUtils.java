package com.lordmau5.wirelessutils.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleportUtils {

    public static class WUTeleporter implements ITeleporter {
        public final double x;
        public final double y;
        public final double z;

        public WUTeleporter(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            world.getBlockState(new BlockPos(x, y, z));
            entity.setPosition(x, y, z);
        }
    }

    public static void teleportPlayer(@Nonnull EntityPlayer player, int dimension, double x, double y, double z) {
        World world = DimensionManager.getWorld(dimension);
        if ( world == null )
            return;

        teleportPlayer(player, world, x, y, z);
    }

    public static void teleportPlayer(@Nonnull EntityPlayer player, @Nonnull World world, double x, double y, double z) {
        World oldWorld = player.getEntityWorld();

        // If we aren't changing worlds, just update the position.
        if ( oldWorld == world ) {
            player.setPositionAndUpdate(x, y, z);
            return;
        }

        if ( !(world instanceof WorldServer) || !(player instanceof EntityPlayerMP) )
            return;

        MinecraftServer server = world.getMinecraftServer();
        if ( server == null )
            return;

        server.getPlayerList().transferPlayerToDimension(
                (EntityPlayerMP) player,
                world.provider.getDimension(),
                new WUTeleporter(x, y, z)
        );

        player.setPositionAndUpdate(x, y, z);
    }

    @Nullable
    public static Entity teleportEntity(@Nonnull Entity entity, int dimension, double x, double y, double z) {
        World world = DimensionManager.getWorld(dimension);
        if ( world == null )
            return null;

        return teleportEntity(entity, world, x, y, z);
    }

    @Nullable
    public static Entity teleportEntity(@Nonnull Entity entity, @Nonnull World world, double x, double y, double z) {
        if ( entity instanceof EntityPlayer ) {
            teleportPlayer((EntityPlayer) entity, world, x, y, z);
            return entity;
        }

        World oldWorld = entity.getEntityWorld();

        // If we aren't changing worlds, just update the position.
        if ( oldWorld == world ) {
            entity.setPositionAndUpdate(x, y, z);
            world.updateEntityWithOptionalForce(entity, false);
            return entity;
        }

        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);
        tag.removeTag("Dimension");

        world.removeEntity(entity);
        entity.setDead();

        Entity newEntity = EntityList.newEntity(entity.getClass(), world);
        newEntity.readFromNBT(tag);
        newEntity.setPosition(x, y, z);

        newEntity.forceSpawn = true;
        world.spawnEntity(newEntity);
        newEntity.forceSpawn = false;

        return newEntity;
    }
}

package com.lordmau5.wirelessutils.utils;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class WUFakePlayer extends FakePlayer {

    private static Map<Integer, WUFakePlayer> fakePlayerMap = new Int2ObjectOpenHashMap<>();

    private static final UUID uuid = UUID.fromString("1a82a7dc-9467-40e8-b806-b5ebf0a39a73");
    private static GameProfile PROFILE = new GameProfile(uuid, "[WU]");

    public static WUFakePlayer getFakePlayer(@Nonnull World world) {
        int dimension = world.provider.getDimension();
        if ( fakePlayerMap.containsKey(dimension) )
            return fakePlayerMap.get(dimension);

        if ( world instanceof WorldServer ) {
            WUFakePlayer player = new WUFakePlayer((WorldServer) world);
            fakePlayerMap.put(dimension, player);
            return player;
        }

        return null;
    }

    public static WUFakePlayer getFakePlayer(@Nonnull World world, BlockPos pos) {
        WUFakePlayer player = getFakePlayer(world);
        if ( player != null && pos != null )
            player.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), 90, 90);

        return player;
    }

    public WUFakePlayer(WorldServer world) {
        super(world, PROFILE);
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) {

    }

    @Override
    public void openEditSign(TileEntitySign signTile) {

    }
}

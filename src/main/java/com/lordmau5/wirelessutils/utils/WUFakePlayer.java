package com.lordmau5.wirelessutils.utils;

import cofh.core.entity.NetServerHandlerFake;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class WUFakePlayer extends FakePlayer {

    private static final Map<Integer, WUFakePlayer> fakePlayerMap = new Int2ObjectOpenHashMap<>();

    private static final UUID uuid = UUID.fromString("1a82a7dc-9467-40e8-b806-b5ebf0a39a73");
    private static final GameProfile PROFILE = new GameProfile(uuid, "[WU]");

    private final ItemStack[] cachedEquipment = new ItemStack[6];

    public static void removeFakePlayer(@Nonnull World world) {
        int dimension = world.provider.getDimension();
        fakePlayerMap.remove(dimension);
    }

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
        connection = new NetServerHandlerFake(FMLCommonHandler.instance().getMinecraftServerInstance(), this);
        addedToChunk = false;

        capabilities.disableDamage = true;
        setSize(0, 0);

        Arrays.fill(cachedEquipment, ItemStack.EMPTY);
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public void playSound(@Nonnull SoundEvent soundIn, float volume, float pitch) {
        // Intentionally Left Blank
    }

    @Override
    public void openEditSign(TileEntitySign signTile) {
        // Intentionally Left Blank
    }

    public void updateCooldown() {
        this.ticksSinceLastSwing = 10000;
    }

    public void updateAttributes() {
        updateAttributes(false);
    }

    public void updateAttributes(boolean handOnly) {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if ( handOnly && slot.getSlotType() != EntityEquipmentSlot.Type.HAND )
                continue;

            int index = slot.getSlotIndex();
            ItemStack cached = cachedEquipment[index].copy();
            ItemStack stack = getItemStackFromSlot(slot);

            if ( !ItemStack.areItemsEqual(stack, cached) ) {
                if ( !cached.isEmpty() )
                    getAttributeMap().removeAttributeModifiers(cached.getAttributeModifiers(slot));
                if ( !stack.isEmpty() )
                    getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers(slot));

                cachedEquipment[index] = stack;
            }
        }
    }
}

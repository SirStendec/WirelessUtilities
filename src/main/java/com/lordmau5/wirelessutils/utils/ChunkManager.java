package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.Map;
import java.util.Set;

public class ChunkManager {

    private static Map<Integer, Map<ChunkPos, ChunkRequests>> loadedChunks = new Int2ObjectOpenHashMap<>();

    public static ChunkRequests getChunk(Tuple<Integer, ChunkPos> posTuple) {
        return getChunk(posTuple.getFirst(), posTuple.getSecond());
    }

    public static ChunkRequests getChunk(int dimension, ChunkPos chunk) {
        Map<ChunkPos, ChunkRequests> dimStuff = loadedChunks.get(dimension);
        if ( dimStuff == null ) {
            dimStuff = new Object2ObjectOpenHashMap<>();
            loadedChunks.put(dimension, dimStuff);
        }

        ChunkRequests requests = dimStuff.get(chunk);
        if ( requests == null ) {
            requests = new ChunkRequests(dimension, chunk);
            dimStuff.put(chunk, requests);
        }

        return requests;
    }

    public static void unloadWorld(int dimension) {
        Map<ChunkPos, ChunkRequests> chunks = loadedChunks.get(dimension);
        if ( chunks == null )
            return;

        for (ChunkRequests requests : chunks.values())
            requests.unloadAll();

        loadedChunks.remove(dimension);
    }

    public static void unloadAll() {
        Set<Integer> ids = new IntArraySet(loadedChunks.keySet());
        for (int dimension : ids)
            unloadWorld(dimension);
    }

    public static class ChunkRequests {
        private final int dimension;
        private final ChunkPos chunk;

        private final Map<Integer, GameProfile> requestToProfile = new Int2ObjectOpenHashMap<>();
        private final Map<GameProfile, ForgeChunkManager.Ticket> profileTickets = new Object2ObjectOpenHashMap<>();
        private final Map<GameProfile, Set<Integer>> profileRequests = new Object2ObjectOpenHashMap<>();
        private int lastRequest = 0;

        public ChunkRequests(int dimension, ChunkPos chunk) {
            this.dimension = dimension;
            this.chunk = chunk;
        }

        /**
         * Attempt to load the chunk for the given GameProfile.
         *
         * @param profile The profile we're loading for.
         * @return -1 if loading failed, else the ID of the request
         * for use when unloading.
         */
        public int load(GameProfile profile) {
            Set<Integer> requests = profileRequests.get(profile);
            if ( requests == null ) {
                World world = DimensionManager.getWorld(dimension);
                if ( world == null ) {
                    DimensionManager.initDimension(dimension);
                    world = DimensionManager.getWorld(dimension);
                    if ( world == null )
                        return -1;
                }

                ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestPlayerTicket(WirelessUtils.instance, profile.getName(), world, ForgeChunkManager.Type.NORMAL);
                if ( ticket == null )
                    return -1;

                profileTickets.put(profile, ticket);
                ForgeChunkManager.forceChunk(ticket, chunk);
                requests = new IntArraySet();
                profileRequests.put(profile, requests);
            }

            int id = lastRequest++;
            requests.add(id);
            requestToProfile.put(id, profile);
            return id;
        }

        /**
         * Attempt to unload a chunk for the given request ID.
         *
         * @param id The ID returned from the load request.
         */
        public void unload(int id) {
            GameProfile profile = requestToProfile.get(id);
            if ( profile == null )
                return;

            Set<Integer> requests = profileRequests.get(profile);
            if ( requests != null ) {
                requests.remove(id);
                if ( !requests.isEmpty() )
                    return;
            }

            ForgeChunkManager.Ticket ticket = profileTickets.get(profile);

            requestToProfile.remove(id);
            profileTickets.remove(profile);
            profileRequests.remove(profile);

            if ( ticket != null ) {
                ForgeChunkManager.unforceChunk(ticket, chunk);
                ForgeChunkManager.releaseTicket(ticket);
            }
        }

        public void unloadAll() {
            Set<Integer> ids = new IntArraySet(requestToProfile.keySet());
            for (int id : ids)
                unload(id);
        }
    }
}

package com.lordmau5.wirelessutils.utils;

import com.google.common.collect.MapMaker;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class EventDispatcher<T extends Event> {

    public static final EventDispatcher WORLD_UNLOAD = new EventDispatcher<WorldEvent.Unload>();
    public static final EventDispatcher PLACE_BLOCK = new EventDispatcher<BlockEvent.PlaceEvent>();
    public static final EventDispatcher BREAK_BLOCK = new EventDispatcher<BlockEvent.BreakEvent>();
    public static final EventDispatcher CHUNK_LOAD = new EventDispatcher<ChunkEvent.Load>();
    public static final EventDispatcher CHUNK_UNLOAD = new EventDispatcher<ChunkEvent.Unload>();

    private final Map<Integer, Map<Long, ConcurrentMap<IEventListener<T>, IEventListener<T>>>> allListeners = new Int2ObjectOpenHashMap<>();
    private final ConcurrentMap<IEventListener<T>, Map<Integer, Set<Long>>> listenerPositions = new MapMaker().weakKeys().makeMap();

    public void addListener(int dimension, @Nonnull BlockPos pos, @Nonnull IEventListener<T> listener) {
        addListener(dimension, pos.getX() >> 4, pos.getZ() >> 4, listener);
    }

    public void addListener(BlockPosDimension pos, @Nonnull IEventListener<T> listener) {
        addListener(pos.getDimension(), pos, listener);
    }

    public void addListener(int dimension, @Nonnull ChunkPos pos, @Nonnull IEventListener<T> listener) {
        addListener(dimension, pos.x, pos.z, listener);
    }

    public void addListener(int dimension, int chunkX, int chunkZ, @Nonnull IEventListener<T> listener) {
        long chunk = ChunkPos.asLong(chunkX, chunkZ);

        Map<Long, ConcurrentMap<IEventListener<T>, IEventListener<T>>> worldListeners = allListeners.get(dimension);
        if ( worldListeners == null ) {
            worldListeners = new Long2ObjectOpenHashMap<>();
            allListeners.put(dimension, worldListeners);
        }

        ConcurrentMap<IEventListener<T>, IEventListener<T>> listeners = worldListeners.get(chunk);
        if ( listeners == null ) {
            listeners = new MapMaker().weakKeys().weakValues().makeMap();
            worldListeners.put(chunk, listeners);
        }

        listeners.put(listener, listener);

        Map<Integer, Set<Long>> worldPositions = listenerPositions.get(listener);
        if ( worldPositions == null ) {
            worldPositions = new Int2ObjectOpenHashMap<>();
            listenerPositions.put(listener, worldPositions);
        }

        Set<Long> positions = worldPositions.get(dimension);
        if ( positions == null ) {
            positions = new LongOpenHashSet();
            worldPositions.put(dimension, positions);
        }

        positions.add(chunk);
    }

    public void removeListener(@Nonnull IEventListener<T> listener) {
        Map<Integer, Set<Long>> worldPositions = listenerPositions.get(listener);
        if ( worldPositions == null )
            return;

        listenerPositions.remove(listener);

        for (Map.Entry<Integer, Set<Long>> entry : worldPositions.entrySet()) {
            int dimension = entry.getKey();
            Map<Long, ConcurrentMap<IEventListener<T>, IEventListener<T>>> worldListeners = allListeners.get(dimension);
            if ( worldListeners != null ) {
                for (long chunk : entry.getValue()) {
                    ConcurrentMap<IEventListener<T>, IEventListener<T>> listeners = worldListeners.get(chunk);
                    if ( listeners != null ) {
                        listeners.remove(listener);
                        if ( listeners.isEmpty() )
                            worldListeners.remove(chunk);
                    }
                }

                if ( worldListeners.isEmpty() )
                    allListeners.remove(dimension);
            }
        }
    }

    public void removeListener(@Nonnull BlockPosDimension pos, @Nonnull IEventListener<T> listener) {
        removeListener(pos.getDimension(), pos, listener);
    }

    public void removeListener(int dimension, @Nonnull BlockPos pos, @Nonnull IEventListener<T> listener) {
        removeListener(dimension, pos.getX() >> 4, pos.getZ() >> 4, listener);
    }

    public void removeListener(int dimension, @Nonnull ChunkPos pos, @Nonnull IEventListener<T> listener) {
        removeListener(dimension, pos.x, pos.z, listener);
    }

    public void removeListener(int dimension, int chunkX, int chunkZ, @Nonnull IEventListener<T> listener) {
        Map<Long, ConcurrentMap<IEventListener<T>, IEventListener<T>>> worldListeners = allListeners.get(dimension);
        if ( worldListeners == null )
            return;

        long chunk = ChunkPos.asLong(chunkX, chunkZ);

        ConcurrentMap<IEventListener<T>, IEventListener<T>> listeners = worldListeners.get(chunk);
        if ( listeners == null )
            return;

        listeners.remove(listener);
        if ( listeners.isEmpty() ) {
            worldListeners.remove(chunk);
            if ( worldListeners.isEmpty() )
                allListeners.remove(dimension);
        }

        Map<Integer, Set<Long>> worldPositions = listenerPositions.get(listener);
        if ( worldPositions == null )
            return;

        Set<Long> positions = worldPositions.get(dimension);
        if ( positions != null ) {
            positions.remove(chunk);
            if ( positions.isEmpty() ) {
                worldPositions.remove(dimension);
                if ( worldPositions.isEmpty() )
                    listenerPositions.remove(listener);
            }
        }
    }

    public void dispatchEvent(T event) {
        if ( allListeners.isEmpty() || event == null )
            return;

        int dimension;
        long chunk;

        if ( event instanceof BlockEvent ) {
            BlockEvent be = (BlockEvent) event;
            World world = be.getWorld();
            if ( world == null || world.provider == null )
                return;

            dimension = world.provider.getDimension();
            BlockPos pos = be.getPos();
            chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);

        } else if ( event instanceof ChunkEvent ) {
            ChunkEvent ce = (ChunkEvent) event;
            World world = ce.getWorld();
            if ( world == null || world.provider == null )
                return;

            dimension = world.provider.getDimension();
            Chunk chunkObj = ce.getChunk();
            chunk = ChunkPos.asLong(chunkObj.x, chunkObj.z);

        } else if ( event instanceof WorldEvent ) {
            WorldEvent we = (WorldEvent) event;
            World world = we.getWorld();
            dimension = world.provider.getDimension();
            chunk = 0;

        } else
            return;

        Map<Long, ConcurrentMap<IEventListener<T>, IEventListener<T>>> worldListeners = allListeners.get(dimension);
        if ( worldListeners == null )
            return;

        ConcurrentMap<IEventListener<T>, IEventListener<T>> listeners = worldListeners.get(chunk);
        if ( listeners == null )
            return;

        for (IEventListener<T> listener : listeners.keySet())
            listener.handleEvent(event);
    }

    public interface IEventListener<T> {
        default void handleEvent(@Nonnull T event) {
        }
    }
}

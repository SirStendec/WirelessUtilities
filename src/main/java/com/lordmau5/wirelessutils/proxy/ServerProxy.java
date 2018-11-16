package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.wirelessutils.utils.EventDispatcher;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class ServerProxy extends CommonProxy {

    @SubscribeEvent
    public static void onPlaceBlock(BlockEvent.PlaceEvent event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.PLACE_BLOCK.dispatchEvent(event);
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.BREAK_BLOCK.dispatchEvent(event);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.CHUNK_LOAD.dispatchEvent(event);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.CHUNK_UNLOAD.dispatchEvent(event);
    }
}
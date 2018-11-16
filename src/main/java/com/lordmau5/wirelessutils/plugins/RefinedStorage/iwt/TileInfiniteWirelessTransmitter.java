package com.lordmau5.wirelessutils.plugins.RefinedStorage.iwt;

import com.raoulvdberge.refinedstorage.tile.TileNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileInfiniteWirelessTransmitter extends TileNode<NetworkNodeInfiniteWirelessTransmitter> {
    @Override
    @Nonnull
    public NetworkNodeInfiniteWirelessTransmitter createNode(World world, BlockPos pos) {
        return new NetworkNodeInfiniteWirelessTransmitter(world, pos);
    }

    @Override
    public String getNodeId() {
        return NetworkNodeInfiniteWirelessTransmitter.ID;
    }
}

package com.lordmau5.wirelessutils.plugins.RefinedStorage.iwt;

import com.raoulvdberge.refinedstorage.api.network.IWirelessTransmitter;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNode;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NetworkNodeInfiniteWirelessTransmitter extends NetworkNode implements IWirelessTransmitter {
    public static final String ID = "wirelessutils:infinite_wireless_transmitter";

    public NetworkNodeInfiniteWirelessTransmitter(World world, BlockPos pos) {
        super(world, pos);
    }

    @Override
    public int getEnergyUsage() {
        return 1000; // TODO: Config
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean canConduct(@Nullable EnumFacing direction) {
        return EnumFacing.DOWN.equals(direction);
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    public void visit(Operator operator) {
        operator.apply(world, pos.offset(EnumFacing.DOWN), EnumFacing.UP);
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE; // TODO: Config
    }

    @Override
    public BlockPos getOrigin() {
        return pos;
    }

    @Override
    public int getDimension() {
        return world.provider.getDimension();
    }
}

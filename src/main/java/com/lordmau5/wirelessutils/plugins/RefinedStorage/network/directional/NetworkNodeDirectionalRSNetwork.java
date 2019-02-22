package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.directional;

import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.NetworkNodeBase;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.TileRSNetworkBase;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class NetworkNodeDirectionalRSNetwork extends NetworkNodeBase {
    public static final String ID = "wirelessutils:directional_rs_network";

    public NetworkNodeDirectionalRSNetwork(World world, BlockPos pos) {
        super(world, pos);
    }

    @Override
    public int getEnergyUsage() {
        TileRSNetworkBase tile = (TileRSNetworkBase) world.getTileEntity(pos);
        if ( tile == null )
            return 0;

        return tile.getEnergyCost();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean shouldRebuildGraphOnChange() {
        return true;
    }

    @Override
    public boolean canConduct(@Nullable EnumFacing direction) {
        TileDirectionalRSNetwork tile = (TileDirectionalRSNetwork) world.getTileEntity(pos);
        if ( tile != null && tile.getEnumFacing() == direction ) {
            return false;
        }

        return super.canConduct(direction);
    }

    @Override
    public void visit(Operator operator) {
        super.visit(operator);

        TileDirectionalRSNetwork tile = (TileDirectionalRSNetwork) world.getTileEntity(pos);
        if ( tile != null && tile.redstoneControlOrDisable() ) {
            for (BlockPosDimension target : tile.getTargets()) {
                operator.apply(world, target, null);
            }
        }
    }
}

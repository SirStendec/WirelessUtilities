package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.positional;

import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.NetworkNodeBase;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.TileRSNetworkBase;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkNodePositionalRSNetwork extends NetworkNodeBase {
    public static final String ID = "wirelessutils:positional_rs_network";

    public NetworkNodePositionalRSNetwork(World world, BlockPos pos) {
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
    public void visit(Operator operator) {
        super.visit(operator);

        TilePositionalRSNetwork tile = (TilePositionalRSNetwork) world.getTileEntity(pos);
        if ( tile != null && tile.redstoneControlOrDisable() ) {
            for (Tuple<BlockPosDimension, ItemStack> target : tile.getTargets()) {
                operator.apply(world, target.getFirst(), null);
            }
        }
    }
}

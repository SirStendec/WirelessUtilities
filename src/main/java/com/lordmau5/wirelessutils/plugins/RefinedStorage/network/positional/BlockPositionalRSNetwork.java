package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.positional;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.TileRSNetworkBase;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPositionalRSNetwork extends BlockBaseMachine {

    public BlockPositionalRSNetwork() {
        super();

        setName("positional_rs_network");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePositionalRSNetwork();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if ( !world.isRemote ) {
            RefinedStoragePlugin.RSAPI.discoverNode(world, pos);

            TileRSNetworkBase tile = (TileRSNetworkBase) world.getTileEntity(pos);
            if ( tile != null ) {
                tile.calculateAndRebuild();
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        INetworkNodeManager manager = RefinedStoragePlugin.RSAPI.getNetworkNodeManager(world);

        INetworkNode node = manager.getNode(pos);

        manager.removeNode(pos);
        manager.markForSaving();

        if ( node != null && node.getNetwork() != null ) {
            node.getNetwork().getNodeGraph().rebuild();
        }

        super.breakBlock(world, pos, state);
    }
}

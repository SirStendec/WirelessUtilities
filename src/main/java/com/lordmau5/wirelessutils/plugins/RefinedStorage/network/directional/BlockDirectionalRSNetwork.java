package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.directional;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.TileRSNetworkBase;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeManager;
import com.raoulvdberge.refinedstorage.api.util.Action;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDirectionalRSNetwork extends BlockBaseMachine {

    public BlockDirectionalRSNetwork() {
        super();

        setName("directional_rs_network");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileDirectionalRSNetwork.class;
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
            node.getNetwork().getNodeGraph().invalidate(Action.PERFORM, node.getNetwork().world(), node.getNetwork().getPosition());
        }

        super.breakBlock(world, pos, state);
    }
}

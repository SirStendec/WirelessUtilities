package com.lordmau5.wirelessutils.plugins.XNet.network.directional;

import com.lordmau5.wirelessutils.block.base.BlockBaseDirectionalMachine;
import com.lordmau5.wirelessutils.plugins.XNet.network.base.TileXNetNetworkBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDirectionalXNetNetwork extends BlockBaseDirectionalMachine {

    public BlockDirectionalXNetNetwork() {
        super();

        setName("directional_xnet_network");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileDirectionalXNetNetwork();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if ( !world.isRemote ) {
            TileXNetNetworkBase tile = (TileXNetNetworkBase) world.getTileEntity(pos);
            if ( tile != null ) {
                tile.setNeedsRecalculation();
            }
        }
    }
}

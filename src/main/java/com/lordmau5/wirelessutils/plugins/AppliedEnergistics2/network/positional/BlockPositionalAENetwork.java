package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPositionalAENetwork extends BlockBaseMachine {

    public BlockPositionalAENetwork() {
        super();

        setName("positional_ae_network");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TilePositionalAENetwork.class;
    }


//    @Override
//    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
//        super.onBlockPlacedBy(world, pos, state, placer, stack);
//
//        if ( !world.isRemote ) {
//            TileAENetworkBase tile = (TileAENetworkBase) world.getTileEntity(pos);
//
//            if ( tile != null ) {
//                tile.setNeedsRecalculation();
//
//                if ( placer instanceof EntityPlayer ) {
//                    GameProfile profile = ((EntityPlayer) placer).getGameProfile();
//                    int playerID = AEApi.instance().registries().players().getID(profile);
//
//                    tile.getNode().setPlayerID(playerID);
//                }
//            }
//        }
//    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // TODO: Call unlink?

        super.breakBlock(world, pos, state);
    }
}

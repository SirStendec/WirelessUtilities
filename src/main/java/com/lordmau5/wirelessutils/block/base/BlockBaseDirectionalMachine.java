package com.lordmau5.wirelessutils.block.base;

import com.lordmau5.wirelessutils.tile.base.IFacing;
import com.lordmau5.wirelessutils.utils.EnumFacingRotation;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockBaseDirectionalMachine extends BlockBaseMachine {

    protected BlockBaseDirectionalMachine() {
        super();

        if ( hasSidedTransfer() )
            setDefaultState(blockState.getBaseState()
                    .withProperty(Properties.LEVEL, 0)
                    .withProperty(Properties.ACTIVE, false)
                    .withProperty(Properties.FACING_ROTATION, EnumFacingRotation.NORTH)
                    .withProperty(Properties.SIDES[0], false)
                    .withProperty(Properties.SIDES[1], false)
                    .withProperty(Properties.SIDES[2], false)
                    .withProperty(Properties.SIDES[3], false)
                    .withProperty(Properties.SIDES[4], false)
                    .withProperty(Properties.SIDES[5], false)
            );
        else
            setDefaultState(blockState.getBaseState()
                    .withProperty(Properties.LEVEL, 0)
                    .withProperty(Properties.ACTIVE, false)
                    .withProperty(Properties.FACING_ROTATION, EnumFacingRotation.NORTH)
            );
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getActualState(state, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof IFacing ) {
            IFacing facing = (IFacing) tile;

            EnumFacing side = facing.getEnumFacing();
            state = state.withProperty(Properties.FACING_ROTATION, EnumFacingRotation.fromFacing(side, (side == EnumFacing.DOWN || side == EnumFacing.UP) && facing.getRotationX()));
        }

        return state;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof IFacing ) {
            IFacing facing = (IFacing) tile;
            return facing.rotateBlock(axis);
        }

        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        if ( hasSidedTransfer() )
            return new BlockStateContainer(this, Properties.ACTIVE, Properties.LEVEL, Properties.FACING_ROTATION, Properties.SIDES[0], Properties.SIDES[1], Properties.SIDES[2], Properties.SIDES[3], Properties.SIDES[4], Properties.SIDES[5]);

        return new BlockStateContainer(this, Properties.ACTIVE, Properties.LEVEL, Properties.FACING_ROTATION);
    }
}

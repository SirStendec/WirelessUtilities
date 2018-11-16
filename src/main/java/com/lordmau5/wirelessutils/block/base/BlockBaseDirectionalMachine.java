package com.lordmau5.wirelessutils.block.base;

import com.lordmau5.wirelessutils.tile.base.IFacing;
import com.lordmau5.wirelessutils.utils.EnumFacingRotation;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockBaseDirectionalMachine extends BlockBaseMachine {

    public static final PropertyEnum<EnumFacingRotation> FACING = PropertyEnum.<EnumFacingRotation>create("facing", EnumFacingRotation.class);

    public BlockBaseDirectionalMachine() {
        super();

        setDefaultState(blockState.getBaseState().withProperty(Properties.LEVEL, 0).withProperty(Properties.ACTIVE, false).withProperty(FACING, EnumFacingRotation.NORTH));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getActualState(state, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof IFacing ) {
            IFacing facing = (IFacing) tile;

            EnumFacing side = facing.getEnumFacing();
            state = state.withProperty(FACING, EnumFacingRotation.fromFacing(side, (side == EnumFacing.DOWN || side == EnumFacing.UP) && facing.getRotationX()));
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
        return new BlockStateContainer(this, Properties.ACTIVE, Properties.LEVEL, FACING);
    }
}

package com.lordmau5.wirelessutils.block.redstone;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockPoweredRedstoneWire extends BlockRedstoneWire {
    public static final PropertyDirection FACING = Properties.FACING;

    public BlockPoweredRedstoneWire() {
        super();

        setTranslationKey(WirelessUtils.MODID + ".powered_redstone_wire");
        setRegistryName("powered_redstone_wire");
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, NORTH, EAST, SOUTH, WEST, POWER);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        EnumFacing facing = blockState.getValue(FACING);
        if ( facing == side )
            return 15;

        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);

        EnumFacing facing = state.getValue(FACING);
        worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this, false);

        addParticle(state, worldIn, pos, worldIn.rand);

        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        EnumFacing facing = state.getValue(FACING);
        worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this, false);
    }

    public int tickRate(World worldIn) {
        return 20;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        worldIn.setBlockState(pos, Blocks.REDSTONE_WIRE.getDefaultState());
    }

    public void addParticle(IBlockState state, World world, BlockPos pos, Random rand) {
        EnumFacing facing = state.getValue(FACING);
        boolean addMainAxis = facing.getAxisDirection().getOffset() < 0;
        EnumFacing.Axis axis = facing.getAxis();

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        switch (axis) {
            case X:
                x += addMainAxis ? 0.9 : 0.1;
                y += rand.nextDouble();
                z += rand.nextDouble();
                break;

            case Y:
                y += addMainAxis ? 0.9 : 0.1;
                x += rand.nextDouble();
                z += rand.nextDouble();
                break;

            default:
                z += addMainAxis ? 0.9 : 0.1;
                x += rand.nextDouble();
                y += rand.nextDouble();
        }

        world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0D, 0D, 0D);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        addParticle(stateIn, worldIn, pos, rand);
    }
}

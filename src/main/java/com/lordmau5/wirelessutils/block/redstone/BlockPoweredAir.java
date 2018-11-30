package com.lordmau5.wirelessutils.block.redstone;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockPoweredAir extends BlockAir {
    public static final PropertyDirection FACING = Properties.FACING;

    public BlockPoweredAir() {
        super();
        setTranslationKey(WirelessUtils.MODID + ".powered_air");
        setRegistryName("powered_air");
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Material getMaterial(IBlockState state) {
        return Material.CIRCUITS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @SuppressWarnings("deprecation")
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        EnumFacing facing = blockState.getValue(FACING);
        return facing == side ? 15 : 0;
    }

    @SuppressWarnings("deprecation")
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        EnumFacing facing = blockState.getValue(FACING);
        return facing == side ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return false;
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
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        worldIn.setBlockToAir(pos);
    }

    private void addParticle(IBlockState state, World world, BlockPos pos, Random rand) {
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

package com.lordmau5.wirelessutils.block;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class BlockDirectionalAir extends BlockAir {
    public static final PropertyDirection FACING = BlockDispenser.FACING;

    public BlockDirectionalAir() {
        super();
        setTranslationKey(WirelessUtils.MODID + ".directional_air");
        setRegistryName("directional_air");
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
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
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}

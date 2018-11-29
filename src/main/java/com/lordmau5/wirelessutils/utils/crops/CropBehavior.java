package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CropBehavior implements IHarvestBehavior {

    @Override
    public boolean appliesTo(Block block) {
        return block instanceof BlockCrops;
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        Block block = state.getBlock();
        return block instanceof BlockCrops && ((BlockCrops) block).isMaxAge(state);
    }

    @Override
    public boolean harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( silkTouch )
            return harvestByUsing(state, world, pos, fortune, desublimator);

        return harvestByBreaking(state, world, pos, fortune, desublimator);
    }
}

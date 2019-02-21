package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
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
    public HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return doHarvest(state, world, pos, silkTouch, fortune, desublimator) ?
                HarvestResult.SUCCESS : HarvestResult.FAILED;
    }

    private boolean doHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( silkTouch && ModConfig.augments.crop.useActivation )
            return harvestByUsing(state, world, pos, fortune, desublimator);

        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator);
    }
}

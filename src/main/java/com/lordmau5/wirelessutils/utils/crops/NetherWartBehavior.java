package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetherWartBehavior implements IHarvestBehavior {

    public boolean appliesTo(IBlockState state) {
        return state.getBlock() instanceof BlockNetherWart;
    }

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        return state.getBlock() instanceof BlockNetherWart && state.getValue(BlockNetherWart.AGE) >= 3;
    }

    public Tuple<HarvestResult, Integer> harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        return doHarvest(state, world, pos, silkTouch, fortune, desublimator) ? SUCCESS_ONE : FAILURE;
    }

    private boolean doHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( silkTouch && ModConfig.augments.crop.useActivation )
            return harvestByUsing(state, world, pos, fortune, desublimator);

        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator);
    }
}

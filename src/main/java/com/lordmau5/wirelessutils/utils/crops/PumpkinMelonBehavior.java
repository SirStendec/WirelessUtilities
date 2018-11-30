package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PumpkinMelonBehavior implements IHarvestBehavior {

    public boolean appliesTo(Block block) {
        return block instanceof BlockMelon || block instanceof BlockPumpkin;
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        Block block = state.getBlock();
        return block instanceof BlockMelon || block instanceof BlockPumpkin;
    }

    @Override
    public boolean harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator);
    }
}

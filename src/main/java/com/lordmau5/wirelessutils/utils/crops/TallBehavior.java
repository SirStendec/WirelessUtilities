package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TallBehavior implements IHarvestBehavior {

    public final Block target;

    public TallBehavior() {
        target = null;
    }

    public TallBehavior(Block target) {
        this.target = target;
    }

    @Override
    public boolean appliesTo(Block block) {
        return target == null ? (block instanceof BlockReed || block instanceof BlockCactus) : block == target;
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        IBlockState above = world.getBlockState(pos.up());
        IBlockState below = world.getBlockState(pos.down());
        Block block = state.getBlock();
        return appliesTo(block) && (above.getBlock() == block || below.getBlock() == block);
    }

    @Override
    public boolean harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        Block block = state.getBlock();
        IBlockState above = world.getBlockState(pos.up());
        boolean harvested = false;
        if ( above.getBlock() == block )
            harvested = harvest(above, world, pos.up(), silkTouch, fortune, desublimator);

        IBlockState below = world.getBlockState(pos.down());
        if ( below.getBlock() != block )
            return harvested;

        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) || harvested;
    }
}

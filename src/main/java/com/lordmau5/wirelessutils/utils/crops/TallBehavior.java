package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class TallBehavior implements IHarvestBehavior {

    public final Set<Block> targets;
    public final boolean harvestBottom;

    public boolean reverseHarvestOrder = false;
    public int minimumBlocks = 0;
    public int priority = 0;

    public TallBehavior() {
        targets = null;
        harvestBottom = false;
    }

    public TallBehavior(Block... targets) {
        this(ImmutableSet.copyOf(targets), false);
    }

    public TallBehavior(Set<Block> targets, boolean harvestBottom) {
        this(targets, harvestBottom, harvestBottom ? 0 : 2);
    }

    public TallBehavior(Set<Block> targets, int minimumBlocks) {
        this(targets, false, minimumBlocks);
    }

    public TallBehavior(Set<Block> targets, boolean harvestBottom, int minimumBlocks) {
        this.targets = targets;
        this.harvestBottom = harvestBottom;
        this.minimumBlocks = minimumBlocks;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(IBlockState state) {
        Block block = state.getBlock();
        if ( targets != null )
            return targets.contains(block);

        return (block instanceof BlockReed || block instanceof BlockCactus);
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        if ( !appliesTo(state) )
            return false;

        if ( blockLimit < minimumBlocks )
            return false;

        int i = 0;
        while ( appliesTo(state) && i < minimumBlocks ) {
            i++;
            pos = pos.up();
            state = world.getBlockState(pos);
        }

        return i >= minimumBlocks;
    }

    @Override
    public Tuple<HarvestResult, Integer> harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        int count = doHarvest(0, state, world, pos, silkTouch, fortune, blockLimit, desublimator);
        if ( count == 0 )
            return FAILURE;

        return new Tuple<>(HarvestResult.SUCCESS, count);
    }

    private int doHarvest(int i, IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return 0;

        IBlockState above = world.getBlockState(pos.up());
        int harvested = 0;

        if ( !reverseHarvestOrder && i < 255 && appliesTo(above) )
            harvested += doHarvest(i + 1, above, world, pos.up(), silkTouch, fortune, blockLimit - harvested, desublimator);

        if ( blockLimit - harvested < 1 )
            return harvested;

        IBlockState below = world.getBlockState(pos.down());
        if ( harvestBottom || appliesTo(below) ) {
            if ( harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) )
                harvested++;
        }

        if ( blockLimit - harvested < 1 )
            return harvested;

        if ( reverseHarvestOrder && i < 255 && appliesTo(above) )
            harvested = +doHarvest(i + 1, above, world, pos.up(), silkTouch, fortune, blockLimit - harvested, desublimator);

        return harvested;
    }
}

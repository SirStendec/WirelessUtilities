package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class MetaTallBehavior implements IHarvestBehavior {

    public final Set<IBlockState> targets;
    public final boolean harvestBottom;

    public int priority = 0;

    public MetaTallBehavior(IBlockState... targets) {
        this(ImmutableSet.copyOf(targets), false);
    }

    public MetaTallBehavior(Set<IBlockState> targets, boolean harvestBottom) {
        this.harvestBottom = harvestBottom;
        this.targets = targets;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(IBlockState state) {
        return targets.contains(state);
    }

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        if ( !harvestBottom && blockLimit == 1 )
            return false;

        return appliesTo(state) && (harvestBottom || appliesTo(world.getBlockState(pos.up())));
    }

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

        if ( i < 255 && targets.contains(above) )
            harvested += doHarvest(i + 1, above, world, pos.up(), silkTouch, fortune, blockLimit - harvested, desublimator);

        if ( blockLimit - harvested < 1 )
            return harvested;

        IBlockState below = world.getBlockState(pos.down());
        if ( !harvestBottom && !targets.contains(below) )
            return harvested;

        if ( harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) )
            harvested++;

        return harvested;
    }

}

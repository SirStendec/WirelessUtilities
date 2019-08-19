package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class MetaBreakBehavior implements IHarvestBehavior {

    public final Set<IBlockState> targets;

    public int priority = 0;

    public MetaBreakBehavior(IBlockState... targets) {
        this(ImmutableSet.copyOf(targets));
    }

    public MetaBreakBehavior(Set<IBlockState> targets) {
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

        return targets.contains(state);
    }

    public Tuple<HarvestResult, Integer> harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) ? SUCCESS_ONE : FAILURE;
    }
}

package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.state.IBlockState;
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

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        return appliesTo(state) && (harvestBottom || appliesTo(world.getBlockState(pos.up())));
    }

    public IHarvestBehavior.HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return doHarvest(0, state, world, pos, silkTouch, fortune, desublimator) ?
                IHarvestBehavior.HarvestResult.SUCCESS : IHarvestBehavior.HarvestResult.FAILED;
    }

    private boolean doHarvest(int i, IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        IBlockState above = world.getBlockState(pos.up());
        boolean harvested = false;

        if ( i < 255 && targets.contains(above) )
            harvested = doHarvest(i + 1, above, world, pos.up(), silkTouch, fortune, desublimator);

        IBlockState below = world.getBlockState(pos.down());
        if ( !harvestBottom && !targets.contains(below) )
            return harvested;

        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) || harvested;
    }

}

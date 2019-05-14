package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class SimpleBreakBehavior implements IHarvestBehavior {

    public final Set<Block> targets;

    public int priority = 0;

    public SimpleBreakBehavior() {
        this(Blocks.MELON_BLOCK, Blocks.PUMPKIN);
    }

    public SimpleBreakBehavior(Block... targets) {
        this(ImmutableSet.copyOf(targets));
    }

    public SimpleBreakBehavior(Set<Block> targets) {
        this.targets = targets;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(IBlockState state) {
        return targets.contains(state.getBlock());
    }

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        return appliesTo(state);
    }

    public HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) ?
                HarvestResult.SUCCESS : HarvestResult.FAILED;
    }
}

package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class TallCropBehavior implements IHarvestBehavior {

    public final Set<Block> targets;
    public final boolean silkAll;

    public boolean reverseHarvestOrder = false;
    public int minimumBlocks = 0;
    public int priority = 0;

    public TallCropBehavior(Block... targets) {
        this(ImmutableSet.copyOf(targets), false);
    }

    public TallCropBehavior(Block target, boolean silkAll) {
        this(ImmutableSet.of(target), silkAll);
    }

    public TallCropBehavior(Set<Block> targets, boolean silkAll) {
        this.silkAll = silkAll;
        this.targets = targets;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(IBlockState state) {
        return targets.contains(state.getBlock());
    }

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        Block block = state.getBlock();
        if ( !targets.contains(block) || world.getBlockState(pos.down()).getBlock() instanceof BlockCrops )
            return false;

        if ( blockLimit < minimumBlocks )
            return false;

        IBlockState workingState = state;
        BlockPos workingPos = pos;
        Block workingBlock = block;
        int i = 0;
        while ( targets.contains(workingBlock) && i < minimumBlocks ) {
            if ( i++ > 255 )
                break;

            if ( !(workingBlock instanceof BlockCrops) || !((BlockCrops) workingBlock).isMaxAge(workingState) )
                return false;

            workingPos = workingPos.up();
            workingState = world.getBlockState(workingPos);
            workingBlock = workingState.getBlock();
        }

        return i >= minimumBlocks;
    }

    public Tuple<HarvestResult, Integer> harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        int count = doHarvest(0, state, world, pos, silkTouch, fortune, blockLimit, desublimator);
        if ( count == 0 )
            return FAILURE;

        return new Tuple<>(HarvestResult.SUCCESS, count);
    }

    public int doHarvest(int i, IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return 0;

        Block block = state.getBlock();
        if ( !targets.contains(block) || !(block instanceof BlockCrops) || !((BlockCrops) block).isMaxAge(state) )
            return 0;

        int harvested = 0;
        BlockPos posAbove = pos.up();
        IBlockState above = world.getBlockState(posAbove);

        if ( !reverseHarvestOrder && i < 255 && (!silkTouch || !ModConfig.augments.crop.useActivation || silkAll) ) {
            int count = doHarvest(i + 1, above, world, posAbove, silkTouch, fortune, blockLimit - harvested, desublimator);
            if ( count > 0 ) {
                harvested += count;
                IBlockState newState = world.getBlockState(pos);
                if ( newState != state ) {
                    if ( blockLimit - harvested > 0 )
                        harvested += doHarvest(i + 1, newState, world, pos, silkTouch, fortune, blockLimit - harvested, desublimator);
                    return harvested;
                }
            }
        }

        if ( blockLimit - harvested < 1 )
            return harvested;

        if ( silkTouch && ModConfig.augments.crop.useActivation ) {
            if ( harvestByUsing(state, world, pos, fortune, desublimator) )
                harvested++;
        } else {
            if ( harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) )
                harvested++;
        }

        if ( blockLimit - harvested < 1 )
            return harvested;

        if ( reverseHarvestOrder && i < 255 && (!silkTouch || !ModConfig.augments.crop.useActivation || silkAll) )
            harvested += doHarvest(i + 1, above, world, posAbove, silkTouch, fortune, blockLimit - harvested, desublimator);

        return harvested;
    }
}

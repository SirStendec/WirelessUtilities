package com.lordmau5.wirelessutils.utils.crops;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
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

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        Block block = state.getBlock();
        if ( !targets.contains(block) || world.getBlockState(pos.down()).getBlock() instanceof BlockCrops )
            return false;

        IBlockState workingState = state;
        BlockPos workingPos = pos;
        Block workingBlock = block;
        int i = 0;
        while ( targets.contains(workingBlock) ) {
            if ( i++ > 255 )
                break;

            if ( !(workingBlock instanceof BlockCrops) || !((BlockCrops) workingBlock).isMaxAge(workingState) )
                return false;

            workingPos = workingPos.up();
            workingState = world.getBlockState(workingPos);
            workingBlock = workingState.getBlock();
        }

        if ( i < minimumBlocks )
            return false;

        return true;
    }

    public HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return doHarvest(0, state, world, pos, silkTouch, fortune, desublimator) ?
                HarvestResult.SUCCESS : HarvestResult.FAILED;
    }

    public boolean doHarvest(int i, IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        Block block = state.getBlock();
        if ( !targets.contains(block) || !(block instanceof BlockCrops) || !((BlockCrops) block).isMaxAge(state) )
            return false;

        boolean harvested = false;
        BlockPos posAbove = pos.up();
        IBlockState above = world.getBlockState(posAbove);

        if ( !reverseHarvestOrder && i < 255 && (!silkTouch || !ModConfig.augments.crop.useActivation || silkAll) ) {
            harvested = doHarvest(i + 1, above, world, posAbove, silkTouch, fortune, desublimator);
            if ( harvested ) {
                IBlockState newState = world.getBlockState(pos);
                if ( newState != state ) {
                    doHarvest(i + 1, newState, world, pos, silkTouch, fortune, desublimator);
                    return true;
                }
            }
        }

        if ( silkTouch && ModConfig.augments.crop.useActivation )
            harvested = harvestByUsing(state, world, pos, fortune, desublimator) || harvested;
        else
            harvested = harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator) || harvested;

        if ( reverseHarvestOrder && i < 255 && (!silkTouch || !ModConfig.augments.crop.useActivation || silkAll) )
            harvested = doHarvest(i + 1, above, world, posAbove, silkTouch, fortune, desublimator) || harvested;

        return harvested;
    }
}

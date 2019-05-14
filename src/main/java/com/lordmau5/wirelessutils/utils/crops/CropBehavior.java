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

public class CropBehavior implements IHarvestBehavior {

    public Set<Block> targets;
    public int priority = 0;

    public CropBehavior(Block... targets) {
        this(ImmutableSet.copyOf(targets));
    }

    public CropBehavior(Set<Block> targets) {
        this.targets = targets;
    }

    public CropBehavior() {
        this.targets = null;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean appliesTo(IBlockState state) {
        Block block = state.getBlock();
        if ( targets != null )
            return targets.contains(block);

        return block instanceof BlockCrops;
    }

    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        Block block = state.getBlock();
        if ( targets != null && !targets.contains(block) )
            return false;

        return block instanceof BlockCrops && ((BlockCrops) block).isMaxAge(state);
    }

    public HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return doHarvest(state, world, pos, silkTouch, fortune, desublimator) ?
                HarvestResult.SUCCESS : HarvestResult.FAILED;
    }

    private boolean doHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( silkTouch && ModConfig.augments.crop.useActivation )
            return harvestByUsing(state, world, pos, fortune, desublimator);

        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator);
    }
}

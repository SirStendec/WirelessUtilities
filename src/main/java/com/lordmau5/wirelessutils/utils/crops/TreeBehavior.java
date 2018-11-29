package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import java.util.HashSet;
import java.util.List;

public class TreeBehavior implements IHarvestBehavior {

    private final ItemStack SHEARS = new ItemStack(Items.SHEARS);

    @Override
    public boolean appliesTo(Block block) {
        return block instanceof BlockLog;
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        Block block = state.getBlock();
        return block.isWood(world, pos);
    }

    @Override
    public boolean harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return harvest(state, world, pos, silkTouch, fortune, desublimator, 0, new HashSet<>());
    }

    private boolean harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator, int count, HashSet<BlockPos> targets) {
        targets.add(pos);
        Block block = state.getBlock();
        if ( !block.isWood(world, pos) && !block.isLeaves(state, world, pos) )
            return false;

        if ( silkTouch && block instanceof IShearable ) {
            List<ItemStack> drops = ((IShearable) block).onSheared(SHEARS.copy(), world, pos, 0);
            desublimator.insertAll(drops);
            world.setBlockToAir(pos);

        } else
            harvestByBreaking(state, world, pos, fortune, desublimator);

        if ( count < 100 ) {
            for (EnumFacing face : EnumFacing.VALUES) {
                BlockPos offset = pos.offset(face, 1);
                if ( targets.contains(offset) )
                    continue;

                IBlockState offState = world.getBlockState(offset);
                harvest(offState, world, offset, silkTouch, fortune, desublimator, count + 1, targets);
            }
        }

        return true;
    }
}

package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import java.util.*;

public class TreeBehavior implements IHarvestBehavior {

    private Map<Integer, Map<BlockPos, TreeCache>> trees = new Int2ObjectOpenHashMap<>();

    private final ItemStack SHEARS = new ItemStack(Items.SHEARS);

    @Override
    public boolean appliesTo(Block block) {
        return block instanceof BlockLog;
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( world == null )
            return false;

        int dimension = world.provider.getDimension();
        Map<BlockPos, TreeCache> worldTrees = trees.get(dimension);
        if ( worldTrees != null ) {
            TreeCache cache = worldTrees.get(pos);
            if ( cache != null )
                return cache.origin == pos;
        }

        Block block = state.getBlock();
        if ( block.isWood(world, pos) || block.isLeaves(state, world, pos) ) {
            // Make sure that we only harvest trees starting at the bottom, for efficiency.
            BlockPos below = pos.down();
            IBlockState belowState = world.getBlockState(below);
            Block belowBlock = belowState.getBlock();
            if ( belowBlock.isWood(world, below) || belowBlock.isLeaves(belowState, world, below) )
                return false;

            TreeCache cache = new TreeCache(world, pos);
            cache.scan();

            if ( worldTrees == null ) {
                worldTrees = new Object2ObjectOpenHashMap<>();
                trees.put(dimension, worldTrees);
            }

            for (BlockPos basePos : cache.base)
                if ( !worldTrees.containsKey(basePos) )
                    worldTrees.put(basePos, cache);

            return true;
        }

        return false;
    }

    @Override
    public HarvestResult harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        if ( world == null )
            return HarvestResult.FAILED;

        int dimension = world.provider.getDimension();
        Map<BlockPos, TreeCache> worldTrees = trees.get(dimension);
        TreeCache cache = worldTrees == null ? null : worldTrees.get(pos);
        if ( cache == null )
            return HarvestResult.FAILED;

        int count = 0;

        while ( !cache.blocks.isEmpty() ) {
            count++;
            if ( count > ModConfig.augments.crop.treeBlocksPerTick )
                return HarvestResult.PROGRESS;

            BlockPos current = cache.blocks.poll();
            if ( current == null )
                break;

            IBlockState currentState = world.getBlockState(current);
            Block block = currentState.getBlock();
            if ( block != cache.log && !block.isLeaves(currentState, world, current) )
                continue;

            if ( silkTouch && block instanceof IShearable ) {
                List<ItemStack> drops = ((IShearable) block).onSheared(SHEARS.copy(), world, current, 0);
                if ( !desublimator.canInsertAll(drops) ) {
                    cache.blocks.add(current);
                    return HarvestResult.PROGRESS;
                }

                desublimator.insertAll(drops);

                if ( ModConfig.augments.crop.treeEffects )
                    world.playEvent(null, 2001, current, Block.getStateId(currentState));
                world.setBlockToAir(current);

            } else if ( !harvestByBreaking(currentState, world, current, silkTouch, fortune, desublimator, true) ) {
                cache.blocks.add(current);
                return HarvestResult.PROGRESS;
            }

            // If we managed to break the origin, kill the tree since we won't be back.
            // The tree SHOULD be effectively done at this point.
            if ( current == cache.origin )
                break;
        }

        for (BlockPos basePos : cache.base) {
            if ( worldTrees.get(basePos) == cache )
                worldTrees.remove(basePos);
        }

        return HarvestResult.HUGE_SUCCESS;
    }

    public static class TreeCache {
        private final World world;
        public final BlockPos origin;

        public final Set<BlockPos> base;
        public final Queue<BlockPos> blocks;

        public Block log = null;

        public TreeCache(World world, BlockPos origin) {
            this.world = world;
            this.origin = origin;

            base = new HashSet<>();
            blocks = new PriorityQueue<>(Comparator.comparingDouble(value -> ((BlockPos) value).distanceSq(origin)).reversed());

            base.add(origin);
            blocks.add(origin);
        }

        public void scan() {
            scan(origin, 0, new HashSet<>());
        }

        public void scan(BlockPos pos, int depth, Set<BlockPos> visited) {
            visited.add(pos);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if ( block != log ) {
                if ( block.isWood(world, pos) && log == null )
                    log = block;
                else if ( !block.isLeaves(state, world, pos) )
                    return;
            }

            blocks.add(pos);

            if ( pos.getY() == origin.getY() )
                base.add(pos);

            if ( depth < ModConfig.augments.crop.treeScanDepth ) {
                for (BlockPos offset : BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 1, 1))) {
                    if ( !visited.contains(offset) )
                        scan(offset, depth + 1, visited);
                }
            }
        }
    }

}

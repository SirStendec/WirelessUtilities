package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class TreeBehavior implements IHarvestBehavior {

    private Map<Integer, Map<BlockPosDimension, TreeCache>> trees = new Int2ObjectOpenHashMap<>();

    private final ItemStack SHEARS = new ItemStack(Items.SHEARS);

    public boolean appliesTo(IBlockState state) {
        return state.getBlock() instanceof BlockLog;
    }

    @Override
    public int getBlockEstimate(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( !canHarvest(state, world, pos, silkTouch, fortune, blockLimit, desublimator) )
            return 0;

        BlockPosDimension machinePos = desublimator.getPosition();
        int dimension = world.provider.getDimension();
        Map<BlockPosDimension, TreeCache> worldTrees = trees.get(dimension);
        if ( worldTrees == null )
            return 0;

        TreeCache cache = worldTrees.get(machinePos);
        if ( cache == null )
            return 0;

        int count = cache.blocks.size() + cache.leaves.size();
        return Math.min(ModConfig.augments.crop.treeBlocksPerTick, count);
    }

    @Override
    public boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( world == null || desublimator == null )
            return false;

        if ( TileBaseDesublimator.isBlacklisted(state) )
            return false;

        Block block = state.getBlock();
        if ( !block.isWood(world, pos) && !block.isLeaves(state, world, pos) )
            return false;

        BlockPosDimension machinePos = desublimator.getPosition();
        int dimension = world.provider.getDimension();
        Map<BlockPosDimension, TreeCache> worldTrees = trees.get(dimension);
        if ( worldTrees == null ) {
            worldTrees = new Object2ObjectOpenHashMap<>();
            trees.put(dimension, worldTrees);
        }

        TreeCache cache = worldTrees.get(machinePos);
        if ( cache == null ) {
            cache = new TreeCache(world, machinePos);
            worldTrees.put(machinePos, cache);
        }

        if ( cache.base.contains(pos) )
            return true;

        BlockPos below = pos.down();
        IBlockState belowState = world.getBlockState(below);
        Block belowBlock = belowState.getBlock();
        if ( belowBlock.isWood(world, below) || belowBlock.isLeaves(belowState, world, below) )
            return false;

        cache.scan(pos);
        return true;
    }

    public Tuple<HarvestResult, Integer> harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        if ( world == null || desublimator == null )
            return FAILURE;

        BlockPosDimension machinePos = desublimator.getPosition();
        int dimension = world.provider.getDimension();
        Map<BlockPosDimension, TreeCache> worldTrees = trees.get(dimension);
        TreeCache cache = worldTrees == null ? null : worldTrees.get(machinePos);
        if ( cache == null )
            return FAILURE;

        int count = 0;

        while ( !cache.leaves.isEmpty() || !cache.blocks.isEmpty() ) {
            count++;
            if ( count > ModConfig.augments.crop.treeBlocksPerTick || count > blockLimit )
                return new Tuple<>(HarvestResult.PROGRESS, count - 1);

            BlockPos current = cache.leaves.poll();
            boolean isLeaf = true;
            if ( current == null ) {
                current = cache.blocks.poll();
                isLeaf = false;

                if ( current == null )
                    break;
            }

            IBlockState currentState = world.getBlockState(current);
            if ( TileBaseDesublimator.isBlacklisted(currentState) )
                continue;

            Block block = currentState.getBlock();
            if ( !cache.logs.contains(block) && !block.isLeaves(currentState, world, current) )
                continue;

            if ( silkTouch && block instanceof IShearable ) {
                List<ItemStack> drops = ((IShearable) block).onSheared(SHEARS.copy(), world, current, 0);
                if ( !desublimator.canInsertAll(drops) ) {
                    if ( isLeaf )
                        cache.leaves.add(current);
                    else
                        cache.blocks.add(current);

                    return new Tuple<>(HarvestResult.PROGRESS, count - 1);
                }

                desublimator.insertAll(drops);

                if ( ModConfig.augments.crop.treeEffects )
                    world.playEvent(null, 2001, current, Block.getStateId(currentState));
                world.setBlockToAir(current);

            } else if ( !harvestByBreaking(currentState, world, current, silkTouch, fortune, desublimator, true) ) {
                if ( isLeaf )
                    cache.leaves.add(current);
                else
                    cache.blocks.add(current);

                return new Tuple<>(HarvestResult.PROGRESS, count - 1);
            }

            // Remove this from the list of known bases.
            cache.base.remove(current);
        }

        worldTrees.remove(machinePos);
        return new Tuple<>(HarvestResult.HUGE_SUCCESS, count);
    }

    public static class TreeCache {
        private final World world;

        public final Set<BlockPos> base;
        public final Queue<BlockPos> leaves;
        public final Queue<BlockPos> blocks;

        public final Set<Block> logs;

        public TreeCache(World world, BlockPos origin) {
            this.world = world;

            logs = new HashSet<>();
            base = new HashSet<>();
            blocks = new PriorityQueue<>(Comparator.comparingDouble(value -> ((BlockPos) value).distanceSq(origin)).reversed());
            leaves = new PriorityQueue<>(Comparator.comparingDouble(value -> ((BlockPos) value).distanceSq(origin)).reversed());
        }

        public void scan(BlockPos pos) {
            scan(pos, pos, 0, new HashSet<>());
        }

        public void scan(BlockPos pos, BlockPos origin, int depth, Set<BlockPos> visited) {
            visited.add(pos);

            IBlockState state = world.getBlockState(pos);
            if ( TileBaseDesublimator.isBlacklisted(state) )
                return;

            Block block = state.getBlock();
            boolean isLeaf = block.isLeaves(state, world, pos);

            if ( !isLeaf && !logs.contains(block) ) {
                if ( block.isWood(world, pos) && pos == origin )
                    logs.add(block);
                else
                    return;
            }

            if ( isLeaf && !leaves.contains(pos) )
                leaves.add(pos);
            else if ( !isLeaf && !blocks.contains(pos) )
                blocks.add(pos);

            if ( pos == origin )
                base.add(pos);

            if ( depth < ModConfig.augments.crop.treeScanDepth ) {
                for (BlockPos offset : BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 1, 1))) {
                    if ( !visited.contains(offset) )
                        scan(offset, origin, depth + 1, visited);
                }
            }
        }
    }

}

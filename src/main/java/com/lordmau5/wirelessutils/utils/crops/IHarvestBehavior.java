package com.lordmau5.wirelessutils.utils.crops;

import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.InventoryHelper;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public interface IHarvestBehavior {

    enum HarvestResult {
        HUGE_SUCCESS, // It's hard to overstate my satisfaction.
        SUCCESS,
        PROGRESS,
        FAILED
    }

    Tuple<HarvestResult, Integer> SUCCESS_ONE = new Tuple<>(HarvestResult.SUCCESS, 1);
    Tuple<HarvestResult, Integer> FAILURE = new Tuple<>(HarvestResult.FAILED, 0);

    default int getPriority() {
        return 0;
    }

    default int getBlockEstimate(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator) {
        return 1;
    }

    boolean appliesTo(IBlockState state);

    boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator);

    Tuple<HarvestResult, Integer> harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, int blockLimit, TileBaseDesublimator desublimator);

    default boolean harvestByUsing(IBlockState state, World world, BlockPos pos, int fortune, TileBaseDesublimator desublimator) {
        NonNullList<ItemStack> drops = NonNullList.create();
        Block block = state.getBlock();
        block.getDrops(drops, world, pos, state, fortune);
        if ( !desublimator.canInsertAll(drops) )
            return false;

        FakePlayer player = WUFakePlayer.getFakePlayer(world, pos.up());
        player.inventory.clear();
        state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
        ForgeHooks.onRightClickBlock(player, EnumHand.MAIN_HAND, pos, EnumFacing.UP, new Vec3d(0, 0, 0));

        List<EntityItem> entities = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos));
        if ( entities != null && !entities.isEmpty() ) {
            IItemHandler handler = desublimator.getCapabilityHandler();
            for (EntityItem entity : entities) {
                ItemStack stack = entity.getItem();
                stack = InventoryHelper.insertStackIntoInventory(handler, stack, false);
                entity.setItem(stack);
            }
        }

        desublimator.insertAll(player.inventory.mainInventory);
        for (ItemStack item : player.inventory.mainInventory) {
            if ( item.isEmpty() )
                continue;

            CoreUtils.dropItemStackIntoWorldWithVelocity(item, world, pos);
        }

        player.inventory.clear();
        return true;
    }

    default boolean harvestByBreaking(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator) {
        return harvestByBreaking(state, world, pos, silkTouch, fortune, desublimator, false);
    }

    default boolean harvestByBreaking(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator, boolean isTree) {
        Block block = state.getBlock();
        NonNullList<ItemStack> drops = NonNullList.create();

        FakePlayer player = WUFakePlayer.getFakePlayer(world, pos.up());
        boolean silky;
        if ( silkTouch && block.canSilkHarvest(world, pos, state, player) ) {
            Item item = Item.getItemFromBlock(block);
            int i = 0;
            if ( item.getHasSubtypes() )
                i = block.getMetaFromState(state);

            drops.add(new ItemStack(item, 1, i));
            silky = true;
        } else {
            block.getDrops(drops, world, pos, state, fortune);
            silky = false;
        }

        BlockEvent.HarvestDropsEvent event = new BlockEvent.HarvestDropsEvent(world, pos, state, fortune, 1F, drops, player, silky);
        MinecraftForge.EVENT_BUS.post(event);

        List<ItemStack> finalDrops = event.getDrops();
        if ( finalDrops != null && !finalDrops.isEmpty() ) {
            if ( !desublimator.canInsertAll(finalDrops) )
                return false;

            desublimator.insertAll(finalDrops);
        }

        if ( isTree ? ModConfig.augments.crop.treeEffects : ModConfig.augments.crop.useEffects )
            world.playEvent(null, 2001, pos, Block.getStateId(state));

        world.setBlockToAir(pos);
        return true;
    }
}

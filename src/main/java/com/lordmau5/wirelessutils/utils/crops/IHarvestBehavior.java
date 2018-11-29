package com.lordmau5.wirelessutils.utils.crops;

import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.InventoryHelper;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public interface IHarvestBehavior {

    default int getPriority() {
        return 0;
    }

    boolean appliesTo(Block block);

    boolean canHarvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator);

    boolean harvest(IBlockState state, World world, BlockPos pos, boolean silkTouch, int fortune, TileBaseDesublimator desublimator);

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

    default boolean harvestByBreaking(IBlockState state, World world, BlockPos pos, int fortune, TileBaseDesublimator desublimator) {
        Block block = state.getBlock();
        NonNullList<ItemStack> drops = NonNullList.create();
        block.getDrops(drops, world, pos, state, fortune);
        if ( desublimator.canInsertAll(drops) ) {
            desublimator.insertAll(drops);
            world.setBlockToAir(pos);
            return true;
        }

        return false;
    }
}

package com.lordmau5.wirelessutils.plugins.RefinedStorage.iwt;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.raoulvdberge.refinedstorage.block.BlockCable;
import com.raoulvdberge.refinedstorage.block.BlockNode;
import com.raoulvdberge.refinedstorage.block.info.BlockInfoBuilder;
import com.raoulvdberge.refinedstorage.render.collision.CollisionGroup;
import com.raoulvdberge.refinedstorage.render.constants.ConstantsWirelessTransmitter;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockInfiniteWirelessTransmitter extends BlockNode {
    // From BlockTorch
    private static final AxisAlignedBB INFINITE_WIRELESS_TRANSMITTER_AABB = new AxisAlignedBB(0.4000000059604645D, 0.0D, 0.4000000059604645D, 0.6000000238418579D, 0.6000000238418579D, 0.6000000238418579D);

    private static final String id = "infinite_wireless_transmitter"; // TODO: Better way to handle this

    public BlockInfiniteWirelessTransmitter() {
        super(BlockInfoBuilder.forMod(WirelessUtils.instance, WirelessUtils.MODID, id).tileEntity(TileInfiniteWirelessTransmitter::new).create());
        setCreativeTab(WirelessUtils.creativeTabCU);
    }

    @Override
    public String getTranslationKey() {
        return "tile." + WirelessUtils.MODID + "." + id;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        Item item = Item.getItemFromBlock(this);
        ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if ( !canPlaceBlockAt(world, pos) && world.getBlockState(pos).getBlock() == this ) {
            dropBlockAsItem(world, pos, state, 0);

            world.setBlockToAir(pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return INFINITE_WIRELESS_TRANSMITTER_AABB;
    }

    @Override
    public List<CollisionGroup> getCollisions(TileEntity tile, IBlockState state) {
        return Collections.singletonList(ConstantsWirelessTransmitter.COLLISION);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
//        TileEntity tile = world.getTileEntity(pos.offset(EnumFacing.DOWN));
//
//        if ( tile != null && tile.hasCapability(RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY, EnumFacing.UP) ) {
//            INetworkNodeProxy proxy = tile.getCapability(RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY, EnumFacing.UP);
//
//            if ( proxy != null && proxy.getNode() instanceof INetworkNodeCable ) {
//                return true;
//            }
//        }

        return world.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock() instanceof BlockCable;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasConnectedState() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        tooltip.add(I18n.format("block.refinedstorage:wireless_transmitter.tooltip", TextFormatting.WHITE + I18n.format("block.refinedstorage:cable.name") + TextFormatting.GRAY));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}

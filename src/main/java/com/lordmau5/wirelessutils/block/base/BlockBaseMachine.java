package com.lordmau5.wirelessutils.block.base;

import cofh.api.tileentity.IInventoryRetainer;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import com.lordmau5.wirelessutils.utils.crafting.INBTPreservingIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockBaseMachine extends BlockBaseTile implements IInventoryRetainer, INBTPreservingIngredient {

    protected BlockBaseMachine() {
        super();

        setHardness(5F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( tab != this.getCreativeTab() )
            return;

        Level[] levels = Level.values();

        for (int i = 0; i < levels.length; i++)
            items.add(new ItemStack(this, 1, i));
    }

    @Override
    public boolean isValidForCraft(IRecipe recipe, InventoryCrafting craft, ItemStack stack, ItemStack output) {
        return Block.getBlockFromItem(output.getItem()) == this;
    }

    /* Block State */

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, Properties.ACTIVE, Properties.LEVEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof TileEntityBaseMachine ) {
            TileEntityBaseMachine machine = (TileEntityBaseMachine) tile;
            state = state.withProperty(Properties.ACTIVE, machine.isActive);
        }

        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(Properties.LEVEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        if ( meta < 0 )
            meta = 0;

        if ( meta >= Level.values().length )
            meta = Level.values().length - 1;

        return this.getDefaultState().withProperty(Properties.LEVEL, meta);
    }

    /* Block <-> Item Conversion */

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if ( te instanceof TileEntityBaseMachine ) {
            TileEntityBaseMachine machine = (TileEntityBaseMachine) te;
            machine.setLevel(stack.getMetadata());

            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null ) {
                if ( tag.hasKey("BlockEntityTag") )
                    tag = tag.getCompoundTag("BlockEntityTag");

                machine.readAugmentsFromNBT(tag);
            }

            machine.updateAugmentStatus();

            if ( tag != null )
                machine.readExtraFromNBT(tag);
        }

        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public NBTTagCompound getItemStackTag(IBlockAccess world, BlockPos pos) {
        NBTTagCompound tag = super.getItemStackTag(world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if ( !(tile instanceof TileEntityBaseMachine) )
            return tag;

        TileEntityBaseMachine machine = (TileEntityBaseMachine) tile;

        tag.setBoolean("Configured", true);

        machine.writeAugmentsToNBT(tag);
        machine.writeExtraToNBT(tag);

        return tag;
    }

    @Override
    public boolean retainInventory() {
        return true;
    }

    @Override
    public boolean onBlockActivatedDelegate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if ( stack.getItem() == Items.BED && stack.getDisplayName().equalsIgnoreCase("debug") ) {
            TileEntityBaseMachine machine = (TileEntityBaseMachine) world.getTileEntity(pos);
            if ( machine != null ) {
                machine.debugPrint();
                return true;
            }
        }

        return super.onBlockActivatedDelegate(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    /* Rendering */

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }
}

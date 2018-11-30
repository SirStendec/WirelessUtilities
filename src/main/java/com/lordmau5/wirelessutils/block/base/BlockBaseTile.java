package com.lordmau5.wirelessutils.block.base;

import cofh.api.block.IConfigGui;
import cofh.core.block.BlockCoreTile;
import cofh.core.block.TileNameable;
import cofh.core.util.RayTracer;
import cofh.core.util.helpers.ItemHelper;
import cofh.core.util.helpers.ServerHelper;
import cofh.core.util.helpers.WrenchHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBaseTile extends BlockCoreTile implements IConfigGui {

    protected final boolean standardGui = true;
    protected final boolean configGui = false;

    protected BlockBaseTile() {
        this(Material.IRON);
    }

    protected BlockBaseTile(Material material) {
        super(material, WirelessUtils.MODID);
        setSoundType(SoundType.METAL);
        setCreativeTab(WirelessUtils.creativeTabCU);
    }

    public void setName(String name) {
        setTranslationKey(name);
        setRegistryName(name);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        Item item = Item.getItemFromBlock(this);
        ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        Item item = Item.getItemFromBlock(this);
        if ( item == Items.AIR )
            return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
        stack.setTagCompound(getBasicItemStackTag(world, pos));
        return stack;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.addAll(dropDelegate(getBasicItemStackTag(world, pos), world, pos, fortune));
    }

    public NBTTagCompound getBasicItemStackTag(IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound tag = new NBTTagCompound();

        if ( tile instanceof TileNameable && !((TileNameable) tile).customName.isEmpty() )
            tag = ItemHelper.setItemStackTagName(tag, ((TileNameable) tile).customName);

        if ( tag.isEmpty() )
            return null;

        return tag;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof TileNameable )
            ((TileNameable) tile).setCustomName(ItemHelper.getNameFromItemStack(stack));

        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        RayTraceResult ray = RayTracer.retrace(player);
        if ( ray == null )
            return false;

        PlayerInteractEvent event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, side, ray.hitVec);
        if ( MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Event.Result.DENY )
            return false;

        if ( player.isSneaking() ) {
            if ( WrenchHelper.isHoldingUsableWrench(player, ray) ) {
                if ( ServerHelper.isServerWorld(world) && canDismantle(world, pos, state, player) ) {
                    dismantleBlock(world, pos, state, player, false);
                    WrenchHelper.usedWrench(player, ray);
                }
                return true;
            }
        }

        TileNameable tile = (TileNameable) world.getTileEntity(pos);
        if ( tile == null || tile.isInvalid() )
            return false;

        if ( WrenchHelper.isHoldingUsableWrench(player, ray) ) {
            if ( tile.canPlayerAccess(player) ) {
                if ( ServerHelper.isServerWorld(world) )
                    tile.onWrench(player, side);

                WrenchHelper.usedWrench(player, ray);
            }

            return true;
        }

        if ( onBlockActivatedDelegate(world, pos, state, player, hand, side, hitX, hitY, hitZ) )
            return true;

        if ( standardGui && ServerHelper.isServerWorld(world) )
            return tile.openGui(player);

        return standardGui;
    }

    public boolean onBlockActivatedDelegate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public boolean openConfigGui(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        TileNameable tile = (TileNameable) world.getTileEntity(pos);
        if ( tile == null || tile.isInvalid() )
            return false;

        if ( configGui && ServerHelper.isServerWorld(world) )
            return tile.openConfigGui(player);

        return configGui;
    }

    @Override
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileNameable tile = (TileNameable) world.getTileEntity(pos);
        if ( tile instanceof TileEntityBaseMachine && ((TileEntityBaseMachine) tile).isCreative() && !player.capabilities.isCreativeMode )
            return false;

        return super.canDismantle(world, pos, state, player);
    }
}

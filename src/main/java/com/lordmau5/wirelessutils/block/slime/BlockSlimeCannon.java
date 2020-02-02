package com.lordmau5.wirelessutils.block.slime;

import cofh.api.tileentity.IRedstoneControl;
import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseTile;
import com.lordmau5.wirelessutils.block.base.ISneakActivatedBlock;
import com.lordmau5.wirelessutils.item.base.IJEIInformationItem;
import com.lordmau5.wirelessutils.item.base.ItemBaseAreaCard;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.TileSlimeCannon;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import mezz.jei.api.IModRegistry;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockSlimeCannon extends BlockBaseTile implements IJEIInformationItem, ISneakActivatedBlock {

    private static final PropertyInteger COUNT = PropertyInteger.create("count", 0, 5);
    private static final PropertyInteger SPEED = PropertyInteger.create("speed", 0, 4);

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1F, 0.1875F, 1F);

    public BlockSlimeCannon() {
        super(Material.ROCK);

        setSoundType(SoundType.STONE);
        setHardness(3.5F);
        setName("slime_cannon");

        setDefaultState(blockState.getBaseState().withProperty(COUNT, 1).withProperty(SPEED, 0));
    }

    @Override
    public void registerJEI(IModRegistry registry) {
        IJEIInformationItem.addJEIInformation(registry, new ItemStack(this), "tab." + WirelessUtils.MODID + ".slime_cannon");
    }

    @SuppressWarnings("deprecation")
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress(IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COUNT, SPEED);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return blockState.getBaseState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileSlimeCannon tile = (TileSlimeCannon) worldIn.getTileEntity(pos);
        if ( tile != null ) {
            state = state.withProperty(COUNT, tile.getCount() + 1);
            state = state.withProperty(SPEED, tile.getSpeed());
        }

        return state;
    }

    @Override
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if ( face == EnumFacing.DOWN )
            return BlockFaceShape.SOLID;

        return BlockFaceShape.UNDEFINED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if ( side != EnumFacing.DOWN )
            return false;

        return super.isSideSolid(base_state, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN;
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileSlimeCannon.class;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntity te = world.getTileEntity(pos);
        if ( te instanceof TileSlimeCannon && placer instanceof EntityPlayer ) {
            TileSlimeCannon tile = (TileSlimeCannon) te;

            tile.setControl(IRedstoneControl.ControlMode.LOW);
            tile.setAngles(45F, placer.rotationYaw);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);

        entityIn.motionX *= 0.4D;
        entityIn.motionZ *= 0.4D;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);

        TileSlimeCannon tile = (TileSlimeCannon) world.getTileEntity(pos);
        if ( tile == null || !tile.isActive || rand.nextFloat() > 0.5 )
            return;

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.25D;
        double z = pos.getZ() + 0.5D;

        world.spawnParticle(EnumParticleTypes.SLIME, x, y, z, 0, 0, 0);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return super.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileSlimeCannon tile = (TileSlimeCannon) world.getTileEntity(pos);
        if ( tile == null )
            return false;

        float yaw = tile.getYaw() + 90F;
        if ( yaw > 359F )
            yaw -= 360F;

        tile.setYaw(yaw);
        return true;
    }

    @Override
    public boolean onBlockSneakActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, Vec3d hit) {
        return onBlockActivatedDelegate(world, pos, state, player, hand, side, (float) hit.x, (float) hit.y, (float) hit.z);
    }

    @Override
    public boolean onBlockActivatedDelegate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileSlimeCannon tile = (TileSlimeCannon) world.getTileEntity(pos);
        if ( tile == null )
            return false;

        final ItemStack stack = player.getHeldItem(hand);
        final Item item = stack.getItem();
        final boolean sneaking = player.isSneaking();

        if ( item instanceof ItemBasePositionalCard ) {
            if ( world.isRemote )
                return true;

            final BlockPosDimension here = new BlockPosDimension(pos, world);
            BlockPosDimension target = null;
            if ( item instanceof ItemBaseAreaCard ) {
                final BlockPosDimension center = ((ItemBaseAreaCard) item).getTarget(stack, here);
                final Tuple<BlockPosDimension, BlockPosDimension> corners = center == null ? null : ((ItemBaseAreaCard) item).getCorners(stack, center);
                if ( corners != null ) {
                    final BlockPosDimension first = corners.getFirst();
                    final BlockPosDimension second = corners.getSecond();

                    final int rangeX = 1 + Math.max(0, second.getX() - first.getX());
                    final int rangeY = 1 + Math.max(0, second.getY() - first.getY());
                    final int rangeZ = 1 + Math.max(0, second.getZ() - first.getZ());

                    target = new BlockPosDimension(
                            first.getX() + world.rand.nextInt(rangeX),
                            first.getY() + world.rand.nextInt(rangeY),
                            first.getZ() + world.rand.nextInt(rangeZ),
                            first.getDimension(),
                            first.getFacing()
                    );
                }
            } else
                target = ((ItemBasePositionalCard) item).getTarget(stack, here);

            if ( target != null ) {
                if ( target.getDimension() != world.provider.getDimension() ) {
                    player.sendMessage(new TextComponentTranslation("info." + WirelessUtils.MODID + ".cannon.no_range"));
                    return true;
                }

                // We fire from the center of our block.
                Vec2f angles = tile.calculateTrajectory(target, target.getFacing(), !sneaking);

                // If it's null, the target isn't within range.
                if ( angles == null ) {
                    if ( tile.getVelocity() < ModConfig.blocks.slimeCannon.maxVelocity ) {
                        angles = tile.calculateTrajectory(target, target.getFacing(), (float) ModConfig.blocks.slimeCannon.maxVelocity, !sneaking);
                        if ( angles != null ) {
                            player.sendMessage(new TextComponentTranslation("info." + WirelessUtils.MODID + ".cannon.no_range_velocity"));
                            return true;
                        }
                    }

                    player.sendMessage(new TextComponentTranslation("info." + WirelessUtils.MODID + ".cannon.no_range"));
                    return true;
                }

                tile.setYaw(angles.x);
                tile.setPitch(angles.y);
                tile.sendModePacket();

                player.sendMessage(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".cannon.set",
                        new TextComponentTranslation(
                                "info." + WirelessUtils.MODID + ".blockpos.basic",
                                TextHelpers.getComponent(target.getX()),
                                TextHelpers.getComponent(target.getY()),
                                TextHelpers.getComponent(target.getZ())
                        ),
                        new TextComponentTranslation(
                                "info." + WirelessUtils.MODID + ".degrees",
                                TextHelpers.getComponent(Math.round(tile.getYaw()))
                        ),
                        new TextComponentTranslation(
                                "info." + WirelessUtils.MODID + ".degrees",
                                TextHelpers.getComponent(Math.round(tile.getPitch()))
                        )
                ));

            } else
                player.sendMessage(new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".player_positional_card.invalid.unset"
                ));

            return true;
        }

        if ( item == Items.REDSTONE ) {
            if ( world.isRemote )
                return true;

            if ( sneaking && tile.decrementSpeed() )
                CoreUtils.dropItemStackIntoWorldWithVelocity(new ItemStack(Items.REDSTONE), world, pos);
            else if ( !sneaking && tile.incrementSpeed() ) {
                if ( !player.isCreative() )
                    stack.shrink(1);
            }

            return true;
        }

        if ( item == Items.SLIME_BALL ) {
            if ( world.isRemote )
                return true;

            int count = tile.getCount();
            if ( sneaking ) {
                tile.dropCountMod();
                tile.setCount(0);
            } else if ( count != -1 ) {
                if ( count != 0 )
                    tile.dropCountMod();

                tile.setCount(-1);
                if ( !player.isCreative() )
                    stack.shrink(1);
            }

            return true;
        }

        if ( item == Items.GLOWSTONE_DUST ) {
            if ( world.isRemote )
                return true;

            int count = tile.getCount();
            if ( sneaking ) {
                if ( count > 1 ) {
                    tile.setCount(count - 1);
                    CoreUtils.dropItemStackIntoWorldWithVelocity(new ItemStack(Items.GLOWSTONE_DUST), world, pos);
                } else {
                    tile.dropCountMod();
                    tile.setCount(0);
                }
            } else if ( count < 4 ) {
                if ( count == -1 ) {
                    tile.dropCountMod();
                    tile.setCount(0);
                    count = 0;
                }

                tile.setCount(count + 1);
                if ( !player.isCreative() )
                    stack.shrink(1);
            }

            return true;
        }

        return false;
    }
}

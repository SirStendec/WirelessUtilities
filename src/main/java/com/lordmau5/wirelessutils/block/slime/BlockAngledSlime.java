package com.lordmau5.wirelessutils.block.slime;

import cofh.core.util.RayTracer;
import cofh.core.util.helpers.WrenchHelper;
import com.lordmau5.wirelessutils.block.base.BlockBase;
import com.lordmau5.wirelessutils.tile.TileAngledSlime;
import com.lordmau5.wirelessutils.utils.EnumOmniFacing;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

public class BlockAngledSlime extends BlockBase {
    public static final PropertyEnum<EnumOmniFacing> ATTACHED = PropertyEnum.create("attached", EnumOmniFacing.class);
    public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);

    public static final AxisAlignedBB INTERNAL_BOX = new AxisAlignedBB(-0.4375, -0.4375, -0.4375, 0.4375, 0.4375, 0.4375);

    public static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375);

    public BlockAngledSlime() {
        super(Material.CLAY, MapColor.GREEN);

        setSoundType(SoundType.SLIME);
        setDefaultSlipperiness(0.8F);
        setHardness(1.0F);
        setName("angled_slime");
        setDefaultState(blockState.getBaseState().withProperty(ROTATION, 0).withProperty(ATTACHED, EnumOmniFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ATTACHED, ROTATION);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ATTACHED).ordinal();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return blockState.getBaseState()
                .withProperty(ATTACHED, EnumOmniFacing.values()[meta]);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileAngledSlime tile = (TileAngledSlime) worldIn.getTileEntity(pos);
        if ( tile != null )
            state = state.withProperty(ROTATION, tile.getRotation());

        return state;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        EnumOmniFacing facing = state.getValue(ATTACHED);

        world.setBlockState(pos, state.withProperty(ATTACHED, facing.rotateAround(side.getAxis())));
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileAngledSlime();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        RayTraceResult ray = RayTracer.retrace(player);
        if ( ray == null )
            return false;

        PlayerInteractEvent event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, side, ray.hitVec);
        if ( MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Event.Result.DENY )
            return false;

        /*ItemStack stack = player.getHeldItem(hand);
        if ( stack.getItem() == Items.BED && stack.getDisplayName().equalsIgnoreCase("debug") ) {
            if ( world.isRemote ) {
                state = getActualState(state, world, pos);
                drawRays(state, world, pos, side);
                drawCube(state, world, pos);
            }

            return true;
        }*/

        if ( WrenchHelper.isHoldingUsableWrench(player, ray) )
            return false;

        TileAngledSlime tile = (TileAngledSlime) world.getTileEntity(pos);
        if ( tile != null && player.getHeldItem(hand).isEmpty() ) {
            if ( world.isRemote )
                world.playSound(player, pos, SoundEvents.BLOCK_SLIME_HIT, SoundCategory.BLOCKS, 1F, player.isSneaking() ? 0.75F : 1F);

            EnumOmniFacing attached = state.getValue(ATTACHED);
            if ( side == attached.facing.getOpposite() ) {
                world.setBlockState(pos, state.withProperty(ATTACHED, attached.rotateAround(side.getAxis())));
            } else {
                tile.rotate(player.isSneaking() ? -1 : 1);
            }
            return true;
        }

        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        // TODO: Logic for deciding if we should place in alternate rotation.
        boolean is_rot = false;
        return blockState.getBaseState().withProperty(ATTACHED, EnumOmniFacing.fromFacing(facing.getOpposite(), is_rot));
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        IBlockState state = worldIn.getBlockState(pos);

        if ( entityIn.isSneaking() || state.getValue(ATTACHED).facing == EnumFacing.UP )
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        else
            entityIn.fall(fallDistance, 0.2F);
    }

    /*@SideOnly(Side.CLIENT)
    public void drawRays(IBlockState stateIn, World worldIn, BlockPos pos) {
        for (EnumFacing face : EnumFacing.values()) {
            drawRays(stateIn, worldIn, pos, face);
        }
    }

    @SideOnly(Side.CLIENT)
    public void drawCube(IBlockState state, World world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        AxisAlignedBB bb = INTERNAL_BOX;

        Vector3d cornerA = new Vector3d(bb.minX, bb.minY, bb.minZ);
        Vector3d cornerB = new Vector3d(bb.maxX, bb.minY, bb.minZ);
        Vector3d cornerC = new Vector3d(bb.minX, bb.minY, bb.maxZ);
        Vector3d cornerD = new Vector3d(bb.maxX, bb.minY, bb.maxZ);
        Vector3d cornerE = new Vector3d(bb.minX, bb.maxY, bb.minZ);
        Vector3d cornerF = new Vector3d(bb.maxX, bb.maxY, bb.minZ);
        Vector3d cornerG = new Vector3d(bb.minX, bb.maxY, bb.maxZ);
        Vector3d cornerH = new Vector3d(bb.maxX, bb.maxY, bb.maxZ);

        Matrix4d transformation = getTransformation(state);
        if ( transformation == null )
            return;

        transformation.invert();

        transformation.transform(cornerA);
        transformation.transform(cornerB);
        transformation.transform(cornerC);
        transformation.transform(cornerD);
        transformation.transform(cornerE);
        transformation.transform(cornerF);
        transformation.transform(cornerG);
        transformation.transform(cornerH);

        Vec3d a = new Vec3d(cornerA.x + x, cornerA.y + y, cornerA.z + z);
        Vec3d b = new Vec3d(cornerB.x + x, cornerB.y + y, cornerB.z + z);
        Vec3d c = new Vec3d(cornerC.x + x, cornerC.y + y, cornerC.z + z);
        Vec3d d = new Vec3d(cornerD.x + x, cornerD.y + y, cornerD.z + z);
        Vec3d e = new Vec3d(cornerE.x + x, cornerE.y + y, cornerE.z + z);
        Vec3d f = new Vec3d(cornerF.x + x, cornerF.y + y, cornerF.z + z);
        Vec3d g = new Vec3d(cornerG.x + x, cornerG.y + y, cornerG.z + z);
        Vec3d h = new Vec3d(cornerH.x + x, cornerH.y + y, cornerH.z + z);

        drawParticleLine(EnumParticleTypes.FLAME, world, a, b, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, a, e, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, e, f, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, b, f, 10);

        drawParticleLine(EnumParticleTypes.FLAME, world, a, c, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, b, d, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, e, g, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, f, h, 10);

        drawParticleLine(EnumParticleTypes.FLAME, world, c, g, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, c, d, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, g, h, 10);
        drawParticleLine(EnumParticleTypes.FLAME, world, d, h, 10);
    }

    @SideOnly(Side.CLIENT)
    public void drawRays(IBlockState stateIn, World worldIn, BlockPos pos, EnumFacing face) {
        BlockPos fromPos = pos.offset(face, 2);
        Vec3d from = new Vec3d(fromPos.getX() + 0.5, fromPos.getY() + 0.5, fromPos.getZ() + 0.5);
        Vec3d velocity = new Vec3d(pos.getX() - fromPos.getX(), pos.getY() - fromPos.getY(), pos.getZ() - fromPos.getZ());

        RayTraceResult ray = worldIn.rayTraceBlocks(from, from.add(velocity.x, velocity.y, velocity.z), false);
        if ( ray == null ) {
            drawParticleLine(EnumParticleTypes.SMOKE_NORMAL, worldIn, from, from.add(velocity), 10);
            return;
        }

        AxisAlignedBB no = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
        AngledBounceResult result = tryBounce(from, no, velocity, ray, stateIn, worldIn);
        if ( result == null ) {
            drawParticleLine(EnumParticleTypes.REDSTONE, worldIn, from, ray.hitVec, 10);
            return;
        }

        drawParticleLine(EnumParticleTypes.SMOKE_NORMAL, worldIn, from, result.hitPos, 10);
        Vec3d target = result.hitPos.add(result.velocity);
        drawParticleLine(EnumParticleTypes.REDSTONE, worldIn, result.hitPos, target, 10);
    }

    public void drawParticleLine(EnumParticleTypes type, World world, Vec3d posA, Vec3d posB, int count) {
        double step = posA.distanceTo(posB) / count;

        double stepX = (posB.x - posA.x) / count;
        double stepY = (posB.y - posA.y) / count;
        double stepZ = (posB.z - posA.z) / count;

        for (int x = 0; x < count; x++) {
            world.spawnParticle(type, posA.x, posA.y, posA.z, 0, 0, 0);
            posA = posA.add(stepX, stepY, stepZ);
        }
    }*/

    @Override
    public void onLanded(World worldIn, Entity entityIn) {
        if ( entityIn.isSneaking() )
            super.onLanded(worldIn, entityIn);
        else if ( entityIn.motionY < 0 ) {
            Vec3d entPos = entityIn.getPositionVector();
            RayTraceResult ray = worldIn.rayTraceBlocks(entPos, entPos.add(0, -5, 0));
            if ( ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK ) {
                super.onLanded(worldIn, entityIn);
                return;
            }

            BlockPos pos = ray.getBlockPos();
            IBlockState state = worldIn.getBlockState(pos);
            if ( state.getBlock() != this ) {
                super.onLanded(worldIn, entityIn);
                return;
            }

            state = getActualState(state, worldIn, pos);
            AngledBounceResult result = tryBounce(
                    entityIn.getPositionVector(),
                    entityIn.getEntityBoundingBox(),
                    new Vec3d(entityIn.motionX, entityIn.motionY, entityIn.motionZ),
                    ray,
                    state);

            if ( result == null ) {
                super.onLanded(worldIn, entityIn);
                return;
            }

            // We ignore setting the position and only set the velocity.
            entityIn.motionX = result.velocity.x;
            entityIn.motionY = result.velocity.y;
            entityIn.motionZ = result.velocity.z;

            if ( !(entityIn instanceof EntityLivingBase) )
                entityIn.motionY *= 0.8D;
        }
    }

    public static class AngledBounceResult {
        public final Vec3d hitPos;
        public final Vec3d velocity;

        public AngledBounceResult(Vec3d hitPos, Vec3d velocity) {
            this.hitPos = hitPos;
            this.velocity = velocity;
        }
    }

    public static Matrix4d getTransformation(IBlockState state) {
        EnumOmniFacing attachment = state.getValue(ATTACHED);
        EnumFacing.Axis axis = getAxis(attachment);
        int rotState = state.getValue(ROTATION);

        double rotation;
        double rotationX = 0;
        double rotationY = 0;
        double rotationZ = 0;

        if ( rotState == 0 || rotState > 3 ) {
            Matrix4d out = new Matrix4d();
            out.setIdentity();
            return out;
        }

        if ( attachment == EnumOmniFacing.EAST || attachment == EnumOmniFacing.EAST_ROT || attachment == EnumOmniFacing.SOUTH_ROT || attachment == EnumOmniFacing.WEST_ROT || attachment == EnumOmniFacing.NORTH_ROT ) {
            if ( rotState == 1 )
                rotation = 22.5;
            else if ( rotState == 2 )
                rotation = 45;
            else
                rotation = 67.5;

        } else if ( attachment == EnumOmniFacing.SOUTH || attachment == EnumOmniFacing.WEST || attachment == EnumOmniFacing.DOWN_ROT || attachment == EnumOmniFacing.UP_ROT ) {
            if ( rotState == 1 )
                rotation = -22.5;
            else if ( rotState == 2 )
                rotation = -45;
            else
                rotation = -67.5;

        } else if ( attachment == EnumOmniFacing.NORTH || attachment == EnumOmniFacing.DOWN || attachment == EnumOmniFacing.UP ) {
            if ( rotState == 1 )
                rotation = -22.5;
            else if ( rotState == 2 )
                rotation = 45;
            else
                rotation = -67.5;

        } else
            return null;

        Quat4d quat = new Quat4d(0, 0, 0, 1),
                q1 = new Quat4d();

        if ( axis == EnumFacing.Axis.X )
            rotationX = Math.toRadians(rotation);
        else if ( axis == EnumFacing.Axis.Y )
            rotationY = Math.toRadians(rotation);
        else if ( axis == EnumFacing.Axis.Z )
            rotationZ = Math.toRadians(rotation);

        q1.set(0, Math.sin(rotationY / 2), 0, Math.cos(rotationY / 2));
        quat.mul(q1);

        q1.set(Math.sin(rotationX / 2), 0, 0, Math.cos(rotationX / 2));
        quat.mul(q1);

        q1.set(0, 0, Math.sin(rotationZ / 2), Math.cos(rotationZ / 2));
        quat.mul(q1);

        return new Matrix4d(
                quat,
                new Vector3d(0, 0, 0),
                1
        );
    }

    public AngledBounceResult tryBounce(Vec3d position, AxisAlignedBB boundingBox, Vec3d velocity, RayTraceResult ray, IBlockState state) {
        return tryBounce(position, boundingBox, velocity, ray, state, null);
    }

    public AngledBounceResult tryBounce(Vec3d position, AxisAlignedBB boundingBox, Vec3d velocity, RayTraceResult ray, IBlockState state, World world) {
        EnumOmniFacing attachment = state.getValue(ATTACHED);
        EnumFacing.Axis axis = getAxis(attachment);
        if ( ray == null || ray.sideHit == null || attachment == null || ray.sideHit == attachment.facing || ray.sideHit.getAxis() == axis )
            return null;

        BlockPos pos = ray.getBlockPos();

        Matrix4d transformation = getTransformation(state);
        if ( transformation == null )
            return null;

        // Now that we have a transformation matrix, we need to normalize the
        // incoming position and velocity, and then transform them to align them
        // to our rotated axis for easier maths.

        double offsetX = pos.getX() + 0.5D;
        double offsetY = pos.getY() + 0.5D;
        double offsetZ = pos.getZ() + 0.5D;

        Vector4d tPosition = new Vector4d(position.x - offsetX, position.y - offsetY, position.z - offsetZ, 1);
        Vector4d tVelocity = new Vector4d(velocity.x, velocity.y, velocity.z, 1);

        transformation.transform(tPosition);
        transformation.transform(tVelocity);

        // Now that we've done that, we calculate an intersection between using our
        // axis aligned bounding box.

        Vec3d alignedPos = new Vec3d(tPosition.x, tPosition.y, tPosition.z);
        Vec3d alignedSecond = new Vec3d(tPosition.x + tVelocity.x, tPosition.y + tVelocity.y, tPosition.z + tVelocity.z);

        /*if ( world != null && world.isRemote )
            drawParticleLine(EnumParticleTypes.CLOUD, world, alignedPos.add(offsetX, offsetY, offsetZ), alignedSecond.add(offsetX, offsetY, offsetZ), 10);*/

        RayTraceResult result = INTERNAL_BOX.calculateIntercept(alignedPos, alignedSecond);
        if ( result == null )
            return null;

        // If we got a result from that, then we have a hit! Bounce! This needs to account
        // for the size of the bounding box of the colliding entity for accuracy. This math
        // probably isn't perfect, but it might be good enough.

        tPosition.x = result.hitVec.x;
        tPosition.y = result.hitVec.y;
        tPosition.z = result.hitVec.z;

        int direction = result.sideHit.getAxisDirection().getOffset();
        EnumFacing.Axis hitAxis = result.sideHit.getAxis();
        if ( hitAxis == EnumFacing.Axis.X ) {
            tPosition.x += direction * (boundingBox.maxX - boundingBox.minX);
            tVelocity.x = -tVelocity.x;
        } else if ( hitAxis == EnumFacing.Axis.Y ) {
            tPosition.y += direction * (boundingBox.maxY - boundingBox.minY);
            tVelocity.y = -tVelocity.y;
        } else {
            tPosition.z += direction * (boundingBox.maxZ - boundingBox.minZ);
            tVelocity.z = -tVelocity.z;
        }

        // Finally, now that we've bounced, apply the inverse of our transformation
        // matrix to the results to put them back into world space. Then, return them!

        /*if ( world != null && world.isRemote )
            drawParticleLine(EnumParticleTypes.FLAME, world, new Vec3d(tPosition.x + offsetX, tPosition.y + offsetY, tPosition.z + offsetZ), new Vec3d(tPosition.x + offsetX + tVelocity.x, tPosition.y + offsetY + tVelocity.y, tPosition.z + offsetZ + tVelocity.z), 10);*/

        transformation.invert();

        transformation.transform(tPosition);
        transformation.transform(tVelocity);

        return new AngledBounceResult(
                new Vec3d(tPosition.x + offsetX, tPosition.y + offsetY, tPosition.z + offsetZ),
                new Vec3d(tVelocity.x, tVelocity.y, tVelocity.z)
        );
    }

    public static EnumFacing.Axis getAxis(EnumOmniFacing attachment) {
        switch (attachment) {
            case SOUTH:
            case NORTH:
            case UP:
            case DOWN:
                return EnumFacing.Axis.X;
            case EAST:
            case WEST:
            case UP_ROT:
            case DOWN_ROT:
                return EnumFacing.Axis.Z;
            case NORTH_ROT:
            case EAST_ROT:
            case SOUTH_ROT:
            case WEST_ROT:
            default:
                return EnumFacing.Axis.Y;
        }
    }
}

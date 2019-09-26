package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.MoreObjects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Iterator;
import java.util.List;

@Immutable
public class BlockPosDimension extends BlockPos {
    private static final Logger LOGGER = LogManager.getLogger();

    private final int dimension;
    private final EnumFacing facing;

    @Nullable
    public static BlockPosDimension fromTag(NBTTagCompound tag) {
        if ( tag == null || !tag.hasKey("position") || !tag.hasKey("dimension") )
            return null;

        return new BlockPosDimension(
                BlockPos.fromLong(tag.getLong("position")),
                tag.getInteger("dimension"),
                tag.hasKey("facing") ? EnumFacing.byIndex(tag.getByte("facing")) : null
        );
    }

    @Nullable
    public static BlockPosDimension fromTileEntity(@Nonnull TileEntity entity) {
        return BlockPosDimension.fromTileEntity(entity, null);
    }

    @Nullable
    public static BlockPosDimension fromTileEntity(@Nonnull TileEntity entity, @Nullable EnumFacing facing) {
        BlockPos pos = entity.getPos();
        World world = entity.getWorld();
        if ( pos == null || world == null || world.provider == null )
            return null;

        return new BlockPosDimension(pos, world.provider.getDimension(), facing);
    }

    @Nonnull
    public static BlockPosDimension fromChunk(int x, int z, int dimension) {
        return new BlockPosDimension(x << 4, 1, z << 4, dimension);
    }

    @Nonnull
    public static BlockPosDimension fromChunk(int x, int z, int dimension, @Nullable EnumFacing facing) {
        return new BlockPosDimension(x << 4, 1, z << 4, dimension, facing);
    }

    @Nonnull
    public static BlockPosDimension fromChunk(@Nonnull ChunkPos pos, int dimension) {
        return new BlockPosDimension(pos.getXStart(), 1, pos.getZStart(), dimension);
    }

    @Nonnull
    public static BlockPosDimension fromChunk(@Nonnull ChunkPos pos, int dimension, @Nullable EnumFacing facing) {
        return new BlockPosDimension(pos.getXStart(), 1, pos.getZStart(), dimension, facing);
    }

    @Nonnull
    public static BlockPosDimension fromChunk(@Nonnull Chunk chunk, int dimension) {
        return new BlockPosDimension(chunk.x << 4, 1, chunk.z << 4, dimension);
    }

    @Nonnull
    public static BlockPosDimension fromChunk(@Nonnull Chunk chunk, int dimension, @Nullable EnumFacing facing) {
        return new BlockPosDimension(chunk.x << 4, 1, chunk.z << 4, dimension, facing);
    }

    public BlockPosDimension(@Nonnull BlockPosDimension pos) {
        this(pos, pos.getDimension(), pos.getFacing());
    }

    public BlockPosDimension(int x, int y, int z, int dimension) {
        this(x, y, z, dimension, null);
    }

    public BlockPosDimension(int x, int y, int z, int dimension, @Nullable EnumFacing facing) {
        super(x, y, z);

        this.dimension = dimension;
        this.facing = facing;
    }

    public BlockPosDimension(double x, double y, double z, int dimension) {
        this(x, y, z, dimension, null);
    }

    public BlockPosDimension(double x, double y, double z, int dimension, @Nullable EnumFacing facing) {
        super(x, y, z);

        this.dimension = dimension;
        this.facing = facing;
    }

    public BlockPosDimension(@Nonnull BlockPos pos, int dimension) {
        this(pos, dimension, null);
    }

    public BlockPosDimension(@Nonnull BlockPos pos, int dimension, @Nullable EnumFacing facing) {
        super(pos);

        this.dimension = dimension;
        this.facing = facing;
    }

    @Nonnull
    public BlockPosDimension facing(@Nullable EnumFacing side) {
        return side == getFacing() ? this : new BlockPosDimension(this, getDimension(), side);
    }

    @Override
    @Nonnull
    public BlockPosDimension add(double x, double y, double z) {
        return x == 0D && y == 0D && z == 0D ? this : new BlockPosDimension(
                getX() + x,
                getY() + y,
                getZ() + z,
                getDimension(),
                getFacing()
        );
    }

    @Override
    @Nonnull
    public BlockPosDimension add(@Nonnull Vec3i vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    @Nonnull
    @Override
    public BlockPosDimension add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPosDimension(
                getX() + x,
                getY() + y,
                getZ() + z,
                getDimension(),
                getFacing());
    }

    @Override
    @Nonnull
    public BlockPosDimension subtract(@Nonnull Vec3i vec) {
        return add(-vec.getX(), -vec.getY(), -vec.getZ());
    }

    @Override
    @Nonnull
    public BlockPosDimension up() {
        return up(1);
    }

    @Override
    @Nonnull
    public BlockPosDimension up(int n) {
        return offset(EnumFacing.UP, n);
    }

    @Override
    @Nonnull
    public BlockPosDimension down() {
        return down(1);
    }

    @Override
    @Nonnull
    public BlockPosDimension down(int n) {
        return offset(EnumFacing.DOWN, n);
    }

    @Override
    @Nonnull
    public BlockPosDimension north() {
        return north(1);
    }

    @Override
    @Nonnull
    public BlockPosDimension north(int n) {
        return offset(EnumFacing.NORTH, n);
    }

    @Override
    @Nonnull
    public BlockPosDimension south() {
        return south(1);
    }

    @Override
    @Nonnull
    public BlockPosDimension south(int n) {
        return offset(EnumFacing.SOUTH, n);
    }

    @Override
    @Nonnull
    public BlockPosDimension west() {
        return west(1);
    }

    @Override
    @Nonnull
    public BlockPosDimension west(int n) {
        return offset(EnumFacing.WEST, n);
    }

    @Override
    @Nonnull
    public BlockPosDimension east() {
        return east(1);
    }

    @Override
    @Nonnull
    public BlockPosDimension east(int n) {
        return offset(EnumFacing.EAST, n);
    }

    @Override
    @Nonnull
    public BlockPosDimension rotate(Rotation rotationIn) {
        switch (rotationIn) {
            case NONE:
            default:
                return this;
            case CLOCKWISE_90:
                return new BlockPosDimension(-getZ(), getY(), getX(), getDimension(), getFacing());
            case CLOCKWISE_180:
                return new BlockPosDimension(-getX(), getY(), -getZ(), getDimension(), getFacing());
            case COUNTERCLOCKWISE_90:
                return new BlockPosDimension(getZ(), getY(), -getX(), getDimension(), getFacing());
        }
    }

    @Override
    @Nonnull
    public BlockPosDimension crossProduct(@Nonnull Vec3i vec) {
        return new BlockPosDimension(
                getY() * vec.getZ() - getZ() * getY(),
                getZ() * vec.getX() - getX() * getZ(),
                getX() * getY() - getY() * getX(),
                getDimension(),
                getFacing()
        );
    }

    public boolean isInsideBorders() {
        World world = DimensionManager.getWorld(getDimension(), false);
        if ( world == null )
            return true;

        WorldBorder border = world.getWorldBorder();
        return border != null && border.contains(this);
    }

    public boolean isInsideWorld() {
        World world = DimensionManager.getWorld(getDimension(), false);
        if ( world == null )
            return true;

        if ( !world.isValid(this) )
            return false;

        WorldBorder border = world.getWorldBorder();
        return border == null || border.contains(this);
    }

    @Nonnull
    public BlockPosDimension clipToWorld() {
        return clipToWorld(true);
    }

    @Nonnull
    public BlockPosDimension clipToWorld(boolean includeBorders) {
        World world = DimensionManager.getWorld(getDimension(), false);
        if ( world == null )
            return this;

        int x = getX();
        int y = getY();
        int z = getZ();

        if ( world.isOutsideBuildHeight(this) ) {
            if ( y < 0 )
                y = 0;
            else if ( y >= world.getHeight() )
                y = world.getHeight() - 1;
        }

        if ( includeBorders ) {
            WorldBorder border = world.getWorldBorder();
            if ( border != null && !border.contains(this) ) {
                if ( x < border.minX() )
                    x = (int) border.minX();
                else if ( x > border.maxX() )
                    x = (int) border.maxX();
                if ( z < border.minZ() )
                    z = (int) border.minZ();
                else if ( z > border.maxZ() )
                    z = (int) border.maxZ();
            }
        }

        if ( x == getX() && y == getY() && z == getZ() )
            return this;

        return new BlockPosDimension(
                x, y, z,
                getDimension(),
                getFacing()
        );
    }

    @Nonnull
    @Override
    public BlockPosDimension offset(@Nonnull EnumFacing facing) {
        return offset(facing, 1);
    }

    @Nonnull
    @Override
    public BlockPosDimension offset(@Nonnull EnumFacing direction, int n) {
        return n == 0 ? this : new BlockPosDimension(
                getX() + direction.getXOffset() * n,
                getY() + direction.getYOffset() * n,
                getZ() + direction.getZOffset() * n,
                getDimension(),
                getFacing());
    }

    public void writeToTag(@Nonnull NBTTagCompound tag) {
        tag.setLong("position", toLong());
        tag.setInteger("dimension", getDimension());
        EnumFacing facing = getFacing();
        if ( facing != null )
            tag.setByte("facing", (byte) facing.ordinal());
    }

    public static void removeFromTag(@Nonnull NBTTagCompound tag) {
        tag.removeTag("position");
        tag.removeTag("dimension");
        tag.removeTag("facing");
    }

    @Nonnull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .add("dimension", getDimension())
                .add("facing", getFacing())
                .toString();
    }

    @Override
    public boolean equals(Object other) {
        if ( other instanceof BlockPosDimension ) {
            BlockPosDimension otherPos = (BlockPosDimension) other;
            return otherPos.getDimension() == getDimension() && getFacing() == otherPos.getFacing() && otherPos.getX() == getX() && otherPos.getY() == getY() && otherPos.getZ() == getZ();
        }

        return super.equals(other);
    }

    public boolean equalsIgnoreFacing(Object other) {
        if ( other instanceof BlockPosDimension ) {
            BlockPosDimension otherPos = (BlockPosDimension) other;
            return otherPos.getDimension() == getDimension() && otherPos.getX() == getX() && otherPos.getY() == getY() && otherPos.getZ() == getZ();
        }

        return super.equals(other);
    }

    public double getDistance(@Nonnull Vec3i pos) {
        return getDistance(pos.getX(), pos.getY(), pos.getZ());
    }

    public int getDimension() {
        return dimension;
    }

    @Nullable
    public EnumFacing getFacing() {
        return facing;
    }

    @Nonnull
    public BlockPosDimension toImmutable() {
        return this;
    }

    @Nonnull
    public static Iterator<BlockPosDimension> iterateWithDimension(int dimension, @Nonnull Iterable<BlockPos> positions) {
        Iterator<BlockPos> pos = positions.iterator();

        return new Iterator<BlockPosDimension>() {
            @Override
            public boolean hasNext() {
                return pos.hasNext();
            }

            @Override
            public BlockPosDimension next() {
                return new BlockPosDimension(pos.next(), dimension);
            }
        };
    }

    @Nonnull
    public static Iterable<BlockPosDimension> getAllInBox(@Nonnull BlockPosDimension from, @Nonnull BlockPos to) {
        return getAllInBox(
                Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()),
                Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()),
                from.getDimension(), from.getFacing()
        );
    }

    @Nonnull
    public static Iterable<BlockPosDimension> getAllInBox(@Nonnull BlockPos from, @Nonnull BlockPos to, int dimension) {
        return getAllInBox(from, to, dimension, null);
    }

    @Nonnull
    public static Iterable<BlockPosDimension> getAllInBox(@Nonnull BlockPos from, @Nonnull BlockPos to, int dimension, @Nullable EnumFacing facing) {
        return getAllInBox(
                Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()),
                Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()),
                dimension, facing
        );
    }

    @Nonnull
    public static Iterable<BlockPosDimension> getAllInBox(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, final int dimension, @Nullable final EnumFacing facing) {
        return () -> new AbstractIterator<BlockPosDimension>() {
            private boolean first = true;
            private int x;
            private int y;
            private int z;

            @Override
            protected BlockPosDimension computeNext() {
                if ( first ) {
                    first = false;
                    x = x1;
                    y = y1;
                    z = z1;

                } else if ( x == x2 && y == y2 && z == z2 )
                    return endOfData();
                else {
                    if ( x < x2 )
                        x++;
                    else if ( y < y2 ) {
                        x = x1;
                        y++;
                    } else if ( z < z2 ) {
                        x = x1;
                        y = y1;
                        z++;
                    }
                }

                return new BlockPosDimension(x, y, z, dimension, facing);
            }
        };
    }

    @Nonnull
    public static Iterable<MutableBPD> getAllInBoxMutable(@Nonnull final BlockPos start, @Nonnull final BlockPos end, final int dimension, @Nullable final EnumFacing facing) {
        return getAllInBoxMutable(
                start.getX(), start.getY(), start.getZ(),
                end.getX(), end.getY(), end.getZ(),
                dimension, facing
        );
    }

    @Nonnull
    public static Iterable<MutableBPD> getAllInBoxMutable(@Nonnull final BlockPosDimension start, @Nonnull final BlockPos end) {
        return getAllInBoxMutable(
                start.getX(), start.getY(), start.getZ(),
                end.getX(), end.getY(), end.getZ(),
                start.getDimension(), start.getFacing()
        );
    }

    @Nonnull
    public static Iterable<MutableBPD> getAllInBoxMutable(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, final int dimension, @Nullable final EnumFacing facing) {
        return () -> new AbstractIterator<MutableBPD>() {
            private MutableBPD pos;

            protected MutableBPD computeNext() {
                if ( pos == null ) {
                    pos = new MutableBPD(x1, y1, z1, dimension, facing);
                    return pos;
                } else if ( pos.x == x2 && pos.y == y2 && pos.z == z2 )
                    return endOfData();

                if ( pos.x < x2 )
                    pos.incrementX();
                else if ( pos.y < y2 ) {
                    pos.setX(x1);
                    pos.incrementY();
                } else if ( pos.z < z2 ) {
                    pos.setX(x1);
                    pos.setY(y1);
                    pos.incrementZ();
                }

                return pos;
            }
        };
    }

    public static class MutableBPD extends BlockPosDimension {
        protected int dimension;
        protected EnumFacing facing;

        protected int x;
        protected int y;
        protected int z;

        public MutableBPD() {
            this(0, 0, 0, 0);
        }

        public MutableBPD(int x, int y, int z, int dimension) {
            this(x, y, z, dimension, null);
        }

        public MutableBPD(int x, int y, int z, int dimension, @Nullable EnumFacing facing) {
            super(0, 0, 0, 0, null);
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.facing = facing;
        }

        public MutableBPD(@Nonnull BlockPosDimension pos) {
            this(pos.getX(), pos.getY(), pos.getZ(), pos.getDimension(), pos.getFacing());
        }

        public MutableBPD(@Nonnull BlockPos pos, int dimension) {
            this(pos.getX(), pos.getY(), pos.getZ(), dimension, null);
        }

        public MutableBPD(@Nonnull BlockPos pos, int dimension, @Nullable EnumFacing facing) {
            this(pos.getX(), pos.getY(), pos.getZ(), dimension, facing);
        }

        @Override
        @Nonnull
        public BlockPosDimension add(int x, int y, int z) {
            return super.add(x, y, z).toImmutable();
        }

        @Override
        @Nonnull
        public BlockPosDimension offset(@Nonnull EnumFacing facing, int n) {
            return super.offset(facing, n).toImmutable();
        }

        @Nonnull
        @Override
        public BlockPosDimension facing(@Nullable EnumFacing side) {
            return super.facing(side).toImmutable();
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public int getDimension() {
            return dimension;
        }

        @Override
        @Nullable
        public EnumFacing getFacing() {
            return facing;
        }

        @Nonnull
        public MutableBPD setPos(@Nonnull Vec3i vec) {
            return setPos(vec.getX(), vec.getY(), vec.getZ());
        }

        @Nonnull
        public MutableBPD setPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Nonnull
        public MutableBPD setPos(double x, double y, double z) {
            return setPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        }

        @Nonnull
        public MutableBPD incrementX() {
            return incrementX(1);
        }

        @Nonnull
        public MutableBPD incrementX(int val) {
            return setX(getX() + val);
        }

        @Nonnull
        public MutableBPD incrementY() {
            return incrementY(1);
        }

        @Nonnull
        public MutableBPD incrementY(int val) {
            return setY(getY() + val);
        }

        @Nonnull
        public MutableBPD incrementZ() {
            return incrementZ(1);
        }

        @Nonnull
        public MutableBPD incrementZ(int val) {
            return setZ(getZ() + val);
        }

        @Nonnull
        public MutableBPD setX(int x) {
            this.x = x;
            return this;
        }

        @Nonnull
        public MutableBPD setY(int y) {
            this.y = y;
            return this;
        }

        @Nonnull
        public MutableBPD setZ(int z) {
            this.z = z;
            return this;
        }

        @Nonnull
        public MutableBPD setDimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        @Nonnull
        public MutableBPD setFacing(@Nullable EnumFacing facing) {
            this.facing = facing;
            return this;
        }

        @Nonnull
        public MutableBPD move(@Nonnull EnumFacing facing) {
            return move(facing, 1);
        }

        @Nonnull
        public MutableBPD move(@Nonnull EnumFacing facing, int n) {
            return setPos(
                    getX() + facing.getXOffset() * n,
                    getY() + facing.getYOffset() * n,
                    getZ() + facing.getZOffset() * n
            );
        }

        @Override
        @Nonnull
        public BlockPosDimension toImmutable() {
            return new BlockPosDimension(this);
        }
    }

    public static final class PooledMutableBPD extends MutableBPD {
        private boolean released;
        private static final List<PooledMutableBPD> POOL = Lists.newArrayList();

        private PooledMutableBPD(int x, int y, int z, int dimension, @Nullable EnumFacing facing) {
            super(x, y, z, dimension, facing);
        }

        @Nonnull
        public static PooledMutableBPD retain() {
            return retain(0, 0, 0, 0, null);
        }

        @Nonnull
        public static PooledMutableBPD retain(double x, double y, double z, int dimension) {
            return retain(x, y, z, dimension);
        }

        @Nonnull
        public static PooledMutableBPD retain(double x, double y, double z, int dimension, @Nullable EnumFacing facing) {
            return retain(
                    MathHelper.floor(x),
                    MathHelper.floor(y),
                    MathHelper.floor(z),
                    dimension, facing
            );
        }

        @Nonnull
        public static PooledMutableBPD retain(@Nonnull Vec3i vec, int dimension) {
            return retain(vec, dimension, null);
        }

        @Nonnull
        public static PooledMutableBPD retain(@Nonnull Vec3i vec, int dimension, @Nullable EnumFacing facing) {
            return retain(vec.getX(), vec.getY(), vec.getZ(), dimension, facing);
        }

        @Nonnull
        public static PooledMutableBPD retain(@Nonnull BlockPosDimension pos) {
            return retain(pos.getX(), pos.getY(), pos.getZ(), pos.getDimension(), pos.getFacing());
        }

        @Nonnull
        public static PooledMutableBPD retain(int x, int y, int z, int dimension) {
            return retain(x, y, z, dimension, null);
        }

        @Nonnull
        public static PooledMutableBPD retain(int x, int y, int z, int dimension, @Nullable EnumFacing facing) {
            synchronized (POOL) {
                if ( !POOL.isEmpty() ) {
                    PooledMutableBPD pos = POOL.remove(POOL.size() - 1);
                    if ( pos != null && pos.released ) {
                        pos.released = false;
                        pos.setPos(x, y, z).setDimension(dimension).setFacing(facing);
                        return pos;
                    }
                }
            }

            return new PooledMutableBPD(x, y, z, dimension, facing);
        }

        public void release() {
            released = true;

            synchronized (POOL) {
                if ( POOL.size() < 100 )
                    POOL.add(this);
            }
        }

        @Override
        @Nonnull
        public PooledMutableBPD setPos(int x, int y, int z) {
            if ( released ) {
                LOGGER.error("PooledMutableBPD modified after it was released.", new Throwable());
                released = false;
            }

            return (PooledMutableBPD) super.setPos(x, y, z);
        }

        @Override
        @Nonnull
        public PooledMutableBPD setPos(double x, double y, double z) {
            return (PooledMutableBPD) super.setPos(x, y, z);
        }

        @Nonnull
        @Override
        public PooledMutableBPD setPos(@Nonnull Vec3i vec) {
            return (PooledMutableBPD) super.setPos(vec);
        }

        @Nonnull
        @Override
        public PooledMutableBPD incrementX(int val) {
            return (PooledMutableBPD) super.incrementX(val);
        }

        @Nonnull
        @Override
        public PooledMutableBPD incrementY(int val) {
            return (PooledMutableBPD) super.incrementY(val);
        }

        @Nonnull
        @Override
        public PooledMutableBPD incrementZ(int val) {
            return (PooledMutableBPD) super.incrementZ(val);
        }

        @Nonnull
        @Override
        public PooledMutableBPD incrementX() {
            return (PooledMutableBPD) super.incrementX();
        }

        @Nonnull
        @Override
        public PooledMutableBPD incrementY() {
            return (PooledMutableBPD) super.incrementY();
        }

        @Nonnull
        @Override
        public PooledMutableBPD incrementZ() {
            return (PooledMutableBPD) super.incrementZ();
        }

        @Nonnull
        @Override
        public PooledMutableBPD setX(int x) {
            if ( released ) {
                LOGGER.error("PooledMutableBPD modified after it was released.", new Throwable());
                released = false;
            }

            return (PooledMutableBPD) super.setX(x);
        }

        @Nonnull
        @Override
        public PooledMutableBPD setY(int y) {
            if ( released ) {
                LOGGER.error("PooledMutableBPD modified after it was released.", new Throwable());
                released = false;
            }

            return (PooledMutableBPD) super.setY(y);
        }

        @Nonnull
        @Override
        public PooledMutableBPD setZ(int z) {
            if ( released ) {
                LOGGER.error("PooledMutableBPD modified after it was released.", new Throwable());
                released = false;
            }

            return (PooledMutableBPD) super.setZ(z);
        }

        @Nonnull
        @Override
        public PooledMutableBPD setDimension(int dimension) {
            if ( released ) {
                LOGGER.error("PooledMutableBPD modified after it was released.", new Throwable());
                released = false;
            }

            return (PooledMutableBPD) super.setDimension(dimension);
        }

        @Nonnull
        @Override
        public PooledMutableBPD setFacing(@Nullable EnumFacing facing) {
            if ( released ) {
                LOGGER.error("PooledMutableBPD modified after it was released.", new Throwable());
                released = false;
            }

            return (PooledMutableBPD) super.setFacing(facing);
        }

        @Nonnull
        @Override
        public PooledMutableBPD move(@Nonnull EnumFacing facing) {
            return (PooledMutableBPD) super.move(facing);
        }

        @Nonnull
        @Override
        public PooledMutableBPD move(@Nonnull EnumFacing facing, int n) {
            return (PooledMutableBPD) super.move(facing, n);
        }
    }
}

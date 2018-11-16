package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Iterator;

@Immutable
public class BlockPosDimension extends BlockPos {
    private final int dimension;
    private final EnumFacing facing;

    public static BlockPosDimension fromTag(NBTTagCompound tag) {
        if ( tag == null || !tag.hasKey("position") || !tag.hasKey("dimension") )
            return null;

        EnumFacing[] values = EnumFacing.values();

        return new BlockPosDimension(
                BlockPos.fromLong(tag.getLong("position")),
                tag.getInteger("dimension"),
                tag.hasKey("facing") ?
                        values[((int) tag.getByte("facing")) % values.length] :
                        null
        );
    }

    public static BlockPosDimension fromTileEntity(TileEntity entity) {
        return BlockPosDimension.fromTileEntity(entity, null);
    }

    public static BlockPosDimension fromTileEntity(TileEntity entity, EnumFacing facing) {
        BlockPos pos = entity.getPos();
        World world = entity.getWorld();
        if ( pos == null || world == null || world.provider == null )
            return null;

        return new BlockPosDimension(pos, world.provider.getDimension(), facing);
    }

    public BlockPosDimension(int x, int y, int z, int dimension) {
        this(x, y, z, dimension, null);
    }

    public BlockPosDimension(int x, int y, int z, int dimension, EnumFacing facing) {
        super(x, y, z);

        this.dimension = dimension;
        this.facing = facing;
    }

    public BlockPosDimension(BlockPos pos, int dimension) {
        this(pos, dimension, null);
    }

    public BlockPosDimension(BlockPos pos, int dimension, EnumFacing facing) {
        super(pos);

        this.dimension = dimension;
        this.facing = facing;
    }

    public BlockPosDimension facing(EnumFacing side) {
        return side == facing ? this : new BlockPosDimension(this, getDimension(), side);
    }

    @Nonnull
    @Override
    public BlockPosDimension add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPosDimension(
                this.getX() + x,
                this.getY() + y,
                this.getZ() + z,
                getDimension(),
                getFacing());
    }

    @Nonnull
    @Override
    public BlockPosDimension offset(@Nonnull EnumFacing direction, int n) {
        return n == 0 ? this : new BlockPosDimension(
                this.getX() + direction.getXOffset() * n,
                this.getY() + direction.getYOffset() * n,
                this.getZ() + direction.getZOffset() * n,
                getDimension(),
                getFacing());
    }

    public void writeToTag(NBTTagCompound tag) {
        tag.setLong("position", toLong());
        tag.setInteger("dimension", dimension);
        if ( facing != null )
            tag.setByte("facing", (byte) facing.ordinal());
    }

    public static void removeFromTag(NBTTagCompound tag) {
        tag.removeTag("position");
        tag.removeTag("dimension");
        tag.removeTag("facing");
    }

    @Nonnull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).add("dimension", this.getDimension()).add("facing", this.getFacing()).toString();
    }

    @Override
    public boolean equals(Object other) {
        if ( other instanceof BlockPosDimension ) {
            BlockPosDimension otherPos = (BlockPosDimension) other;
            boolean facingEqual = otherPos.getFacing() == null || getFacing() == null || otherPos.getFacing() == getFacing();
            return otherPos.getDimension() == getDimension() && facingEqual && otherPos.getX() == getX() && otherPos.getY() == getY() && otherPos.getZ() == getZ();
        }

        return super.equals(other);
    }

    public int getDimension() {
        return this.dimension;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    public static Iterator<BlockPosDimension> iterateWithDimension(int dimension, Iterable<BlockPos> positions) {
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
}

package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.MoreObjects;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class BlockArea {
    public final int dimension;

    public final int minX;
    public final int minY;
    public final int minZ;

    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public final int color;
    public final EnumFacing activeSide;

    public final String name;
    public final Vec3d vector;

    public BlockArea(int dimension, int xA, int yA, int zA, int xB, int yB, int zB, EnumFacing activeSide, int color, boolean includeMaximumBlock) {
        this(dimension, xA, yA, zA, xB, yB, zB, activeSide, color, includeMaximumBlock, null, null);
    }

    public BlockArea(int dimension, int xA, int yA, int zA, int xB, int yB, int zB, EnumFacing activeSide, int color, boolean includeMaximumBlock, String name) {
        this(dimension, xA, yA, zA, xB, yB, zB, activeSide, color, includeMaximumBlock, name, null);
    }

    public BlockArea(int dimension, int xA, int yA, int zA, int xB, int yB, int zB, EnumFacing activeSide, int color, boolean includeMaximumBlock, String name, Vec3d vector) {
        minX = Math.min(xA, xB);
        minY = Math.min(yA, yB);
        minZ = Math.min(zA, zB);

        int maxOffset = includeMaximumBlock ? 1 : 0;

        maxX = Math.max(xA, xB) + maxOffset;
        maxY = Math.max(yA, yB) + maxOffset;
        maxZ = Math.max(zA, zB) + maxOffset;

        this.dimension = dimension;
        this.color = color;
        this.activeSide = activeSide;
        this.name = name;
        this.vector = vector;
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, EnumFacing activeSide, int color, boolean includeMaximumBlock) {
        this(dimension, cornerA.getX(), cornerA.getY(), cornerA.getZ(), cornerB.getX(), cornerB.getY(), cornerB.getZ(), activeSide, color, includeMaximumBlock);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, EnumFacing activeSide, int color, boolean includeMaximumBlock, String name) {
        this(dimension, cornerA.getX(), cornerA.getY(), cornerA.getZ(), cornerB.getX(), cornerB.getY(), cornerB.getZ(), activeSide, color, includeMaximumBlock, name);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, EnumFacing activeSide, int color, boolean includeMaximumBlock, String name, Vec3d vector) {
        this(dimension, cornerA.getX(), cornerA.getY(), cornerA.getZ(), cornerB.getX(), cornerB.getY(), cornerB.getZ(), activeSide, color, includeMaximumBlock, name, vector);
    }

    public BlockArea(BlockPosDimension pos, int color) {
        this(pos.getDimension(), pos, pos, pos.getFacing(), color, true);
    }

    public BlockArea(BlockPosDimension pos, int color, String name) {
        this(pos.getDimension(), pos, pos, pos.getFacing(), color, true, name);
    }

    public BlockArea(BlockPosDimension pos, int color, String name, Vec3d vector) {
        this(pos.getDimension(), pos, pos, pos.getFacing(), color, true, name, vector);
    }

    public BlockArea(BlockPosDimension cornerA, BlockPos cornerB, int color) {
        this(cornerA.getDimension(), cornerA, cornerB, cornerA.getFacing(), color, true);
    }

    public BlockArea(BlockPosDimension cornerA, BlockPos cornerB, int color, String name) {
        this(cornerA.getDimension(), cornerA, cornerB, cornerA.getFacing(), color, true, name);
    }

    public BlockArea(BlockPosDimension cornerA, BlockPos cornerB, int color, String name, Vec3d vector) {
        this(cornerA.getDimension(), cornerA, cornerB, cornerA.getFacing(), color, true, name, vector);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, int color) {
        this(dimension, cornerA, cornerB, null, color, true);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, int color, String name) {
        this(dimension, cornerA, cornerB, null, color, true, name);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, int color, String name, Vec3d vector) {
        this(dimension, cornerA, cornerB, null, color, true, name, vector);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, EnumFacing activeSide, int color) {
        this(dimension, cornerA, cornerB, activeSide, color, true);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, EnumFacing activeSide, int color, String name) {
        this(dimension, cornerA, cornerB, activeSide, color, true, name);
    }

    public BlockArea(int dimension, BlockPos cornerA, BlockPos cornerB, EnumFacing activeSide, int color, String name, Vec3d vector) {
        this(dimension, cornerA, cornerB, activeSide, color, true, name, vector);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dimension", dimension)
                .add("minX", minX)
                .add("minY", minY)
                .add("minZ", minZ)
                .add("maxX", maxX)
                .add("maxY", maxY)
                .add("maxZ", maxZ)
                .add("color", color)
                .add("side", activeSide)
                .add("name", name)
                .add("vector", vector)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        BlockArea blockArea = (BlockArea) o;
        return dimension == blockArea.dimension &&
                minX == blockArea.minX &&
                minY == blockArea.minY &&
                minZ == blockArea.minZ &&
                maxX == blockArea.maxX &&
                maxY == blockArea.maxY &&
                maxZ == blockArea.maxZ &&
                color == blockArea.color &&
                vector == blockArea.vector;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, activeSide, minX, minY, minZ, maxX, maxY, maxZ, color, vector);
    }
}

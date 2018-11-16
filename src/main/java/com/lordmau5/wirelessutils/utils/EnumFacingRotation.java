package com.lordmau5.wirelessutils.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumFacingRotation implements IStringSerializable {
    DOWN_Z(EnumFacing.DOWN, false, "down_z", 7),
    UP_Z(EnumFacing.UP, false, "up_z", 6),
    NORTH(EnumFacing.NORTH, false, "north", 3),
    SOUTH(EnumFacing.SOUTH, false, "south", 2),
    WEST(EnumFacing.WEST, false, "west", 5),
    EAST(EnumFacing.EAST, false, "east", 4),
    DOWN_X(EnumFacing.DOWN, true, "down_x", 1),
    UP_X(EnumFacing.UP, true, "up_x", 0);

    public final EnumFacing facing;
    public final boolean rotation_x;
    public final String name;
    private final int opposite;

    private EnumFacingRotation(EnumFacing facing, boolean rotation_x, String name, int opposite) {
        this.facing = facing;
        this.rotation_x = rotation_x;
        this.name = name;
        this.opposite = opposite;
    }

    public static EnumFacingRotation fromFacing(EnumFacing facing, boolean rotation_x) {
        return values()[facing.ordinal() + (facing.getAxis() == EnumFacing.Axis.Y && rotation_x ? 6 : 0)];
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public EnumFacingRotation getOpposite() {
        return EnumFacingRotation.values()[opposite];
    }

    public EnumFacingRotation rotateY() {
        switch (this) {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            case UP_X:
                return UP_Z;
            case UP_Z:
                return UP_X;
            case DOWN_X:
                return DOWN_X;
            case DOWN_Z:
                return DOWN_Z;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    public EnumFacingRotation rotateX() {
        switch (this) {
            case NORTH:
                return DOWN_X;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
            case SOUTH:
                return UP_X;
            case UP_X:
            case UP_Z:
                return NORTH;
            case DOWN_X:
            case DOWN_Z:
                return SOUTH;
        }
    }

    public EnumFacingRotation rotateZ() {
        switch (this) {
            case EAST:
                return DOWN_Z;
            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            case WEST:
                return UP_Z;
            case UP_X:
            case UP_Z:
                return EAST;
            case DOWN_Z:
            case DOWN_X:
                return WEST;
        }
    }

    public EnumFacingRotation rotateAround(EnumFacing.Axis axis) {
        switch (axis) {
            case X:
                if ( this != EAST && this != WEST )
                    return rotateX();
                return this;
            case Y:
                return rotateY();
            case Z:
                if ( this != NORTH && this != SOUTH )
                    return rotateZ();
                return this;
            default:
                throw new IllegalStateException("Unable to get facing for axis: " + axis);
        }
    }


}

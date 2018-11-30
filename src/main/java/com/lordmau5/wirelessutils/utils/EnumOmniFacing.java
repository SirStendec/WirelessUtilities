package com.lordmau5.wirelessutils.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumOmniFacing implements IStringSerializable {
    DOWN(EnumFacing.DOWN, false, "down", 1, 2), // 0
    DOWN_ROT(EnumFacing.DOWN, true, "down_rot", 0, 3), // 1
    UP(EnumFacing.UP, false, "up", 3, 0), // 2
    UP_ROT(EnumFacing.UP, true, "up_rot", 2, 1), // 3
    NORTH(EnumFacing.NORTH, false, "north", 5, 8), // 4
    NORTH_ROT(EnumFacing.NORTH, true, "north_rot", 4, 9), // 5
    EAST(EnumFacing.EAST, false, "east", 7, 10), // 6
    EAST_ROT(EnumFacing.EAST, true, "east_rot", 6, 11), // 7
    SOUTH(EnumFacing.SOUTH, false, "south", 9, 4), // 8
    SOUTH_ROT(EnumFacing.SOUTH, true, "south_rot", 8, 5), // 9
    WEST(EnumFacing.WEST, false, "west", 11, 6), // 10
    WEST_ROT(EnumFacing.WEST, true, "west_rot", 10, 7); // 11

    /* Static */

    private static final EnumOmniFacing[] facings = new EnumOmniFacing[EnumFacing.values().length];

    static {
        EnumOmniFacing[] values = values();
        for (int i = 0; i < values.length; i++) {
            EnumOmniFacing value = values[i];
            if ( !value.rotated )
                facings[value.facing.ordinal()] = value;
        }
    }

    public static EnumOmniFacing fromFacing(EnumFacing facing, boolean rotated) {
        EnumOmniFacing out = facings[facing.ordinal() % facings.length];
        if ( rotated )
            return out.oppositeRotation();
        return out;
    }

    public static EnumOmniFacing fromInt(int index) {
        EnumOmniFacing[] values = values();
        return values[index % values.length];
    }

    /* Enum */

    public final EnumFacing facing;
    public final boolean rotated;
    public final String name;
    private final int oppositeRotation;
    private final int opposite;

    EnumOmniFacing(EnumFacing facing, boolean rotated, String name, int oppositeRotation, int opposite) {
        this.facing = facing;
        this.rotated = rotated;
        this.name = name;
        this.oppositeRotation = opposite;
        this.opposite = opposite;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public EnumOmniFacing opposite() {
        return EnumOmniFacing.values()[opposite];
    }

    public EnumOmniFacing oppositeRotation() {
        return EnumOmniFacing.values()[oppositeRotation];
    }

    public EnumOmniFacing rotateY() {
        switch (this) {
            case NORTH:
                return EAST;
            case NORTH_ROT:
                return EAST_ROT;
            case EAST:
                return SOUTH;
            case EAST_ROT:
                return SOUTH_ROT;
            case SOUTH:
                return WEST;
            case SOUTH_ROT:
                return WEST_ROT;
            case WEST:
                return NORTH;
            case WEST_ROT:
                return NORTH_ROT;
            case UP:
                return UP_ROT;
            case UP_ROT:
                return UP;
            case DOWN:
                return DOWN_ROT;
            case DOWN_ROT:
                return DOWN;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    public EnumOmniFacing rotateX() {
        switch (this) {
            case NORTH:
                return DOWN;
            case NORTH_ROT:
                return DOWN_ROT;
            case EAST:
                return EAST_ROT;
            case EAST_ROT:
                return EAST;
            case SOUTH:
                return UP;
            case SOUTH_ROT:
                return UP_ROT;
            case WEST:
                return WEST_ROT;
            case WEST_ROT:
                return WEST;
            case DOWN:
                return SOUTH;
            case DOWN_ROT:
                return SOUTH_ROT;
            case UP:
                return NORTH;
            case UP_ROT:
                return NORTH_ROT;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    public EnumOmniFacing rotateZ() {
        switch (this) {
            case NORTH:
                return NORTH_ROT;
            case NORTH_ROT:
                return NORTH;
            case EAST:
                return DOWN_ROT;
            case EAST_ROT:
                return DOWN;
            case SOUTH:
                return SOUTH_ROT;
            case SOUTH_ROT:
                return SOUTH;
            case WEST:
                return UP_ROT;
            case WEST_ROT:
                return UP;
            case DOWN:
                return WEST_ROT;
            case DOWN_ROT:
                return WEST;
            case UP:
                return EAST_ROT;
            case UP_ROT:
                return EAST;
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
        }
    }

    public EnumOmniFacing rotateAround(EnumFacing.Axis axis) {
        switch (axis) {
            case X:
                return rotateX();
            case Y:
                return rotateY();
            case Z:
                return rotateZ();
            default:
                throw new IllegalStateException("Unable to get facing for axis: " + axis);
        }
    }
}

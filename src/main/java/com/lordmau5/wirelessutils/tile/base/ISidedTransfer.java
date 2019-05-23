package com.lordmau5.wirelessutils.tile.base;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public interface ISidedTransfer {

    enum TransferSide {
        FRONT(0),
        BACK(1),
        LEFT(2),
        RIGHT(3),
        TOP(4),
        BOTTOM(5);

        public final int index;
        public static final TransferSide[] VALUES = new TransferSide[6];

        TransferSide(int index) {
            this.index = index;
        }

        static {
            for (TransferSide side : values()) {
                VALUES[side.index] = side;
            }
        }

        public static TransferSide byIndex(int index) {
            return VALUES[MathHelper.abs(index % VALUES.length)];
        }
    }

    enum Mode {
        PASSIVE(0, false),
        ACTIVE(1, true),
        DISABLED(2, false),
        INPUT(3, true),
        OUTPUT(4, true);

        public final int index;
        public final boolean active;
        public static final Mode[] VALUES = new Mode[5];

        Mode(int index, boolean active) {
            this.index = index;
            this.active = active;
        }

        public Mode next(boolean specific) {
            switch (this) {
                case PASSIVE:
                    if ( specific )
                        return INPUT;
                    return ACTIVE;
                case INPUT:
                    return OUTPUT;
                case OUTPUT:
                case ACTIVE:
                    return DISABLED;
                default:
                    return PASSIVE;
            }
        }

        public Mode prev(boolean specific) {
            switch (this) {
                case PASSIVE:
                    return DISABLED;
                case OUTPUT:
                    return INPUT;
                case INPUT:
                case ACTIVE:
                    return PASSIVE;
                default:
                    if ( specific )
                        return OUTPUT;
                    return ACTIVE;
            }
        }

        static {
            for (Mode mode : values()) {
                VALUES[mode.ordinal()] = mode;
            }
        }

        public static Mode byIndex(int index) {
            return VALUES[MathHelper.abs(index % VALUES.length)];
        }
    }

    EnumFacing getEnumFacing();

    boolean getRotationX();

    default TransferSide getSideForFacing(EnumFacing face) {
        EnumFacing facing = getEnumFacing();
        boolean rot = getRotationX();

        if ( facing == face )
            return TransferSide.FRONT;

        else if ( facing.getOpposite() == face )
            return TransferSide.BACK;

        switch (facing) {
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                if ( face == EnumFacing.UP )
                    return TransferSide.TOP;
                else if ( face == EnumFacing.DOWN )
                    return TransferSide.BOTTOM;
                else if ( facing.rotateY() == face )
                    return TransferSide.LEFT;

                return TransferSide.RIGHT;

            case UP:
                if ( face == EnumFacing.NORTH )
                    return rot ? TransferSide.RIGHT : TransferSide.BOTTOM;
                else if ( face == EnumFacing.EAST )
                    return rot ? TransferSide.BOTTOM : TransferSide.LEFT;
                else if ( face == EnumFacing.WEST )
                    return rot ? TransferSide.TOP : TransferSide.RIGHT;
                return rot ? TransferSide.LEFT : TransferSide.TOP;

            default:
                if ( face == EnumFacing.NORTH )
                    return rot ? TransferSide.RIGHT : TransferSide.TOP;
                else if ( face == EnumFacing.EAST )
                    return rot ? TransferSide.TOP : TransferSide.LEFT;
                else if ( face == EnumFacing.WEST )
                    return rot ? TransferSide.BOTTOM : TransferSide.RIGHT;
                return rot ? TransferSide.LEFT : TransferSide.BOTTOM;
        }
    }

    default EnumFacing getFacingForSide(TransferSide side) {
        EnumFacing facing = getEnumFacing();

        if ( side == TransferSide.BACK )
            return facing.getOpposite();

        else if ( side == TransferSide.TOP ) {
            if ( facing == EnumFacing.UP )
                return getRotationX() ? EnumFacing.WEST : EnumFacing.SOUTH;
            else if ( facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.EAST : EnumFacing.NORTH;

            return EnumFacing.UP;

        } else if ( side == TransferSide.BOTTOM ) {
            if ( facing == EnumFacing.UP )
                return getRotationX() ? EnumFacing.EAST : EnumFacing.NORTH;
            else if ( facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.WEST : EnumFacing.SOUTH;

            return EnumFacing.DOWN;

        } else if ( side == TransferSide.LEFT ) {
            if ( facing == EnumFacing.UP || facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.SOUTH : EnumFacing.EAST;

            return facing.rotateY();

        } else if ( side == TransferSide.RIGHT ) {
            if ( facing == EnumFacing.UP || facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.NORTH : EnumFacing.WEST;

            return facing.rotateYCCW();

        } else
            return facing;
    }

    default boolean isModeSpecific() {
        return false;
    }

    String TEXTURE_PREFIX = "wirelessutils:block/side_";

    default String getTextureForMode(Mode mode) {
        switch (mode) {
            case DISABLED:
                return TEXTURE_PREFIX + "disabled";
            case INPUT:
                return TEXTURE_PREFIX + "input";
            case OUTPUT:
                return TEXTURE_PREFIX + "output";
            default:
                return null;
        }
    }

    default String getTextureForMode(Mode mode, boolean input) {
        if ( mode == Mode.ACTIVE )
            mode = input ? Mode.INPUT : Mode.OUTPUT;

        return getTextureForMode(mode);
    }

    default boolean canSideTransfer(TransferSide side) {
        return side != TransferSide.FRONT;
    }

    default Mode getSideTransferMode(EnumFacing face) {
        return getSideTransferMode(getSideForFacing(face));
    }

    Mode getSideTransferMode(TransferSide side);

    default void setSideTransferMode(int side, Mode mode) {
        setSideTransferMode(TransferSide.byIndex(side), mode);
    }

    void setSideTransferMode(TransferSide side, Mode mode);

    void transferSide(TransferSide side);

    default void executeSidedTransfer() {
        for (TransferSide side : TransferSide.VALUES) {
            if ( getSideTransferMode(side).active )
                transferSide(side);
        }
    }
}

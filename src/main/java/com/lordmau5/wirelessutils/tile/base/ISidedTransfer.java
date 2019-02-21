package com.lordmau5.wirelessutils.tile.base;

import net.minecraft.util.EnumFacing;

public interface ISidedTransfer {

    enum TransferSide {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    EnumFacing getEnumFacing();

    boolean getRotationX();

    default TransferSide getSideForFacing(EnumFacing face) {
        EnumFacing facing = getEnumFacing();

        if ( facing == face )
            return TransferSide.FRONT;

        else if ( facing.getOpposite() == face )
            return TransferSide.BACK;

        else if ( facing.rotateY() == face )
            return TransferSide.RIGHT;

        else if ( facing.rotateYCCW() == face )
            return TransferSide.LEFT;

        else if ( facing.rotateAround(getRotationX() ? EnumFacing.Axis.X : EnumFacing.Axis.Z) == face )
            return TransferSide.BOTTOM;

        return TransferSide.TOP;
    }

    default int getSideProperty() {
        int out = 0;

        TransferSide[] sides = TransferSide.values();
        for (int i = 0; i < sides.length; i++)
            if ( isSideTransferEnabled(sides[i]) )
                out += 1 << i;

        return out;
    }

    default EnumFacing getFacingForSide(TransferSide side) {
        EnumFacing facing = getEnumFacing();

        if ( side == TransferSide.BACK )
            return facing.getOpposite();

        else if ( side == TransferSide.TOP ) {
            if ( facing == EnumFacing.UP )
                return getRotationX() ? EnumFacing.EAST : EnumFacing.NORTH;
            else if ( facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.WEST : EnumFacing.SOUTH;

            return EnumFacing.UP;

        } else if ( side == TransferSide.BOTTOM ) {
            if ( facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.EAST : EnumFacing.NORTH;
            else if ( facing == EnumFacing.UP )
                return getRotationX() ? EnumFacing.WEST : EnumFacing.SOUTH;

            return EnumFacing.DOWN;

        } else if ( side == TransferSide.LEFT ) {
            if ( facing == EnumFacing.UP )
                return getRotationX() ? EnumFacing.NORTH : EnumFacing.WEST;
            else if ( facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.SOUTH : EnumFacing.EAST;

            return facing.rotateY();

        } else if ( side == TransferSide.RIGHT ) {
            if ( facing == EnumFacing.UP )
                return getRotationX() ? EnumFacing.NORTH : EnumFacing.WEST;
            else if ( facing == EnumFacing.DOWN )
                return getRotationX() ? EnumFacing.SOUTH : EnumFacing.EAST;

            return facing.rotateYCCW();

        } else
            return facing;
    }

    /*default boolean isSideTransferEnabled(EnumFacing face) {
        return isSideTransferEnabled(getSideForFacing(face));
    }*/

    default boolean canSideTransfer(TransferSide side) {
        return side != TransferSide.FRONT;
    }

    boolean isSideTransferEnabled(TransferSide side);

    default void setSideTransferEnabled(int side, boolean enabled) {
        TransferSide[] values = TransferSide.values();
        setSideTransferEnabled(values[side % values.length], enabled);
    }

    void setSideTransferEnabled(TransferSide side, boolean enabled);

    void transferSide(TransferSide side);

    default void updateSidedTransfer() {
        for (TransferSide side : TransferSide.values()) {
            if ( isSideTransferEnabled(side) )
                transferSide(side);
        }
    }
}

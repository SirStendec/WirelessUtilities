package com.lordmau5.wirelessutils.tile.base;

import cofh.api.tileentity.IReconfigurableFacing;
import net.minecraft.util.EnumFacing;

public interface IFacing extends IReconfigurableFacing {
    EnumFacing getEnumFacing();

    @Override
    default int getFacing() {
        return getEnumFacing().ordinal();
    }

    boolean getRotationX();

    boolean setRotationX(boolean rotationX);

    boolean setFacing(EnumFacing facing);

    @Override
    default boolean setFacing(int side, boolean alternate) {
        if ( side == getFacing() )
            return true;

        if ( side < 0 || side > 5 )
            return false;

        return setFacing(EnumFacing.values()[side]);
    }

    default boolean rotateBlock(EnumFacing side) {
        if ( getEnumFacing().getAxis().isVertical() && side.getAxis().isVertical() ) {
            return setRotationX(!getRotationX());
        }

        return setFacing(getEnumFacing().rotateAround(side.getAxis()));
    }

    @Override
    default boolean rotateBlock() {
        return false;
    }
}

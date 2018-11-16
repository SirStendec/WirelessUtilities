package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;

public interface IDirectionalMachine extends IFacing {

    @Override
    default boolean allowYAxisFacing() {
        return true;
    }

    int getRange();

    int getRangeLength();

    int getRangeWidth();

    int getRangeHeight();

    void setRangeLength(int length);

    void setRangeWidth(int width);

    void setRangeHeight(int height);

    void setRanges(int height, int length, int width);

    int getOffsetHorizontal();

    int getOffsetVertical();

    void setOffsetHorizontal(int offset);

    void setOffsetVertical(int offset);

    default Tuple<BlockPosDimension, BlockPosDimension> calculateTargetCorners(BlockPosDimension origin) {
        EnumFacing facing = getEnumFacing();
        EnumFacing.Axis axis = facing.getAxis();

        BlockPosDimension offset;

        int rangeY = getRangeHeight();
        int rangeX;
        int rangeZ;

        if ( axis == EnumFacing.Axis.X ) {
            rangeX = getRangeLength();
            rangeZ = getRangeWidth();
            offset = origin.offset(facing, rangeX + 1);
            offset = offset.add(0, getOffsetVertical(), getOffsetHorizontal() * facing.getAxisDirection().getOffset());

        } else if ( axis == EnumFacing.Axis.Y ) {
            offset = origin.offset(facing, rangeY + 1);
            if ( getRotationX() ) {
                offset = offset.add(getOffsetVertical(), 0, getOffsetHorizontal());
                rangeX = getRangeLength();
                rangeZ = getRangeWidth();
            } else {
                offset = offset.add(getOffsetHorizontal(), 0, getOffsetVertical());
                rangeX = getRangeWidth();
                rangeZ = getRangeLength();
            }
        } else {
            rangeX = getRangeWidth();
            rangeZ = getRangeLength();
            offset = origin.offset(facing, rangeZ + 1);
            offset = offset.add(getOffsetHorizontal() * -facing.getAxisDirection().getOffset(), getOffsetVertical(), 0);
        }

        return new Tuple<>(
                offset.add(-rangeX, -rangeY, -rangeZ),
                offset.add(rangeX, rangeY, rangeZ)
        );
    }
}
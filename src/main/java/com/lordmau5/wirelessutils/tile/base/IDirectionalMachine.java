package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;

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

    default AxisAlignedBB getTargetBoundingBox(BlockPosDimension origin) {
        Tuple<BlockPosDimension, BlockPosDimension> corners = calculateTargetCorners(origin);
        BlockPosDimension posA = corners.getFirst();
        BlockPosDimension posB = corners.getSecond();

        return new AxisAlignedBB(
                posA.getX(), posA.getY(), posA.getZ(),
                posB.getX() + 1, posB.getY() + 1, posB.getZ() + 1
        );

        /*int minX = posA.getX();
        int minY = posA.getY();
        int minZ = posA.getZ();

        int maxX = posB.getX();
        int maxY = posB.getY();
        int maxZ = posB.getZ();

        if ( maxX < minX ) {
            int temp = minX;
            minX = maxX;
            maxX = temp;
        }

        if ( maxY < minY ) {
            int temp = minY;
            minY = maxY;
            maxY = temp;
        }

        if ( maxZ < minZ ) {
            int temp = minZ;
            minZ = maxZ;
            maxZ = temp;
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);*/
    }

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

        BlockPosDimension minPos = offset.add(-rangeX, -rangeY, -rangeZ);
        BlockPosDimension maxPos = offset.add(rangeX, rangeY, rangeZ);

        if ( origin.isInsideBorders() ) {
            minPos = minPos.clipToWorld();
            maxPos = maxPos.clipToWorld();
        }

        return new Tuple<>(minPos, maxPos);
    }
}
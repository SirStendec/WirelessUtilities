package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
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

        if ( origin.isInsideWorld() ) {
            minPos = minPos.clipToWorld();
            maxPos = maxPos.clipToWorld();
        }

        return new Tuple<>(minPos, maxPos);
    }

    static boolean isRangeValid(int range, int height, int length, int width) {
        ModConfig.Common.DirectionalArea area = ModConfig.common.area;
        if ( area == ModConfig.Common.DirectionalArea.SUM_OF_RANGES )
            return height + length + width <= (3 * range);

        else if ( area == ModConfig.Common.DirectionalArea.MAX_RANGE )
            return length <= range && height <= range && width <= range;

        else if ( area == ModConfig.Common.DirectionalArea.AREA ) {
            range = 1 + (range * 2);
            double blocks = Math.pow(range, 3);

            height = 1 + (height * 2);
            length = 1 + (length * 2);
            width = 1 + (width * 2);

            return ((double) height * length * width) <= blocks;

        } else
            return false;
    }

    static int getMaximumAllIncrease(int range, int height, int length, int width) {
        ModConfig.Common.DirectionalArea area = ModConfig.common.area;
        if ( area == ModConfig.Common.DirectionalArea.SUM_OF_RANGES ) {
            int remaining = (range * 3) - (height + length + width);
            if ( remaining <= 0 )
                return 0;
            return Math.floorDiv(remaining, 3);

        } else if ( area == ModConfig.Common.DirectionalArea.MAX_RANGE ) {
            return Math.min(range - height, Math.min(range - length, range - width));

        } else if ( area == ModConfig.Common.DirectionalArea.AREA ) {
            int maxValue = 1 + (range * 3);
            if ( maxValue > Byte.MAX_VALUE )
                maxValue = Byte.MAX_VALUE;

            range = 1 + (range * 2);
            double blocks = Math.pow(range, 3);
            int added = 0;
            int maxAdded = Math.min(maxValue - height, Math.min(maxValue - length, maxValue - width));

            height = 1 + (height * 2) + 2;
            length = 1 + (length * 2) + 2;
            width = 1 + (width * 2) + 2;

            while ( added < maxAdded && height * length * width <= blocks ) {
                added++;
                height += 2;
                width += 2;
                length += 2;
            }

            return added;

        } else
            return 0;
    }

    static int getMaximumHeightIncrease(int range, int height, int length, int width) {
        ModConfig.Common.DirectionalArea area = ModConfig.common.area;
        if ( area == ModConfig.Common.DirectionalArea.SUM_OF_RANGES ) {
            int remaining = (range * 3) - (height + length + width);
            if ( remaining <= 0 )
                return 0;
            return remaining;

        } else if ( area == ModConfig.Common.DirectionalArea.MAX_RANGE ) {
            return Math.max(0, range - height);

        } else if ( area == ModConfig.Common.DirectionalArea.AREA ) {
            int maxValue = 1 + (range * 3);
            if ( maxValue > Byte.MAX_VALUE )
                maxValue = Byte.MAX_VALUE;

            range = 1 + (range * 2);
            double blocks = Math.pow(range, 3);
            int added = 0;

            int maxAdded = maxValue - height;

            height = 1 + (height * 2) + 2;
            length = 1 + (length * 2);
            width = 1 + (width * 2);

            while ( added < maxAdded && height * length * width <= blocks ) {
                added++;
                height += 2;
            }

            return added;

        } else
            return 0;
    }

    static int getMaximumLengthIncrease(int range, int height, int length, int width) {
        ModConfig.Common.DirectionalArea area = ModConfig.common.area;
        if ( area == ModConfig.Common.DirectionalArea.SUM_OF_RANGES ) {
            int remaining = (range * 3) - (height + length + width);
            if ( remaining <= 0 )
                return 0;
            return remaining;

        } else if ( area == ModConfig.Common.DirectionalArea.MAX_RANGE ) {
            return Math.max(0, range - length);

        } else if ( area == ModConfig.Common.DirectionalArea.AREA ) {
            int maxValue = 1 + (range * 3);
            if ( maxValue > Byte.MAX_VALUE )
                maxValue = Byte.MAX_VALUE;

            range = 1 + (range * 2);
            double blocks = Math.pow(range, 3);
            int added = 0;

            int maxAdded = maxValue - length;

            height = 1 + (height * 2);
            length = 1 + (length * 2) + 2;
            width = 1 + (width * 2);

            while ( added < maxAdded && height * length * width <= blocks ) {
                added++;
                length += 2;
            }

            return added;

        } else
            return 0;
    }

    static int getMaximumWidthIncrease(int range, int height, int length, int width) {
        ModConfig.Common.DirectionalArea area = ModConfig.common.area;
        if ( area == ModConfig.Common.DirectionalArea.SUM_OF_RANGES ) {
            int remaining = (range * 3) - (height + length + width);
            if ( remaining <= 0 )
                return 0;
            return remaining;

        } else if ( area == ModConfig.Common.DirectionalArea.MAX_RANGE ) {
            return Math.max(0, range - width);

        } else if ( area == ModConfig.Common.DirectionalArea.AREA ) {
            int maxValue = 1 + (range * 3);
            if ( maxValue > Byte.MAX_VALUE )
                maxValue = Byte.MAX_VALUE;

            range = 1 + (range * 2);
            double blocks = Math.pow(range, 3);
            int added = 0;

            int maxAdded = maxValue - width;

            height = 1 + (height * 2);
            length = 1 + (length * 2);
            width = 1 + (width * 2) + 2;

            while ( added < maxAdded && height * length * width <= blocks ) {
                added++;
                width += 2;
            }

            return added;

        } else
            return 0;
    }

}
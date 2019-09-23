package com.lordmau5.wirelessutils.tile.base;

import com.google.common.collect.AbstractIterator;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDirectionalMachine extends IFacing {

    @Override
    default boolean allowYAxisFacing() {
        return true;
    }

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
        int rangeWidth, rangeLength, rangeHeight, offsetHorizontal, offsetVertical;

        if ( this instanceof IConfigurableRange ) {
            IConfigurableRange config = (IConfigurableRange) this;
            rangeHeight = config.getRangeHeight();
            rangeLength = config.getRangeLength();
            rangeWidth = config.getRangeWidth();
            offsetHorizontal = config.getOffsetHorizontal();
            offsetVertical = config.getOffsetVertical();

        } else {
            rangeHeight = 0;
            rangeLength = 0;
            rangeWidth = 0;
            offsetHorizontal = 0;
            offsetVertical = 0;
        }

        return calculateCorners(
                origin,
                getEnumFacing(),
                rangeWidth, rangeLength, rangeHeight,
                offsetHorizontal, offsetVertical, getRotationX(), true
        );
    }

    static Tuple<BlockPosDimension, BlockPosDimension> calculateCorners(
            @Nonnull BlockPosDimension origin, @Nullable EnumFacing facing,
            int rangeWidth, int rangeLength, int rangeHeight,
            int offsetHorizontal, int offsetVertical, boolean rotationX, boolean doOffset
    ) {
        EnumFacing.Axis axis = facing == null ? EnumFacing.Axis.X : facing.getAxis();
        BlockPosDimension offset = origin;

        int rangeY = rangeHeight;
        int rangeX;
        int rangeZ;

        if ( axis == EnumFacing.Axis.X ) {
            rangeX = rangeLength;
            rangeZ = rangeWidth;
            if ( doOffset )
                offset = origin.offset(facing, rangeX + 1);
            if ( facing != null )
                offset = offset.add(0, offsetVertical, offsetHorizontal * facing.getAxisDirection().getOffset());

        } else if ( axis == EnumFacing.Axis.Y ) {
            if ( doOffset )
                offset = origin.offset(facing, rangeY + 1);

            if ( rotationX ) {
                offset = offset.add(offsetVertical, 0, offsetHorizontal);
                rangeX = rangeLength;
                rangeZ = rangeWidth;
            } else {
                offset = offset.add(offsetHorizontal, 0, offsetVertical);
                rangeX = rangeWidth;
                rangeZ = rangeLength;
            }
        } else {
            rangeX = rangeWidth;
            rangeZ = rangeLength;
            if ( doOffset )
                offset = origin.offset(facing, rangeZ + 1);
            if ( facing != null )
                offset = offset.add(offsetHorizontal * -facing.getAxisDirection().getOffset(), offsetVertical, 0);
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

    @Nonnull
    static Iterable<Tuple<BlockPosDimension, ItemStack>> iterateTargets(@Nonnull final IDirectionalMachine machine, @Nonnull final BlockPosDimension origin, @Nullable final EnumFacing facing) {
        return iterateTargets(machine, origin, facing, ItemStack.EMPTY);
    }

    @Nonnull
    static Iterable<Tuple<BlockPosDimension, ItemStack>> iterateTargets(@Nonnull final IDirectionalMachine machine, @Nonnull final BlockPosDimension origin, @Nullable final EnumFacing facing, @Nonnull final ItemStack stack) {
        final Tuple<BlockPosDimension, BlockPosDimension> corners = machine.calculateTargetCorners(origin);
        return iterateTargets(corners.getFirst(), corners.getSecond(), facing, stack);
    }

    static Iterable<Tuple<BlockPosDimension, ItemStack>> iterateTargets(@Nonnull BlockPosDimension start, @Nonnull BlockPosDimension end, @Nullable EnumFacing facing, @Nonnull ItemStack stack) {
        final int x1 = start.getX();
        final int y1 = start.getY();
        final int z1 = start.getZ();

        final int x2 = end.getX();
        final int y2 = end.getY();
        final int z2 = end.getZ();

        final int dimension = start.getDimension();

        return () -> new AbstractIterator<Tuple<BlockPosDimension, ItemStack>>() {
            private BlockPosDimension.MutableBPD pos;
            private Tuple<BlockPosDimension, ItemStack> out;

            @Override
            protected Tuple<BlockPosDimension, ItemStack> computeNext() {
                if ( pos == null ) {
                    pos = new BlockPosDimension.MutableBPD(x1, y1, z1, dimension, facing);
                    out = new Tuple<>(pos, stack);
                    return out;
                } else if ( pos.getX() == x2 && pos.getY() == y2 && pos.getZ() == z2 )
                    return endOfData();

                if ( pos.getX() < x2 )
                    pos.incrementX();
                else if ( pos.getY() < y2 ) {
                    pos.setX(x1);
                    pos.incrementY();
                } else if ( pos.getZ() < z2 ) {
                    pos.setX(x1);
                    pos.setY(y1);
                    pos.incrementZ();
                }

                return out;
            }
        };
    }

}
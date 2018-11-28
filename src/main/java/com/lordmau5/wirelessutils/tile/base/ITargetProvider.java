package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraftforge.common.DimensionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ITargetProvider {

    /**
     * Method to be called when the state has changed and
     * any internally cached target list should be rebuilt.
     */
    void calculateTargets();

    /**
     * Fetch a list of potential targets.
     *
     * @return List of target locations.
     */
    Iterable<BlockPosDimension> getTargets();

    /**
     * Calculate the distance between two positions, adding a penalty of 1000 for
     * targets in other dimensions and using dimensional scale factors for correct
     * calculations within the nether.
     *
     * @param origin The first position
     * @param target The second position
     * @return The distance squared in blocks between the two positions.
     */
    static long calculateDistance(BlockPosDimension origin, BlockPosDimension target) {
        int x1, x2, y1, y2, z1, z2, extra;
        x1 = origin.getX();
        y1 = origin.getY();
        z1 = origin.getZ();
        x2 = target.getX();
        y2 = target.getY();
        z2 = target.getZ();

        if ( origin.getDimension() != target.getDimension() ) {
            double originFactor = DimensionManager.getProvider(origin.getDimension()).getMovementFactor();
            double targetFactor = DimensionManager.getProvider(target.getDimension()).getMovementFactor();
            double factor = originFactor / targetFactor;

            x1 *= factor;
            y1 *= factor;
            z1 *= factor;

            extra = 1000;
        } else
            extra = 0;

        int dX = x1 - x2;
        int dY = y1 - y2;
        int dZ = z1 - z2;

        return dX * dX + dY * dY + dZ * dZ + extra;
    }

    /**
     * Sort a list of targets by their distance from the origin.
     *
     * @param origin The origin
     * @param list   The list of targets
     */
    static void sortTargetList(BlockPosDimension origin, List<BlockPosDimension> list) {
        Map<BlockPosDimension, Long> distanceMap = new HashMap<>();

        Collections.sort(list, (o1, o2) -> {
            long d1, d2;
            if ( distanceMap.containsKey(o1) )
                d1 = distanceMap.get(o1);
            else {
                d1 = calculateDistance(origin, o1);
                distanceMap.put(o1, d1);
            }

            if ( distanceMap.containsKey(o2) )
                d2 = distanceMap.get(o2);
            else {
                d2 = calculateDistance(origin, o2);
                distanceMap.put(o2, d2);
            }

            if ( d1 < d2 )
                return -1;
            else if ( d1 > d2 )
                return 1;

            // If the distance is the same, fall back to comparing the raw
            // positions so that sorting behavior remains consistent.
            return o1.compareTo(o2);
        });
    }
}

package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ITargetProvider {

    /**
     * Method to be called by the Worker before it starts to
     * re-build the target cache. Used for resetting state.
     */
    void onTargetCacheRebuild();

    /**
     * Method to be called when the state has changed and
     * any internally cached target list should be rebuilt.
     */
    void calculateTargets();

    /**
     * Fetch a list of potential block/tile targets.
     *
     * @return List of target locations.
     */
    Iterable<Tuple<BlockPosDimension, ItemStack>> getTargets();

    /**
     * Fetch a list of potential entity targets.
     *
     * @return List of target entities.
     */
    Iterable<Tuple<Entity, ItemStack>> getEntityTargets();

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
            World originWorld = DimensionManager.getWorld(origin.getDimension(), false);
            World targetWorld = DimensionManager.getWorld(target.getDimension(), false);

            double originFactor = (originWorld == null || originWorld.provider == null) ? 0 : originWorld.provider.getMovementFactor();
            double targetFactor = (targetWorld == null || targetWorld.provider == null) ? 0 : targetWorld.provider.getMovementFactor();

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
    static void sortTargetList(BlockPosDimension origin, List<Tuple<BlockPosDimension, ItemStack>> list) {
        Map<BlockPosDimension, Long> distanceMap = new HashMap<>();

        list.sort((val1, val2) -> {
            BlockPosDimension o1 = val1.getFirst();
            BlockPosDimension o2 = val2.getFirst();

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

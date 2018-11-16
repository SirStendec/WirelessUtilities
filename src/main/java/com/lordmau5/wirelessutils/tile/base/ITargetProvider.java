package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;

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
}

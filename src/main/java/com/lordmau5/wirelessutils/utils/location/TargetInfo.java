package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.MoreObjects;

public class TargetInfo {

    public final BlockPosDimension pos;
    public boolean processBlock;

    public boolean processInventory;
    public boolean useSingleChest = false;
    public int liveSlots;
    public int[] slots;

    public TargetInfo(BlockPosDimension pos) {
        this.pos = pos;
    }

    public MoreObjects.ToStringHelper getStringBuilder() {
        return MoreObjects.toStringHelper(this)
                .add("pos", pos)
                .add("pBlock", processBlock)
                .add("pInv", processInventory)
                .add("singleChest", useSingleChest)
                .add("liveSlots", liveSlots)
                .add("slots", slots);
    }

    @Override
    public String toString() {
        return getStringBuilder().toString();
    }
}
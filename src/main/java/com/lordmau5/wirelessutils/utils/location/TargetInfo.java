package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.MoreObjects;
import net.minecraft.tileentity.TileEntity;

public class TargetInfo {

    public final BlockPosDimension pos;

    public TileEntity tile;

    public boolean processBlock;
    public boolean processTile;
    public boolean processInventory;

    public boolean useSingleChest = false;
    public int liveSlots;
    public int[] slots;

    public TargetInfo(BlockPosDimension pos, TileEntity tile) {
        this.pos = pos;
        this.tile = tile;
    }

    public MoreObjects.ToStringHelper getStringBuilder() {
        return MoreObjects.toStringHelper(this)
                .add("pos", pos)
                .add("pBlock", processBlock)
                .add("pTile", processTile)
                .add("pInv", processInventory)
                .add("tile", tile)
                .add("singleChest", useSingleChest)
                .add("liveSlots", liveSlots)
                .add("slots", slots);
    }

    @Override
    public String toString() {
        return getStringBuilder().toString();
    }
}
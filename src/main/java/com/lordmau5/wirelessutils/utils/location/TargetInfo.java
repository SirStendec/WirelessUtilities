package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class TargetInfo {

    public final BlockPosDimension pos;

    public TileEntity tile;
    public Entity entity;

    public boolean processBlock;
    public boolean processTile;
    public boolean processInventory;
    public boolean processEntity;

    public boolean useSingleChest = false;
    public long lastEffect = 0;
    public int liveSlots;
    public int[] slots;

    public TargetInfo(BlockPosDimension pos, TileEntity tile, Entity entity) {
        this.pos = pos;
        this.tile = tile;
        this.entity = entity;
    }

    public MoreObjects.ToStringHelper getStringBuilder() {
        return MoreObjects.toStringHelper(this)
                .add("pos", pos)
                .add("pBlock", processBlock)
                .add("pTile", processTile)
                .add("pInv", processInventory)
                .add("pEnt", processEntity)
                .add("tile", tile)
                .add("entity", entity)
                .add("effect", lastEffect)
                .add("singleChest", useSingleChest)
                .add("liveSlots", liveSlots)
                .add("slots", slots);
    }

    @Override
    public String toString() {
        return getStringBuilder().toString();
    }
}
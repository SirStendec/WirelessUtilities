package com.lordmau5.repack.codechicken.lib.bakedmodel;

import net.minecraft.client.renderer.block.model.ItemOverrideList;

/**
 * Created by covers1624 on 19/01/19.
 */
public class OverrideListModel extends DummyModel {

    private final ItemOverrideList overrideList;

    public OverrideListModel(ItemOverrideList overrideList) {
        this.overrideList = overrideList;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrideList;
    }
}

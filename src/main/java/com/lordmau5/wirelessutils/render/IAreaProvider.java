package com.lordmau5.wirelessutils.render;

import com.lordmau5.wirelessutils.utils.location.BlockArea;

public interface IAreaProvider {
    boolean shouldRenderAreas();

    Iterable<BlockArea> getRenderedAreas();
}

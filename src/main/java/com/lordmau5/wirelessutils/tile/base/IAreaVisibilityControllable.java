package com.lordmau5.wirelessutils.tile.base;

public interface IAreaVisibilityControllable {

    boolean usesDefaultColor();

    boolean shouldRenderAreas();

    void enableRenderAreas(boolean enabled);

    void setDefaultColor(int color);

    int getDefaultColor();

}

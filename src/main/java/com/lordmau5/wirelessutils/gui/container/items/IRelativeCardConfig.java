package com.lordmau5.wirelessutils.gui.container.items;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public interface IRelativeCardConfig {

    int getX();

    int getY();

    int getZ();

    @Nullable
    EnumFacing getFacing();

    boolean setX(int x);

    boolean setY(int y);

    boolean setZ(int z);

    boolean setFacing(@Nullable EnumFacing facing);

    boolean allowNullFacing();

    @Nullable
    Vec3d getVector();

}

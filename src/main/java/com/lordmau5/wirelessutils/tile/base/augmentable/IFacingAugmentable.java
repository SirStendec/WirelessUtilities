package com.lordmau5.wirelessutils.tile.base.augmentable;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface IFacingAugmentable {

    void setFacingAugmented(boolean augmented, @Nullable EnumFacing facing);

}

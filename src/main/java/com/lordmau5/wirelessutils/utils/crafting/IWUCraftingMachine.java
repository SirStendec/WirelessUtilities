package com.lordmau5.wirelessutils.utils.crafting;

import javax.annotation.Nullable;

public interface IWUCraftingMachine {

    @Nullable
    IWURecipe getCurrentRecipe();

    float getCraftingProgress();

    boolean canCraft();

}

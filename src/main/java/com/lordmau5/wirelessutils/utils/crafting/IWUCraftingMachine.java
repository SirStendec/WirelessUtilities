package com.lordmau5.wirelessutils.utils.crafting;

import javax.annotation.Nullable;

public interface IWUCraftingMachine {

    @Nullable
    String getRecipeCategory();

    @Nullable
    IWURecipe getCurrentRecipe();

    float getCraftingProgress();

    boolean canCraft();

}

package com.lordmau5.wirelessutils.tile.base.augmentable;

public interface IBudgetInfoProvider {

    int getBudgetCurrent();

    int getBudgetMax();

    int getBudgetPerTick();

    int getBudgetPerOperation();
}

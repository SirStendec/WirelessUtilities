package com.lordmau5.wirelessutils.tile.base;

public interface IEnergyHistory {

    void syncHistory();

    byte getHistoryTick();

    long[] getEnergyHistory();

}

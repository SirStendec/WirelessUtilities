package com.lordmau5.wirelessutils.tile.base;

public interface IConfigurableWorldTickRate {

    int getActualWorldTickRate();

    int getWorldTickRate();

    int getMinWorldTickRate();

    default int getMaxWorldTickRate() {
        return Byte.MAX_VALUE;
    }

    void setWorldTickRate(int rate);

    boolean hasWorldTick();

}
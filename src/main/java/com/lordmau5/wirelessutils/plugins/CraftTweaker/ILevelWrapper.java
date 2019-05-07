package com.lordmau5.wirelessutils.plugins.CraftTweaker;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenSetter;

@SuppressWarnings("unused")
@ZenClass("mods.wirelessutils.ILevelWrapper")
@ZenRegister
public interface ILevelWrapper {

    @ZenSetter("name")
    void setName(String name);

    @ZenGetter("name")
    String getName();

    @ZenSetter("augmentSlots")
    void setAugmentSlots(int slots);

    @ZenGetter("augmentSlots")
    int getAugmentSlots();

    @ZenSetter("rarity")
    void setRarity(int rarity);

    @ZenGetter("rarity")
    int getRarity();

    @ZenSetter("creative")
    void setCreative(boolean creative);

    @ZenGetter("creative")
    boolean getCreative();

    @ZenSetter("color")
    void setColor(int color);

    @ZenGetter("color")
    int getColor();

    @ZenSetter("maxChargerCapacity")
    void setMaxChargerCapacity(long capacity);

    @ZenGetter("maxChargerCapacity")
    long getMaxChargerCapacity();

    @ZenSetter("maxChargerTransfer")
    void setMaxChargerTransfer(long transfer);

    @ZenGetter("maxChargerTransfer")
    long getMaxChargerTransfer();

    @ZenSetter("craftingTPT")
    void setCraftingTPT(int ticks);

    @ZenGetter("craftingTPT")
    int getCraftingTPT();

    @ZenSetter("baseEnergyPerOperation")
    void setBaseEnergyPerOperation(int energy);

    @ZenGetter("baseEnergyPerOperation")
    int getBaseEnergyPerOperation();

    @ZenSetter("maxEnergyCapacity")
    void setMaxEnergyCapacity(long capacity);

    @ZenGetter("maxEnergyCapacity")
    long getMaxEnergyCapacity();

    @ZenSetter("maxCondenserTransfer")
    void setMaxCondenserTransfer(int transfer);

    @ZenGetter("maxCondenserTransfer")
    int getMaxCondenserTransfer();

    @ZenSetter("maxCondenserCapacity")
    void setMaxCondenserCapacity(int capacity);

    @ZenGetter("maxCondenserCapacity")
    int getMaxCondenserCapacity();

    @ZenSetter("maxItemsPerTick")
    void setMaxItemsPerTick(int items);

    @ZenGetter("maxItemsPerTick")
    int getMaxItemsPerTick();

    @ZenGetter("index")
    int getIndex();
}

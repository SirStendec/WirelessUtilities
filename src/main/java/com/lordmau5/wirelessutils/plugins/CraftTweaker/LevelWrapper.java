package com.lordmau5.wirelessutils.plugins.CraftTweaker;

import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.item.EnumRarity;

public class LevelWrapper implements ILevelWrapper {
    private final Level level;

    LevelWrapper(Level level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "<level:" + getIndex() + ">";
    }

    @Override
    public int hashCode() {
        return level.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof LevelWrapper )
            return ((LevelWrapper) obj).level.equals(level);
        return false;
    }

    public void setName(String name) {
        level.name = name;
    }

    public String getName() {
        return level.name;
    }

    public void setAugmentSlots(int slots) {
        if ( slots < 0 )
            slots = 0;
        else if ( slots > 9 )
            slots = 9;

        level.augmentSlots = slots;
    }

    public int getAugmentSlots() {
        return level.augmentSlots;
    }

    public void setRarity(int rarity) {
        EnumRarity[] values = EnumRarity.values();
        if ( rarity < 0 )
            rarity = 0;

        if ( rarity >= values.length )
            rarity = values.length - 1;

        level.rarity = values[rarity];
    }

    public int getRarity() {
        return level.rarity.ordinal();
    }

    public void setCreative(boolean creative) {
        level.isCreative = creative;
    }

    public boolean getCreative() {
        return level.isCreative;
    }

    public void setColor(int color) {
        level.color = color;
    }

    public int getColor() {
        return level.color;
    }

    public void setMaxChargerCapacity(long capacity) {
        level.maxChargerCapacity = capacity;
    }

    public long getMaxChargerCapacity() {
        return level.maxChargerCapacity;
    }

    public void setMaxChargerTransfer(long transfer) {
        level.maxChargerTransfer = transfer;
    }

    public long getMaxChargerTransfer() {
        return level.maxChargerTransfer;
    }

    public void setCraftingTPT(int ticks) {
        level.craftingTPT = ticks;
    }

    public int getCraftingTPT() {
        return level.craftingTPT;
    }

    public void setBaseEnergyPerOperation(int energy) {
        level.baseEnergyPerOperation = energy;
    }

    public int getBaseEnergyPerOperation() {
        return level.baseEnergyPerOperation;
    }

    public void setMaxEnergyCapacity(long capacity) {
        level.maxEnergyCapacity = capacity;
    }

    public long getMaxEnergyCapacity() {
        return level.maxEnergyCapacity;
    }

    public void setMaxCondenserTransfer(int transfer) {
        level.maxCondenserTransfer = transfer;
    }

    public int getMaxCondenserTransfer() {
        return level.maxCondenserTransfer;
    }

    public void setMaxCondenserCapacity(int capacity) {
        level.maxCondenserCapacity = capacity;
    }

    public int getMaxCondenserCapacity() {
        return level.maxCondenserCapacity;
    }

    public void setMaxItemsPerTick(int items) {
        level.budgetPerTick = items * level.costPerItem;
        level.maxBudget = level.budgetPerTick;
    }

    public int getMaxItemsPerTick() {
        return level.budgetPerTick / level.costPerItem;
    }

    public void setBudgetPerTick(int budget) {
        level.budgetPerTick = budget;
    }

    public int getBudgetPerTick() {
        return level.budgetPerTick;
    }

    public void setMaxBudget(int budget) {
        level.maxBudget = budget;
    }

    public int getMaxBudget() {
        return level.maxBudget;
    }

    public void setCostPerItem(int cost) {
        level.costPerItem = cost;
    }

    public int getCostPerItem() {
        return level.costPerItem;
    }

    public int getIndex() {
        return level.toInt();
    }

}

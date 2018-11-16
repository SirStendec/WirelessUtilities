package com.lordmau5.wirelessutils.utils;

import net.minecraft.nbt.NBTTagCompound;

public class BigEnergyStorage implements cofh.redstoneflux.api.IEnergyStorage {

    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;

    public BigEnergyStorage(long capacity) {
        this(capacity, capacity, capacity);
    }

    public BigEnergyStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public BigEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    // Saving and Loading

    public BigEnergyStorage readFromNBT(NBTTagCompound tag) {
        energy = tag.getLong("Energy");
        if ( energy > capacity )
            energy = capacity;

        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        if ( energy < 0 )
            energy = 0;

        if ( energy > 0 )
            tag.setLong("Energy", energy);

        return tag;
    }


    public BigEnergyStorage setCapacity(long capacity) {
        this.capacity = capacity;
        if ( energy > capacity )
            energy = capacity;

        return this;
    }

    public BigEnergyStorage setMaxTransfer(long maxTransfer) {
        setMaxExtract(maxTransfer);
        setMaxReceive(maxTransfer);
        return this;
    }

    public BigEnergyStorage setMaxReceive(long maxReceive) {
        this.maxReceive = maxReceive;
        return this;
    }

    public BigEnergyStorage setMaxExtract(long maxExtract) {
        this.maxExtract = maxExtract;
        return this;
    }

    public int getMaxReceive() {
        if ( maxReceive > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;

        return (int) maxReceive;
    }

    public int getMaxExtract() {
        if ( maxExtract > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;

        return (int) maxExtract;
    }

    public long getFullMaxReceive() {
        return maxReceive;
    }

    public long getFullMaxExtract() {
        return maxExtract;
    }

    public BigEnergyStorage setEnergyStored(long energy) {
        if ( energy > capacity )
            energy = capacity;
        else if ( energy < 0 )
            energy = 0;

        this.energy = energy;
        return this;
    }

    public BigEnergyStorage modifyEnergyStored(long value) {
        energy += value;
        if ( energy > capacity )
            energy = capacity;
        if ( energy < 0 )
            energy = 0;

        return this;
    }


    // Forge API

    public long receiveEnergy(long maxReceive, boolean simulate) {
        long received = Math.min(capacity - energy, Math.min(maxReceive, this.maxReceive));

        if ( !simulate )
            energy += received;

        return received;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) receiveEnergy((long) maxReceive, simulate);
    }

    public long extractEnergy(long maxExtract, boolean simulate) {
        long extracted = Math.min(energy, Math.min(maxExtract, this.maxExtract));
        if ( !simulate )
            energy -= extracted;

        return extracted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return (int) extractEnergy((long) maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        if ( energy > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;

        return (int) energy;
    }

    @Override
    public int getMaxEnergyStored() {
        if ( capacity > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;

        return (int) capacity;
    }

    public long getFullEnergyStored() {
        return energy;
    }

    public long getFullMaxEnergyStored() {
        return capacity;
    }
}

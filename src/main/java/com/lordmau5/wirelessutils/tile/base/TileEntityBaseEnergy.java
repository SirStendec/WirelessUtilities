package com.lordmau5.wirelessutils.tile.base;

import cofh.api.tileentity.IEnergyInfo;
import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.utils.BigEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public abstract class TileEntityBaseEnergy extends TileEntityBaseMachine implements IEnergyInfo, IEnergyStorage {
    private BigEnergyStorage energyStorage;

    protected long energyPerTick = 0;

    protected TileEntityBaseEnergy() {
        super();

        energyStorage = new BigEnergyStorage(calculateEnergyCapacity());
        energyStorage.setMaxTransfer(calculateEnergyMaxTransfer());
    }

    @Override
    public void updateLevel() {
        super.updateLevel();

        energyStorage.setCapacity(calculateEnergyCapacity());
        energyStorage.setMaxTransfer(calculateEnergyMaxTransfer());
    }

    public long calculateEnergyCapacity() {
        return level.maxChargerCapacity;
    }

    public long calculateEnergyMaxTransfer() {
        return level.maxChargerTransfer;
    }


    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addBool(isActive);
        payload.addLong(getFullMaxEnergyStored());
        payload.addLong(getFullEnergyStored());
        payload.addLong(getFullMaxExtract());
        payload.addLong(getFullMaxReceive());
        payload.addLong(energyPerTick);
        return payload;
    }

    @Override
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);

        isActive = payload.getBool();
        energyStorage.setCapacity(payload.getLong());
        energyStorage.setEnergyStored(payload.getLong());
        energyStorage.setMaxExtract(payload.getLong());
        energyStorage.setMaxReceive(payload.getLong());
        energyPerTick = payload.getLong();
    }

    @Override
    public int getInfoEnergyPerTick() {
        if ( energyPerTick > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;

        return (int) energyPerTick;
    }

    @Override
    public int getInfoMaxEnergyPerTick() {
        return getMaxExtract();
    }

    public long getFullMaxEnergyPerTick() {
        return getFullMaxExtract();
    }

    public long getFullEnergyPerTick() {
        return energyPerTick;
    }

    @Override
    public int getInfoEnergyStored() {
        return getEnergyStored();
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        super.writeExtraToNBT(tag);
        writeEnergyToNBT(tag);
        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        readEnergyFromNBT(tag);
    }

    public void writeEnergyToNBT(NBTTagCompound tag) {
        if ( getEnergyStored() > 0 )
            getEnergyStorage().writeToNBT(tag);
    }

    public void readEnergyFromNBT(NBTTagCompound tag) {
        getEnergyStorage().readFromNBT(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY
                ? CapabilityEnergy.ENERGY.cast(this)
                : super.getCapability(capability, facing);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if ( isCreative )
            return 0;

        return getEnergyStorage().receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if ( isCreative )
            return Math.min(getMaxExtract(), maxExtract);

        return getEnergyStorage().extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        if ( isCreative )
            return Integer.MAX_VALUE;

        return getEnergyStorage().getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        if ( isCreative )
            return Integer.MAX_VALUE;

        return getEnergyStorage().getMaxEnergyStored();
    }

    public int getMaxExtract() {
        return getEnergyStorage().getMaxExtract();
    }

    public int getMaxReceive() {
        return getEnergyStorage().getMaxReceive();
    }

    public long getFullMaxExtract() {
        return getEnergyStorage().getFullMaxExtract();
    }

    public long getFullMaxReceive() {
        return getEnergyStorage().getFullMaxReceive();
    }

    public long getFullMaxEnergyStored() {
        if ( isCreative )
            return Long.MAX_VALUE;

        return getEnergyStorage().getFullMaxEnergyStored();
    }

    public long getFullEnergyStored() {
        if ( isCreative )
            return Long.MAX_VALUE;

        return getEnergyStorage().getFullEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public BigEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}

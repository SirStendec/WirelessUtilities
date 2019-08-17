package com.lordmau5.wirelessutils.tile.base;

import cofh.api.tileentity.IEnergyInfo;
import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import com.lordmau5.wirelessutils.utils.BigEnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public abstract class TileEntityBaseEnergy extends TileEntityBaseMachine implements IEnergyInfo, IEnergyStorage, IEnergyHistory {
    private final BigEnergyStorage energyStorage;

    public static final byte PKT_HISTORY_REQUEST = 100;
    public static final byte PKT_HISTORY_SYNC = 101;

    protected long energyHistory[] = new long[40];
    protected byte energyHistoryPos = -1;
    protected boolean energyHistoryRequested = false;

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


    /* Energy History */

    public void saveEnergyHistory(long energy) {
        energyHistoryPos++;
        if ( energyHistoryPos >= 40 )
            energyHistoryPos = 0;

        energyHistory[energyHistoryPos] = energy;
    }

    public byte getHistoryTick() {
        return energyHistoryPos;
    }

    @Override
    public long[] getEnergyHistory() {
        long[] out = new long[40];

        int initial = energyHistoryPos + 1;
        int trailing = energyHistory.length - initial;

        System.arraycopy(energyHistory, initial, out, 0, trailing);
        System.arraycopy(energyHistory, 0, out, trailing, initial);

        return out;
    }

    /* Packets */

    @Override
    public void handleTileInfoPacket(PacketBase payload, boolean isServer, EntityPlayer thePlayer) {
        byte type = payload.getByte();

        TilePacketID[] packets = TilePacketID.values();
        if ( type < packets.length ) {
            switch (packets[type]) {
                case S_GUI:
                    handleGuiPacket(payload);
                    return;
                case S_FLUID:
                    handleFluidPacket(payload);
                    return;
                case C_ACCESS:
                    handleAccessPacket(payload);
                    return;
                case C_CONFIG:
                    handleConfigPacket(payload);
                    return;
                case C_MODE:
                    handleModePacket(payload);
                    return;
                default:
            }
        }

        handleTileInfoPackageDelegate(type, payload, isServer, thePlayer);
    }

    public void handleTileInfoPackageDelegate(byte type, PacketBase payload, boolean isServer, EntityPlayer thePlayer) {
        if ( type == PKT_HISTORY_REQUEST ) {
            handleEnergyHistoryRequestPacket(payload, thePlayer);
            return;
        } else if ( type == PKT_HISTORY_SYNC ) {
            handleEnergyHistorySyncPacket(payload);
        }
    }

    public PacketBase getEnergyHistoryRequestPacket() {
        PacketBase payload = PacketTileInfo.newPacket(this);
        payload.addByte(PKT_HISTORY_REQUEST);
        return payload;
    }

    protected void handleEnergyHistoryRequestPacket(PacketBase payload, EntityPlayer player) {
        PacketBase syncPacket = getEnergyHistorySyncPacket();
        if ( syncPacket != null )
            PacketHandler.sendTo(syncPacket, player);
    }

    public PacketBase getEnergyHistorySyncPacket() {
        PacketBase payload = PacketTileInfo.newPacket(this);
        payload.addByte(PKT_HISTORY_SYNC);

        if ( world != null && !world.isRemote ) {
            for (long energy : energyHistory)
                payload.addLong(energy);

            payload.addByte(energyHistoryPos);
        }

        return payload;
    }

    protected void handleEnergyHistorySyncPacket(PacketBase payload) {
        for (int i = 0; i < energyHistory.length; i++)
            energyHistory[i] = payload.getLong();

        energyHistoryPos = payload.getByte();
    }

    public void syncHistory() {
        if ( world == null || !world.isRemote )
            return;

        PacketBase requestPacket = getEnergyHistoryRequestPacket();
        if ( requestPacket != null )
            PacketHandler.sendToServer(requestPacket);
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

        if ( world != null && world.isRemote )
            saveEnergyHistory(energyPerTick);
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

        if ( !simulate )
            markChunkDirty();
        return getEnergyStorage().receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if ( isCreative )
            return Math.min(getMaxExtract(), maxExtract);

        if ( !simulate )
            markChunkDirty();

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

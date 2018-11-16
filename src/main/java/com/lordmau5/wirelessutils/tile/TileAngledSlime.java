package com.lordmau5.wirelessutils.tile;

import cofh.core.block.TileCore;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileAngledSlime extends TileCore implements ITilePacketHandler {

    private int rotation = 2;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if ( oldState.getBlock() != newState.getBlock() )
            return super.shouldRefresh(world, pos, oldState, newState);

        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("Rotation", (byte) rotation);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if ( compound.hasKey("Rotation") )
            rotation = compound.getByte("Rotation");
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        if ( rotation > 3 )
            rotation = 0;
        else if ( rotation < 0 )
            rotation = 3;

        if ( rotation == this.rotation )
            return;

        this.rotation = rotation;
        if ( world.isRemote ) {
            markChunkDirty();
            callBlockUpdate();
        }
    }

    public void rotate(int value) {
        setRotation(rotation + value);
    }

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(rotation);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        rotation = payload.getByte();
    }
}

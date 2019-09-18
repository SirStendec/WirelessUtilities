package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;

public abstract class BasePacket extends PacketBase {

    public void sendToNearbyPlayers(@Nonnull BlockPosDimension pos) {
        sendToNearbyPlayers(pos, pos.getDimension());
    }

    public void sendToNearbyPlayers(@Nonnull BlockPos pos, int dimension) {
        World world = DimensionManager.getWorld(dimension, false);
        if ( world != null )
            PacketHandler.sendToAllAround(this, world, pos.getX(), pos.getY(), pos.getZ());
    }

    public void sendToNearbyPlayers(@Nonnull TileEntity tile) {
        PacketHandler.sendToAllAround(this, tile);
    }

    public void sendToNearbyWorkers(@Nonnull BlockPosDimension pos) {
        sendToNearbyWorkers(pos, pos.getDimension());
    }

    public void sendToNearbyWorkers(@Nonnull TileEntity tile) {
        BlockPos pos = tile.getPos();
        World world = tile.getWorld();
        if ( pos == null || world == null || world.provider == null )
            return;

        sendToNearbyWorkers(pos, world.provider.getDimension());
    }

    public void sendToNearbyWorkers(@Nonnull BlockPos pos, int dimension) {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if ( player.dimension == dimension && pos.distanceSq(player.posX, player.posY, player.posZ) < 1024 ) {
                if ( ModItems.itemGlasses.isPlayerWearing(player) )
                    PacketHandler.sendTo(this, player);
            }
        }
    }

}

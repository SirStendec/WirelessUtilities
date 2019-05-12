package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class PacketParticleLine extends PacketBase {

    public static void initialize() {
        PacketHandler.INSTANCE.registerPacket(PacketParticleLine.class);
    }

    public PacketParticleLine() {
    }

    public PacketParticleLine(EnumParticleTypes particle, boolean longDistance, float x, float y, float z, float xDelta, float yDelta, float zDelta, int count, float xOffset, float yOffset, float zOffset, int... arguments) {
        addInt(particle.getParticleID());
        addBool(longDistance);
        addFloat(x);
        addFloat(y);
        addFloat(z);
        addFloat(xDelta);
        addFloat(yDelta);
        addFloat(zDelta);
        addInt(count);
        addFloat(xOffset);
        addFloat(yOffset);
        addFloat(zOffset);

        addByte(arguments.length);
        for (int i : arguments)
            addInt(i);
    }


    public void handlePacket(EntityPlayer player, boolean isServer) {
        if ( isServer )
            return;

        World world = player.getEntityWorld();
        if ( world == null )
            return;

        EnumParticleTypes particle = EnumParticleTypes.getParticleFromId(getInt());
        if ( particle == null )
            particle = EnumParticleTypes.BARRIER;

        boolean longDistance = getBool();
        float x = getFloat();
        float y = getFloat();
        float z = getFloat();
        float xDelta = getFloat();
        float yDelta = getFloat();
        float zDelta = getFloat();
        int count = getInt();
        float xOffset = getFloat();
        float yOffset = getFloat();
        float zOffset = getFloat();

        int length = getByte();
        int[] arguments = new int[length];
        if ( length > 0 ) {
            for (int i = 0; i < length; i++)
                arguments[i] = getInt();
        }

        try {
            for (int i = 0; i < count; i++) {
                x += xDelta;
                y += yDelta;
                z += zDelta;

                world.spawnParticle(particle, longDistance, x, y, z, xOffset, yOffset, zOffset, arguments);
            }

        } catch (Throwable err) {
            /* An error ~ */
        }

    }
}

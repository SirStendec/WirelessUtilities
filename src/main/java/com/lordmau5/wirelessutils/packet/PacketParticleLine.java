package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketParticleLine extends BasePacket {

    public static void initialize() {
        PacketHandler.INSTANCE.registerPacket(PacketParticleLine.class);
    }

    public PacketParticleLine() {

    }

    @Nullable
    public static PacketParticleLine betweenPoints(EnumParticleTypes particle, boolean requireGlasses, @Nonnull BlockPos origin, @Nullable EnumFacing originFace, @Nonnull BlockPos target, @Nullable EnumFacing targetFace, float frequency, float offsetX, float offsetY, float offsetZ, int... arguments) {
        float x = 0.5F;
        float y = 0.5F;
        float z = 0.5F;

        if ( targetFace != null ) {
            EnumFacing.AxisDirection direction = targetFace.getAxisDirection();

            switch (targetFace.getAxis()) {
                case X:
                    x = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
                    break;
                case Y:
                    y = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
                    break;
                case Z:
                    z = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
                default:
            }
        }

        return betweenPoints(
                particle, requireGlasses,
                origin, originFace,
                target.getX() + x,
                target.getY() + y,
                target.getZ() + z,
                frequency, offsetX, offsetY, offsetZ, arguments
        );
    }

    @Nullable
    public static PacketParticleLine betweenPoints(EnumParticleTypes particle, boolean requireGlasses, @Nonnull BlockPos origin, @Nullable EnumFacing originFace, @Nonnull Entity target, float frequency, float offsetX, float offsetY, float offsetZ, int... arguments) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        double xSize = box == null ? target.width : box.maxX - box.minX;
        double ySize = box == null ? target.height : box.maxY - box.minY;
        double zSize = box == null ? target.width : box.maxZ - box.minZ;

        return betweenPoints(
                particle, requireGlasses,
                origin, originFace,
                (float) (target.posX + xSize / 2),
                (float) (target.posY + ySize / 2),
                (float) (target.posZ + zSize / 2),
                frequency, offsetX, offsetY, offsetX, arguments
        );
    }

    @Nullable
    public static PacketParticleLine betweenPoints(EnumParticleTypes particle, boolean requireGlasses, @Nonnull BlockPos origin, @Nullable EnumFacing originFace, float x, float y, float z, float frequency, float offsetX, float offsetY, float offsetZ, int... arguments) {
        float originX = 0.5F;
        float originY = 0.5F;
        float originZ = 0.5F;

        if ( originFace != null ) {
            EnumFacing.AxisDirection direction = originFace.getAxisDirection();

            switch (originFace.getAxis()) {
                case X:
                    originX = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
                    break;
                case Y:
                    originY = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
                    break;
                case Z:
                    originZ = direction == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
                default:
            }
        }

        return betweenPoints(
                particle, requireGlasses,
                origin.getX() + originX,
                origin.getY() + originY,
                origin.getZ() + originZ,
                x, y, z,
                frequency, offsetX, offsetY, offsetZ, arguments
        );
    }

    @Nullable
    public static PacketParticleLine betweenPoints(EnumParticleTypes particle, boolean requireGlasses, float x1, float y1, float z1, float x2, float y2, float z2, float frequency, float offsetX, float offsetY, float offsetZ, int... arguments) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
        if ( distance == 0 )
            return null;

        int points = (int) Math.floor(distance * frequency);

        return new PacketParticleLine(
                particle, requireGlasses,
                x1, y1, z1,
                (x2 - x1) / points,
                (y2 - y1) / points,
                (z2 - z1) / points,
                points,
                offsetX, offsetY, offsetZ,
                arguments
        );
    }


    public PacketParticleLine(EnumParticleTypes particle, boolean requireGlasses, float x, float y, float z, float xDelta, float yDelta, float zDelta, int count, float xOffset, float yOffset, float zOffset, int... arguments) {
        addInt(particle.getParticleID());
        addBool(requireGlasses);
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

        final World world = player.getEntityWorld();
        if ( world == null )
            return;

        EnumParticleTypes particle = EnumParticleTypes.getParticleFromId(getInt());
        if ( particle == null )
            particle = EnumParticleTypes.BARRIER;

        final boolean requireGlasses = getBool();
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

        if ( requireGlasses && !ModItems.itemGlasses.isPlayerWearing(player) )
            return;

        // TODO: Determine if these are actually work particles, if we ever
        // use this packet for more things.
        if ( !ModConfig.rendering.particlesEnabled )
            return;

        try {
            for (int i = 0; i < count; i++) {
                x += xDelta;
                y += yDelta;
                z += zDelta;

                world.spawnParticle(particle, false, x, y, z, xOffset, yOffset, zOffset, arguments);
            }

        } catch (Throwable err) {
            /* An error ~ */
        }

    }
}

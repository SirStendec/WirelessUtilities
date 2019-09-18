package com.lordmau5.wirelessutils.packet;

import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.render.particles.ParticleSpawner;
import com.lordmau5.wirelessutils.render.particles.WUParticleTypes;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketCustomParticle extends BasePacket {

    public static void initialize() {
        PacketHandler.INSTANCE.registerPacket(PacketCustomParticle.class);
    }

    public PacketCustomParticle() {

    }

    public PacketCustomParticle(WUParticleTypes type, boolean requireGlasses, boolean ignoreRange, float xCoord, float yCoord, float zCoord, float xSpeed, float ySpeed, float zSpeed, int... parameters) {
        addInt(type.ordinal());
        addBool(requireGlasses);
        addBool(ignoreRange);
        addFloat(xCoord);
        addFloat(yCoord);
        addFloat(zCoord);
        addFloat(xSpeed);
        addFloat(ySpeed);
        addFloat(zSpeed);

        addByte(parameters.length);
        for (int i : parameters)
            addInt(i);
    }

    public void handlePacket(EntityPlayer player, boolean isServer) {
        if ( isServer )
            return;

        final WUParticleTypes particle = WUParticleTypes.byIndex(getInt());
        final boolean requireGlasses = getBool();

        if ( requireGlasses && !ModItems.itemGlasses.isPlayerWearing(player) )
            return;

        final boolean ignoreRange = getBool();
        final float x = getFloat();
        final float y = getFloat();
        final float z = getFloat();
        final float speedX = getFloat();
        final float speedY = getFloat();
        final float speedZ = getFloat();

        final int[] parameters = new int[getByte()];
        for (int i = 0; i < parameters.length; i++)
            parameters[i] = getInt();

        ParticleSpawner.spawnParticle(particle, ignoreRange, x, y, z, speedX, speedY, speedZ, parameters);
    }


    // Line

    @Nullable
    public static PacketCustomParticle makeLine(boolean requireGlasses, @Nonnull BlockPos origin, @Nullable EnumFacing originFace, @Nonnull BlockPos target, @Nullable EnumFacing targetFace, int color) {
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

        return makeLine(
                requireGlasses,
                origin, originFace,
                target.getX() + x,
                target.getY() + y,
                target.getZ() + z,
                color
        );
    }

    @Nullable
    public static PacketCustomParticle makeLine(boolean requireGlasses, @Nonnull BlockPos origin, @Nullable EnumFacing originFace, @Nonnull Entity target, int color) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        double xSize = box == null ? target.width : box.maxX - box.minX;
        double ySize = box == null ? target.height : box.maxY - box.minY;
        double zSize = box == null ? target.width : box.maxZ - box.minZ;

        return makeLine(
                requireGlasses,
                origin, originFace,
                (float) (target.posX + xSize / 2),
                (float) (target.posY + ySize / 2),
                (float) (target.posZ + zSize / 2),
                color
        );
    }

    @Nullable
    public static PacketCustomParticle makeLine(boolean requireGlasses, @Nonnull BlockPos origin, @Nullable EnumFacing originFace, float x, float y, float z, int color) {
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

        return makeLine(
                requireGlasses,
                origin.getX() + originX,
                origin.getY() + originY,
                origin.getZ() + originZ,
                x, y, z,
                color
        );
    }

    @Nullable
    public static PacketCustomParticle makeLine(boolean requireGlasses, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        return new PacketCustomParticle(
                WUParticleTypes.LINE,
                requireGlasses,
                false,
                x1, y1, z1,
                x2, y2, z2,
                color
        );
    }
}

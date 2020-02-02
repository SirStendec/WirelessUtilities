package com.lordmau5.wirelessutils.utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderUtils {

    public static int FACE_UP = 0b100000;
    public static int FACE_DOWN = 0b010000;
    public static int FACE_NORTH = 0b001000;
    public static int FACE_EAST = 0b000100;
    public static int FACE_SOUTH = 0b000010;
    public static int FACE_WEST = 0b000001;

    public static int ALL_FACES = 0b111111;


    /**
     * A vertex definition for a simple 2-dimensional quad defined in counter-clockwise order with the top-left origin.
     */
    public static final Vector4f[] SIMPLE_QUAD = {
            new Vector4f(1, 1, 0, 0),
            new Vector4f(1, 0, 0, 0),
            new Vector4f(0, 0, 0, 0),
            new Vector4f(0, 1, 0, 0)
    };

    // add or subtract from the sprites UV location to remove transparent lines in between textures
    private static final float UV_CORRECT = 1F / 16F / 10000;


    /**
     * Rotation algorithm Taken off Max_the_Technomancer from <a href= "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/2772267-tesr-getting-darker-and-lighter-as-it-rotates">here</a>
     *
     * @param face the {@link EnumFacing face} to rotate for
     */
    @SideOnly(Side.CLIENT)
    public static void rotateForFace(final EnumFacing face) {
        GlStateManager.rotate(face == EnumFacing.DOWN ? 0 : face == EnumFacing.UP ? 180F : (face == EnumFacing.NORTH) || (face == EnumFacing.EAST) ? 90F : -90F, face.getAxis() == EnumFacing.Axis.Z ? 1 : 0, 0, face.getAxis() == EnumFacing.Axis.Z ? 0 : 1);
        GlStateManager.rotate(-90, 0, 0, 1);
    }


    /**
     * Put a lot of effort into this, it gets the entities exact (really, really exact) position
     *
     * @param entity       The entity to calculate the position of
     * @param partialTicks The multiplier used to predict where the entity is/will be
     * @return The position of the entity as a Vec3d
     */
    @Nonnull
    public static Vec3d getEntityRenderPos(@Nonnull final Entity entity, @Nonnull final double partialTicks) {
        double flyingMultiplier = 1.825;
        double yFlying = 1.02;
        double yAdd = 0.0784000015258789;

        if ( (entity instanceof EntityPlayer) && ((EntityPlayer) entity).capabilities.isFlying ) {
            flyingMultiplier = 1.1;
            yFlying = 1.67;
            yAdd = 0;
        }

        final double yGround = ((entity.motionY + yAdd) == 0) && (entity.prevPosY > entity.posY) ? entity.posY - entity.prevPosY : 0;
        double xFall = 1;
        if ( flyingMultiplier == 1.825 ) {
            if ( entity.motionX != 0 ) {
                if ( (entity.motionY + yAdd) != 0 ) {
                    xFall = 0.6;
                } else if ( yGround != 0 ) {
                    xFall = 0.6;
                }
            } else {
                xFall = 0.6;
            }
        }

        double zFall = 1;
        if ( flyingMultiplier == 1.825 ) {
            if ( entity.motionZ != 0 ) {
                if ( (entity.motionY + yAdd) != 0 ) {
                    zFall = 0.6;
                } else if ( yGround != 0 ) {
                    zFall = 0.6;
                }
            } else {
                zFall = 0.6;
            }
        }

        final double dX = entity.posX - ((entity.prevPosX - entity.posX) * partialTicks) - ((entity.motionX * xFall) * flyingMultiplier);
        final double dY = entity.posY - yGround - ((entity.prevPosY - entity.posY) * partialTicks) - ((entity.motionY + yAdd) * yFlying);
        final double dZ = entity.posZ - ((entity.prevPosZ - entity.posZ) * partialTicks) - ((entity.motionZ * zFall) * flyingMultiplier);

        return new Vec3d(dX, dY, dZ);
    }

    /**
     * Rotates around X axis based on Pitch input and around Y axis based on Yaw input
     *
     * @param pitch the pitch
     * @param yaw   the yaw
     */
    @SideOnly(Side.CLIENT)
    public static void rotateForPitchYaw(final double pitch, final double yaw) {
        GlStateManager.rotate((float) yaw, 0, 1, 0);
        GlStateManager.rotate((float) pitch, 1, 0, 0);
    }

    /**
     * Gets the pitch rotation between two vectors
     *
     * @param source      the source vector
     * @param destination the destination vector
     * @return the pitch rotation
     */
    public static double getPitch(@Nonnull final Vec3d source, @Nonnull final Vec3d destination) {
        return getPitch(destination.subtract(source).normalize());

    }

    public static double getPitch(@Nonnull final Vec3d vector) {
        return MathHelper.atan2(-vector.y, MathHelper.sqrt(vector.x * vector.x + vector.z * vector.z));
    }

    /**
     * Gets the yaw rotation between two vectors
     *
     * @param source      the source vector
     * @param destination the destination vector
     * @return the yaw rotation
     */
    public static double getYaw(@Nonnull final Vec3d source, @Nonnull final Vec3d destination) {
        return getYaw(destination.subtract(source).normalize());
    }

    public static double getYaw(@Nonnull final Vec3d vector) {
        return MathHelper.atan2(vector.x, vector.z);
    }

    /**
     * @param red   the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
     * @param green the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
     * @param blue  the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
     * @return the color in ARGB format
     */
    public static long color(int red, int green, int blue) {

        red = MathHelper.clamp(red, 0x00, 0xFF);
        green = MathHelper.clamp(green, 0x00, 0xFF);
        blue = MathHelper.clamp(blue, 0x00, 0xFF);

        final int alpha = 0xFF;

        // 0x alpha red green blue
        // 0xaarrggbb

        // int colorRGBA = 0;
        // colorRGBA |= red << 16;
        // colorRGBA |= green << 8;
        // colorRGBA |= blue << 0;
        // colorRGBA |= alpha << 24;

        return blue | red << 16 | green << 8 | alpha << 24;

    }

    /**
     * @param red   the red value of the color, 0F and 1F
     * @param green the green value of the color, 0F and 1F
     * @param blue  the blue value of the color, 0F and 1F
     * @return the color in ARGB format
     */
    public static long colorf(final float red, final float green, final float blue) {
        final int redInt = Math.max(0, Math.min(255, Math.round(red * 255)));
        final int greenInt = Math.max(0, Math.min(255, Math.round(green * 255)));
        final int blueInt = Math.max(0, Math.min(255, Math.round(blue * 255)));
        return color(redInt, greenInt, blueInt);
    }

    // Below are some helper methods to upload data to the buffer for use by FastTESRs

    public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
        return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
    }

    public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
        return packedLightmapCoords & 0xFFFF; // get lower 4 bytes
    }

    /**
     * Renders a simple 2 dimensional quad at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderSimpleQuad(Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        renderCustomQuad(SIMPLE_QUAD, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);
    }

    /**
     * Render a cube at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param xMin               The minimum X coordinate as 16ths of a block.
     * @param xMax               The maximum X coordinate as 16ths of a block.
     * @param yMin               The minimum Y coordinate as 16ths of a block.
     * @param yMax               The maximum Y coordinate as 16ths of a block.
     * @param zMin               The minimum Z coordinate as 16ths of a block.
     * @param zMax               The maximum Z coordinate as 16ths of a block.
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderCube(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        renderCube(
                (float) xMin / 16F,
                (float) xMax / 16F,
                (float) yMin / 16F,
                (float) yMax / 16F,
                (float) zMin / 16F,
                (float) zMax / 16F,
                ALL_FACES, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight
        );
    }

    /**
     * Render a cube at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param xMin               The minimum X coordinate as 16ths of a block.
     * @param xMax               The maximum X coordinate as 16ths of a block.
     * @param yMin               The minimum Y coordinate as 16ths of a block.
     * @param yMax               The maximum Y coordinate as 16ths of a block.
     * @param zMin               The minimum Z coordinate as 16ths of a block.
     * @param zMax               The maximum Z coordinate as 16ths of a block.
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderCube(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, int faceMask, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        renderCube(
                (float) xMin / 16F,
                (float) xMax / 16F,
                (float) yMin / 16F,
                (float) yMax / 16F,
                (float) zMin / 16F,
                (float) zMax / 16F,
                faceMask, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight
        );
    }

    /**
     * Render a cube at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param xSize              The X size as 16ths of a block.
     * @param ySize              The X size as 16ths of a block.
     * @param zSize              The X size as 16ths of a block.
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderCube(int xSize, int ySize, int zSize, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        renderCube(
                0F, (float) xSize / 16F,
                0F, (float) ySize / 16F,
                0F, (float) zSize / 16F,
                ALL_FACES, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight
        );
    }

    /**
     * Render a cube at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param xSize              The X size as 16ths of a block.
     * @param ySize              The X size as 16ths of a block.
     * @param zSize              The X size as 16ths of a block.
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderCube(int xSize, int ySize, int zSize, int faceMask, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        renderCube(
                0F, (float) xSize / 16F,
                0F, (float) ySize / 16F,
                0F, (float) zSize / 16F,
                faceMask, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight
        );
    }

    /**
     * Render a cube at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param xMin               The minimum X coordinate.
     * @param xMax               The maximum X coordinate.
     * @param yMin               The minimum Y coordinate.
     * @param yMax               The maximum Y coordinate.
     * @param zMin               The minimum Z coordinate.
     * @param zMax               The maximum Z coordinate.
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderCube(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, int faceMask, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        /*
        The corners:
          G----H
         /|   /|
        C----D |
        | E--|-F   ^   ^
        |/   |/    |  /
        A----B    (y)(z)(x)-->
         */

        if ( faceMask == 0 )
            return;

        Vector4f cornerA = new Vector4f(xMin, yMin, zMin, 0);
        Vector4f cornerB = new Vector4f(xMax, yMin, zMin, 0);
        Vector4f cornerC = new Vector4f(xMin, yMax, zMin, 0);
        Vector4f cornerD = new Vector4f(xMax, yMax, zMin, 0);

        Vector4f cornerE = new Vector4f(xMin, yMin, zMax, 0);
        Vector4f cornerF = new Vector4f(xMax, yMin, zMax, 0);
        Vector4f cornerG = new Vector4f(xMin, yMax, zMax, 0);
        Vector4f cornerH = new Vector4f(xMax, yMax, zMax, 0);

        if ( (faceMask & FACE_SOUTH) != 0 )
            renderCustomQuad(new Vector4f[]{cornerA, cornerB, cornerD, cornerC}, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);

        if ( (faceMask & FACE_EAST) != 0 )
            renderCustomQuad(new Vector4f[]{cornerB, cornerF, cornerH, cornerD}, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);

        if ( (faceMask & FACE_NORTH) != 0 )
            renderCustomQuad(new Vector4f[]{cornerF, cornerE, cornerG, cornerH}, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);

        if ( (faceMask & FACE_WEST) != 0 )
            renderCustomQuad(new Vector4f[]{cornerE, cornerA, cornerC, cornerG}, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);

        if ( (faceMask & FACE_UP) != 0 )
            renderCustomQuad(new Vector4f[]{cornerC, cornerD, cornerH, cornerG}, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);

        if ( (faceMask & FACE_DOWN) != 0 )
            renderCustomQuad(new Vector4f[]{cornerF, cornerB, cornerA, cornerE}, baseOffset, buffer, transform, transformOffset, color, texture, lightmapSkyLight, lightmapBlockLight);
    }


    /**
     * Renders a simple 2 dimensional quad at a given position to a given buffer with the given transforms, color, texture and lightmap values.
     *
     * @param baseOffset         the base offset. This will be untouched by the model matrix transformations.
     * @param buffer             the buffer to upload the quads to. Vertex format of BLOCK is assumed.
     * @param transform          the model matrix to use as the transform matrix.
     * @param color              the color of the quad. The format is ARGB where each component is represented by a byte.
     * @param texture            the TextureAtlasSprite object to gain the UV data from.
     * @param lightmapSkyLight   the skylight lightmap coordinates for the quad.
     * @param lightmapBlockLight the blocklight lightmap coordinates for the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void renderCustomQuad(final Vector4f[] customQuad, Vector3f baseOffset, BufferBuilder buffer, Matrix4f transform, @Nullable Vector3f transformOffset, long color, @Nullable TextureAtlasSprite texture, int lightmapSkyLight, int lightmapBlockLight) {
        // Getting the RGBA values from the color. (The color is in ARGB format)
        // To put it another way - unpacking an int representation of a color to a 4-component float vector representation.
        float r = ((color & 0xFF0000) >> 16) / 255F;
        float g = ((color & 0xFF00) >> 8) / 255F;
        float b = (color & 0xFF) / 255F;
        float a = ((color & 0xFF000000) >> 24) / 255F;

        float offset = 0;
        if ( texture == null ) {
            final float h = Vector4f.sub(customQuad[0], customQuad[3], null).length();
            final float w = Vector4f.sub(customQuad[0], customQuad[1], null).length();
            final float divisor = Math.max(h, w);
            offset = (1 - divisor) / 2F;
        }

        // A quad consists of 4 vertices so the loop is executed 4 times.
        for (int i = 0; i < 4; ++i) {
            // Getting the vertex position from a set of predefined positions for a basic quad.
            Vector4f quadPos = customQuad[i];

            // We need to make a copy of the vertex because it can be used for rendering other faces.
            if ( transformOffset != null )
                quadPos = new Vector4f(quadPos.x - transformOffset.x, quadPos.y - transformOffset.y, quadPos.z - transformOffset.z, quadPos.w);
            else
                quadPos = new Vector4f(quadPos);

            // Transforming the position vector by the transform matrix.
            Matrix4f.transform(transform, quadPos, quadPos);

            if ( transformOffset != null )
                quadPos.set(quadPos.x + transformOffset.x, quadPos.y + transformOffset.y, quadPos.z + transformOffset.z);

            // Getting the texture UV coordinates from an index. The quad looks like this
            // 0 3
            // 1 2
            float u, v;

            if ( texture != null ) {
                u = i < 2 ? texture.getMaxU() - UV_CORRECT : texture.getMinU() + UV_CORRECT;
                v = i == 1 || i == 2 ? texture.getMaxV() - UV_CORRECT : texture.getMinV() + UV_CORRECT;
            } else {
                u = i < 2 ? 1 - offset - UV_CORRECT : 0 + offset + UV_CORRECT;
                v = i == 1 || i == 2 ? 0 - offset - UV_CORRECT : 0 + offset + UV_CORRECT;
            }

            // Uploading the quad data to the buffer.
            buffer
                    .pos(quadPos.x + baseOffset.x, quadPos.y + baseOffset.y, quadPos.z + baseOffset.z)
                    .color(r, g, b, a)
                    .tex(u, v)
                    .lightmap(lightmapSkyLight, lightmapBlockLight)
                    .endVertex();
        }
    }


    /**
     * Maps a value from one range to another range. Taken from https://stackoverflow.com/a/5732117
     *
     * @param input       the input
     * @param inputStart  the start of the input's range
     * @param inputEnd    the end of the input's range
     * @param outputStart the start of the output's range
     * @param outputEnd   the end of the output's range
     * @return the newly mapped value
     */
    public static double map(final double input, final double inputStart, final double inputEnd, final double outputStart, final double outputEnd) {
        final double input_range = inputEnd - inputStart;
        final double output_range = outputEnd - outputStart;

        return (((input - inputStart) * output_range) / input_range) + outputStart;
    }
}

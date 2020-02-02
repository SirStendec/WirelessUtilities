package com.lordmau5.wirelessutils.render;

import com.lordmau5.wirelessutils.tile.TileSlimeCannon;
import com.lordmau5.wirelessutils.utils.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;

// This isn't a FastTESR because we want to be able to bind the destroy texture
// and use that when the block is being destroyed.

@SideOnly(Side.CLIENT)
public class TESRSlimeCannon extends TileEntitySpecialRenderer<TileSlimeCannon> {

    private static final Vector3f ORIGIN = new Vector3f(0.5F, 0.5F, 0.5F);
    private static final Vector3f AXIS_X = new Vector3f(1F, 0F, 0F);
    private static final Vector3f AXIS_Y = new Vector3f(0F, 1F, 0F);
    private static final Vector3f AXIS_Z = new Vector3f(0F, 0F, 1F);

    private static final String TEXTURE = new ResourceLocation("minecraft", "blocks/slime").toString();

    @Override
    public void render(TileSlimeCannon te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if ( destroyStage >= 0 ) {
            bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(5888);

        } else
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        RenderHelper.disableStandardItemLighting();

        if ( destroyStage >= 0 )
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA_SATURATE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        else
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if ( Minecraft.isAmbientOcclusionEnabled() )
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        else
            GlStateManager.shadeModel(GL11.GL_FLAT);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, alpha, buffer);
        buffer.setTranslation(0, 0, 0);

        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        if ( destroyStage >= 0 ) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    @Override
    public void renderTileEntityFast(@Nonnull TileSlimeCannon tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        final TextureAtlasSprite slime = destroyStage >= 0 ? null : Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE);

        // Where are we aiming?
        final float rotation = (float) Math.toRadians(-tile.getYaw());
        final float angle = (float) Math.toRadians(-tile.getPitch());

        // Make the barrel float up and down all the time.
        final BlockPos pos = tile.getPos();
        final int chunkPos = ((pos.getX() % 32) << 10) + ((pos.getZ() % 32) << 5) + (pos.getY() % 32);
        final float offsetY = MathHelper.sin(((getWorld().getTotalWorldTime() + chunkPos + partialTicks) % 2130) / 10F) * 0.1F + 0.1625F;

        // The barrel also spins when we're running, handled by updateAnimation on the tile.
        float spin = tile.rotation;
        if ( tile.isActive || tile.rotationSpeed > TileSlimeCannon.MIN_BARREL_SPEED || spin != 0 )
            spin += (tile.rotationSpeed / 20F) * partialTicks;

        // Lighting!
        final World world = tile.getWorld();
        final IBlockState state = world.getBlockState(pos);
        final int packed = state.getPackedLightmapCoords(world, pos);
        final int skyLight = RenderUtils.getLightmapSkyLightCoordsFromPackedLightmapCoords(packed);
        final int blockLight = RenderUtils.getLightmapBlockLightCoordsFromPackedLightmapCoords(packed);

        // Do a bunch of math stuff.
        final Matrix4f rotX = new Matrix4f().rotate(angle, AXIS_X);
        final Matrix4f rotY = new Matrix4f().rotate(rotation, AXIS_Y);
        final Matrix4f rotZ = new Matrix4f().rotate((float) Math.toRadians(spin), AXIS_Z);

        final Vector3f offset = new Vector3f((float) x, (float) y + offsetY, (float) z);

        Matrix4f transform = new Matrix4f();
        Matrix4f.mul(transform, rotY, transform);
        Matrix4f.mul(transform, rotX, transform);
        Matrix4f.mul(transform, rotZ, transform);

        final long color = destroyStage >= 0 ? 0x80FFFFFF : 0xFFFFFFFF;

        RenderUtils.renderCube(
                5, 11,
                5, 11,
                3, 7,
                offset,
                buffer,
                transform,
                ORIGIN,
                color,
                slime,
                skyLight,
                blockLight
        );

        RenderUtils.renderCube(
                6, 10,
                6, 10,
                7, 13,
                RenderUtils.ALL_FACES ^ RenderUtils.FACE_SOUTH,
                offset,
                buffer,
                transform,
                ORIGIN,
                color,
                slime,
                skyLight,
                blockLight
        );
    }
}

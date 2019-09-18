package com.lordmau5.wirelessutils.render.particles;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ParticleLine extends Particle {

    public double deltaX;
    public double deltaY;
    public double deltaZ;

    protected ParticleLine(World world, double x, double y, double z, double x2, double y2, double z2, float r, float g, float b) {
        super(world, x, y, z);

        particleRed = r;
        particleGreen = g;
        particleBlue = b;
        particleAlpha = 1;

        particleGravity = 0;

        motionX = 0;
        motionY = 0;
        motionZ = 0;

        this.deltaX = x2;
        this.deltaY = y2;
        this.deltaZ = z2;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Tessellator tessellator = Tessellator.getInstance();

        float x1 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float y1 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float z1 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

        float x2 = (float) (this.prevPosX + (this.deltaX - this.prevPosX) * (double) partialTicks - interpPosX);
        float y2 = (float) (this.prevPosY + (this.deltaY - this.prevPosY) * (double) partialTicks - interpPosY);
        float z2 = (float) (this.prevPosZ + (this.deltaZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

        // TODO: Figure out why this isn't rendering the correct color.

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, particleAlpha);
        GlStateManager.glLineWidth(10F);
        GlStateManager.disableLighting();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        buffer.pos(x1, y1, z1).endVertex();
        buffer.pos(x2, y2, z2).endVertex();

        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        /*int remaining = particleMaxAge - particleAge;
        particleAlpha = (float) remaining / particleMaxAge;*/

        if ( particleAge++ >= particleMaxAge )
            setExpired();
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {
        @Nullable
        @Override
        public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... arguments) {
            int color = 0xFFFFFF;
            float r, g, b;

            if ( arguments.length == 1 )
                color = arguments[0];

            if ( arguments.length == 3 ) {
                r = (arguments[0] & 255) / 255F;
                g = (arguments[1] & 255) / 255F;
                b = (arguments[2] & 255) / 255F;
            } else {
                r = (color >> 16 & 255) / 255F;
                g = (color >> 8 & 255) / 255F;
                b = (color & 255) / 255F;
            }

            return new ParticleLine(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, r, g, b);
        }
    }
}

package com.lordmau5.wirelessutils.render.particles;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

@SideOnly(Side.CLIENT)
public class ParticleSpawner {

    private static Minecraft mc = Minecraft.getMinecraft();
    protected static final Map<Integer, IParticleFactory> particleTypes = new Int2ObjectArrayMap<>();

    static {
        particleTypes.put(WUParticleTypes.LINE.ordinal(), new ParticleLine.Factory());
    }

    public static Particle spawnParticle(WUParticleTypes type, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        return spawnParticle(type.ordinal(), ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    public static Particle spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        IParticleFactory factory = particleTypes.get(particleID);
        if ( factory == null || mc == null || mc.effectRenderer == null )
            return null;

        if ( !ignoreRange ) {
            Entity entity = mc.getRenderViewEntity();
            if ( entity == null )
                return null;

            double deltaX = entity.posX - xCoord;
            double deltaY = entity.posY - yCoord;
            double deltaZ = entity.posZ - zCoord;

            if ( deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1024D )
                return null;
        }

        Particle particle = factory.createParticle(particleID, mc.world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
        if ( particle == null )
            return null;

        mc.effectRenderer.addEffect(particle);
        return particle;
    }

    public static int calculateParticleLevel(boolean minimize) {
        int value = mc.gameSettings.particleSetting;
        if ( minimize && value == 2 && mc.world.rand.nextInt(10) == 0 )
            return 1;
        if ( value == 1 && mc.world.rand.nextInt(3) == 0 )
            return 2;
        return value;
    }

}

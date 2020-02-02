package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderAsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class EntityTracer extends EntityBaseThrowable {

    private final int MAX_STEPS = 120;

    private int color = 0;
    private ArrayList<Vec3d> points;
    private boolean traced = false;
    private boolean done = false;

    public EntityTracer(World world) {
        super(world);
    }

    public EntityTracer(World world, int color) {
        super(world);
        this.color = color;
    }

    public EntityTracer(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityTracer(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void onEntityUpdate() {
        // Intentionally do nothing~!
    }

    @Override
    public void onUpdate() {
        if ( !world.isRemote || ticksExisted > 20 || isDead ) {
            setDead();
            return;
        }

        if ( !traced ) {
            done = false;
            points = new ArrayList<>();

            int steps = 0;
            while ( !done && steps < MAX_STEPS ) {
                super.onUpdate();
                points.add(new Vec3d(posX, posY, posZ));
                steps++;
            }

            isDead = false;
            traced = true;
        }

        if ( points != null && ticksExisted % 4 == 0 ) {
            float colorR = (color >> 16 & 255) / 255.0F;
            float colorG = (color >> 8 & 255) / 255.0F;
            float colorB = (color & 255) / 255.0F;

            if ( colorR == 0 )
                colorR = 0.000001F;

            int i = 1;
            for (Vec3d point : points) {
                if ( i == MAX_STEPS )
                    world.spawnParticle(EnumParticleTypes.REDSTONE, point.x, point.y, point.z, 1F, 1F, 1F);
                else
                    world.spawnParticle(EnumParticleTypes.REDSTONE, point.x, point.y, point.z, colorR, colorG, colorB);

                i++;
            }
        }
    }

    @Override
    protected void onImpact(@Nonnull RayTraceResult result) {
        done = true;
        isDead = true;
    }

    public static class Factory implements IRenderFactory<EntityTracer> {
        @Override
        public Render<? super EntityTracer> createRenderFor(RenderManager manager) {
            return new RenderAsItem<EntityTracer>(manager, Items.AIR, Minecraft.getMinecraft().getRenderItem());
        }
    }
}

package com.lordmau5.wirelessutils.utils.location;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class RayTracing {

    public static RayTraceResult findEntityOnPath(@Nonnull World world, @Nonnull Vec3d start, @Nonnull Vec3d end, Predicate<? super Entity> predicate) {
        RayTraceResult out = null;
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z), predicate);
        double closest = 0D;

        for (Entity target : list) {
            AxisAlignedBB bounds = target.getEntityBoundingBox().grow(0.30000001192092896D);
            RayTraceResult result = bounds.calculateIntercept(start, end);
            if ( result != null ) {
                double distance = start.squareDistanceTo(result.hitVec);
                if ( distance < closest || closest == 0D ) {
                    out = new RayTraceResult(target, result.hitVec);
                    closest = distance;
                }
            }
        }

        return out;
    }

}

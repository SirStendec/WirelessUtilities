package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.pearl.EntityQuenchedPearl;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemQuenchedPearl extends ItemBasePearl {

    public ItemQuenchedPearl() {
        super();
        setName("quenched_pearl");
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entity) {
        World world = entity.getEntityWorld();
        if ( world != null && world.isRemote && world.rand.nextFloat() < 0.05F )
            world.spawnParticle(EnumParticleTypes.WATER_DROP, entity.posX, entity.posY + 0.5D, entity.posZ, 0, 0, 0);

        return false;
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityQuenchedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityQuenchedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityQuenchedPearl(worldIn, stack);
    }
}

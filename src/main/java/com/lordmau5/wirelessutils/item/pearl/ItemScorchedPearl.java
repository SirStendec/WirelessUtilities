package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.pearl.EntityScorchedPearl;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemScorchedPearl extends ItemBasePearl {

    public ItemScorchedPearl() {
        super();
        setName("scorched_pearl");
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if ( !entityItem.isInWater() )
            entityItem.setFire(1);
        return false;
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityScorchedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityScorchedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityScorchedPearl(worldIn, stack);
    }
}

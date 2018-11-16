package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.pearl.EntityQuenchedPearl;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemQuenchedPearl extends ItemBasePearl {

    public ItemQuenchedPearl() {
        super();
        setName("quenched_pearl");
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, EntityPlayer playerIn, IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityQuenchedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityQuenchedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityQuenchedPearl(worldIn, stack);
    }
}

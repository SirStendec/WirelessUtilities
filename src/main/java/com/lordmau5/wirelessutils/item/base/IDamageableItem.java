package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDamageableItem {
    
    default boolean shouldItemTakeDamage(@Nonnull EntityItemEnhanced entity, @Nonnull ItemStack stack, @Nullable DamageSource source, float amount) {
        return true;
    }
}

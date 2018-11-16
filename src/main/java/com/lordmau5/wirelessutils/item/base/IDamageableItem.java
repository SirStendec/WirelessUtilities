package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public interface IDamageableItem {

    default boolean shouldItemTakeDamage(EntityItemEnhanced entity, ItemStack stack, DamageSource source, float amount) {
        return true;
    }
}

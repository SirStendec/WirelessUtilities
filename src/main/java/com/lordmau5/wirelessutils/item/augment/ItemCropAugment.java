package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemCropAugment extends ItemAugment {
    public ItemCropAugment() {
        super();
        setName("crop_augment");
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if ( enchantment == Enchantments.SILK_TOUCH ) {
            return true;
        }

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        boolean silky = false;
        if ( !stack.isEmpty() )
            silky = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;

        if ( augmentable instanceof ICropAugmentable )
            ((ICropAugmentable) augmentable).setCropAugmented(!stack.isEmpty(), silky);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ICropAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ICropAugmentable;
    }
}

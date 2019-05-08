package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.ILevellingBlock;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBlockAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCropAugment extends ItemAugment {
    public ItemCropAugment() {
        super();
        setName("crop_augment");
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        Level level = Level.getLevel(ModConfig.augments.crop.requiredLevel);
        if ( !level.equals(Level.getMinLevel()) ) {
            tooltip.add(new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".augment.min_level",
                    level.getTextComponent()
            ).getFormattedText());
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if ( ModConfig.augments.crop.allowSilkTouch && enchantment == Enchantments.SILK_TOUCH )
            return true;

        if ( ModConfig.augments.crop.allowFortune && enchantment == Enchantments.FORTUNE )
            return true;

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        boolean silky = false;
        int fortune = 0;
        if ( !stack.isEmpty() ) {
            silky = ModConfig.augments.crop.allowSilkTouch && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
            fortune = ModConfig.augments.crop.allowFortune ? EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) : 0;
        }

        if ( augmentable instanceof ICropAugmentable )
            ((ICropAugmentable) augmentable).setCropAugmented(!stack.isEmpty(), silky, fortune);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ICropAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ILevellingBlock )
            if ( ((ILevellingBlock) augmentable).getLevel().toInt() < ModConfig.augments.crop.requiredLevel )
                return false;

        if ( augmentable instanceof IBlockAugmentable && ((IBlockAugmentable) augmentable).isBlockAugmented() )
            return false;

        if ( augmentable instanceof IWorldAugmentable && ((IWorldAugmentable) augmentable).isWorldAugmented() )
            return false;

        return augmentable instanceof ICropAugmentable;
    }
}

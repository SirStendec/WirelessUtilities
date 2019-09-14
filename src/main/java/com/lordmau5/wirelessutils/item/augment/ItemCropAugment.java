package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiAdminAugment;
import com.lordmau5.wirelessutils.gui.client.pages.augments.PageAugmentBlock;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBlockAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICropAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IDispenserAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCropAugment extends ItemAugment {
    public ItemCropAugment() {
        super();
        setName("crop_augment");
    }

    @Override
    public void addExplanation(@Nonnull List<String> tooltip, @Nonnull String name, Object... args) {
        super.addExplanation(tooltip, name, args);
        if ( ModConfig.augments.crop.processTrees && StringHelper.isShiftKeyDown() )
            addLocalizedLines(tooltip, "item." + WirelessUtils.MODID + ".crop_augment.tree");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.crop.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.crop.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.crop.energyDrain;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.getLevel(ModConfig.augments.crop.requiredLevel);
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.crop.budgetAddition;
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.crop.budgetMultiplier;
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
        if ( stack.isEmpty() || stack.getItem() != this )
            return false;

        NBTTagCompound tag = stack.getTagCompound();

        if ( (tag == null || !tag.hasKey("SilkTouch")) && ModConfig.augments.crop.allowSilkTouch && enchantment == Enchantments.SILK_TOUCH )
            return true;

        if ( (tag == null || !tag.hasKey("Fortune")) && ModConfig.augments.crop.allowFortune && enchantment == Enchantments.FORTUNE )
            return true;

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if ( stack.isEmpty() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return;

        if ( tag.hasKey("Fortune", Constants.NBT.TAG_BYTE) ) {
            byte fortune = tag.getByte("Fortune");
            if ( fortune > 0 )
                tooltip.add(Enchantments.FORTUNE.getTranslatedName(fortune));
        }

        if ( tag.getBoolean("SilkTouch") )
            tooltip.add(Enchantments.SILK_TOUCH.getTranslatedName(1));
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        boolean silky = false;
        int fortune = 0;
        if ( !stack.isEmpty() ) {
            NBTTagCompound tag = stack.getTagCompound();

            silky = (tag != null && tag.hasKey("SilkTouch")) ? tag.getBoolean("SilkTouch") : ModConfig.augments.crop.allowSilkTouch && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
            fortune = (tag != null && tag.hasKey("Fortune")) ? tag.getByte("Fortune") : ModConfig.augments.crop.allowFortune ? EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) : 0;
        }

        if ( augmentable instanceof ICropAugmentable )
            ((ICropAugmentable) augmentable).setCropAugmented(!stack.isEmpty(), silky, fortune);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ICropAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IBlockAugmentable && ((IBlockAugmentable) augmentable).isBlockAugmented() )
            return false;

        if ( augmentable instanceof IWorldAugmentable && ((IWorldAugmentable) augmentable).isWorldAugmented() )
            return false;

        if ( augmentable instanceof IDispenserAugmentable && ((IDispenserAugmentable) augmentable).isDispenserAugmented() )
            return false;

        return augmentable instanceof ICropAugmentable;
    }

    @Nullable
    @Override
    public PageBase getAdminGuiPage(GuiAdminAugment gui) {
        return new PageAugmentBlock(gui);
    }
}

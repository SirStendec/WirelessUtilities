package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
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

public class ItemBlockAugment extends ItemAugment {
    public ItemBlockAugment() {
        super();
        setName("block_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.block.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.block.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.block.energyDrain;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.augments.block.requiredLevel);
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.block.budgetAddition;
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.block.budgetMultiplier;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getItemEnchantability() {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( (tag == null || !tag.hasKey("SilkTouch")) && ModConfig.augments.block.allowSilkTouch && enchantment == Enchantments.SILK_TOUCH )
            return true;

        if ( (tag == null || !tag.hasKey("Fortune")) && ModConfig.augments.block.allowFortune && enchantment == Enchantments.FORTUNE )
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

            silky = (tag != null && tag.hasKey("SilkTouch")) ? tag.getBoolean("SilkTouch") : ModConfig.augments.block.allowSilkTouch && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
            fortune = (tag != null && tag.hasKey("Fortune")) ? tag.getByte("Fortune") : ModConfig.augments.block.allowFortune ? EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) : 0;
        }

        if ( augmentable instanceof IBlockAugmentable )
            ((IBlockAugmentable) augmentable).setBlockAugmented(!stack.isEmpty(), silky, fortune);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IBlockAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ICropAugmentable && ((ICropAugmentable) augmentable).isCropAugmented() )
            return false;

        if ( augmentable instanceof IWorldAugmentable && ((IWorldAugmentable) augmentable).isWorldAugmented() )
            return false;

        if ( augmentable instanceof IDispenserAugmentable && ((IDispenserAugmentable) augmentable).isDispenserAugmented() )
            return false;

        return augmentable instanceof IBlockAugmentable;
    }

    @Nullable
    @Override
    public PageBase getAdminGuiPage(GuiAdminAugment gui) {
        return new PageAugmentBlock(gui);
    }
}

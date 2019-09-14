package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemRangeAugment extends ItemAugment {
    public static final int INTERDIMENSIONAL_VALUE = OreDictionary.WILDCARD_VALUE - 1;

    public ItemRangeAugment() {
        super();
        setName("range_augment");
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] drain = ModConfig.augments.range.energyDrain;
        if ( drain.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= drain.length )
            idx = drain.length - 1;

        return drain[idx];
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        if ( isInterdimensional(stack) )
            return Level.fromInt(ModConfig.augments.range.interdimensionalLevel);

        return super.getRequiredLevelDelegate(stack);
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] additions = ModConfig.augments.range.budgetAddition;
        if ( additions.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= additions.length )
            idx = additions.length - 1;

        return additions[idx];
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        double[] mults = ModConfig.augments.range.budgetMultiplier;
        if ( mults.length == 0 )
            return 1;

        int idx = getLevel(stack).toInt();
        if ( idx >= mults.length )
            idx = mults.length - 1;

        return mults[idx];
    }

    @Override
    public int getTiers() {
        return Math.min(ModConfig.augments.range.availableTiers, Level.values().length);
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IRangeAugmentable )
            ((IRangeAugmentable) augmentable).setRange(stack);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        if ( IDirectionalMachine.class.isAssignableFrom(klass) ) {
            if ( isInterdimensional(stack) || stack.getMetadata() >= ModConfig.augments.range.maxTierDirectional )
                return false;
        }

        return IRangeAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( (augmentable instanceof IDirectionalMachine) && (isInterdimensional(stack) || stack.getMetadata() >= ModConfig.augments.range.maxTierDirectional) )
            return false;

        return augmentable instanceof IRangeAugmentable;
    }

    public boolean isInterdimensional(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return false;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Interdimensional") )
                return tag.getBoolean("Interdimensional");
        }

        return stack.getMetadata() == INTERDIMENSIONAL_VALUE;
    }

    @Override
    public boolean shouldRequireLowerTier(@Nonnull ItemStack stack) {
        return !isInterdimensional(stack);
    }

    public int getDirectionalRange(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("DirectionalRange") )
                return tag.getInteger("DirectionalRange");
        }

        int index = stack.getMetadata();
        if ( index >= ModConfig.augments.range.maxTierDirectional )
            return 0;

        int[] range = ModConfig.augments.range.directionalBlocks;
        if ( range == null || range.length == 0 )
            return 0;

        if ( index >= range.length || index < 0 )
            index = range.length - 1;

        return 3 * range[index];
    }

    public int getPositionalRange(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ModConfig.augments.range.blocksPerTier;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("PositionalRange") )
                return tag.getInteger("PositionalRange");
        }

        return (stack.getMetadata() + 2) * ModConfig.augments.range.blocksPerTier;
    }

    public void addSlotLockExplanation(@Nonnull List<String> tooltip, @Nonnull TileEntity entity, @Nonnull Slot slot, @Nonnull ItemStack stack) {
        super.addSlotLockExplanation(tooltip, entity, slot, stack);
        addLocalizedLines(tooltip, getTranslationKey() + ".lock");
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        super.getSubItems(tab, items);

        if ( ModConfig.augments.range.enableInterdimensional )
            items.add(new ItemStack(this, 1, INTERDIMENSIONAL_VALUE));
    }

    @Nullable
    @Override
    public String getTierNameDelegate(@Nonnull ItemStack stack) {
        if ( isInterdimensional(stack) )
            return StringHelper.localize(getTranslationKey(stack) + ".interdimensional");

        return null;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        final String name = stack.getTranslationKey();
        boolean isInterdimensional = isInterdimensional(stack);
        int positionalRange = getPositionalRange(stack);
        int directionalRange = getDirectionalRange(stack);

        if ( isInterdimensional && !ModConfig.augments.range.enableInterdimensional ) {
            tooltip.add(
                    new TextComponentTranslation("item." + WirelessUtils.MODID + ".disabled")
                            .setStyle(new Style().setColor(TextFormatting.RED)).getFormattedText());

        } else if ( !isInterdimensional && directionalRange > 0 ) {
            directionalRange = 1 + (2 * Math.floorDiv(directionalRange, 3));

            tooltip.add(new TextComponentTranslation(
                    name + ".tip.directional",
                    directionalRange, directionalRange, directionalRange
            ).getFormattedText());
        }

        tooltip.add(new TextComponentTranslation(
                name + ".tip.positional",
                isInterdimensional ?
                        new TextComponentTranslation(name + ".tip.interdimensional").getFormattedText() :
                        new TextComponentTranslation(name + ".tip.range_positional", positionalRange)
        ).getFormattedText());
    }
}

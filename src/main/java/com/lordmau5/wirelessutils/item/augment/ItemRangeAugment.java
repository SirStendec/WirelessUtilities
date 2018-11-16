package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
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
    public int getTiers() {
        return Math.min(ModConfig.augments.range.availableTiers, Level.values().length);
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IRangeAugmentable )
            ((IRangeAugmentable) augmentable).setRange(stack);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        if ( IDirectionalMachine.class.isAssignableFrom(klass) ) {
            if ( isInterdimensional(stack) || stack.getMetadata() >= ModConfig.augments.range.maxTierDirectional )
                return false;
        }

        return IRangeAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( (augmentable instanceof IDirectionalMachine) && (isInterdimensional(stack) || stack.getMetadata() >= ModConfig.augments.range.maxTierDirectional) )
            return false;

        return augmentable instanceof IRangeAugmentable;
    }

    public boolean isInterdimensional(ItemStack stack) {
        return stack.getItem() == this && stack.getMetadata() == INTERDIMENSIONAL_VALUE;
    }

    @Override
    public boolean shouldRequireLowerTier(ItemStack stack) {
        return !isInterdimensional(stack);
    }

    public int getDirectionalRange(ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        return 3 * (stack.getMetadata() + 1);
    }

    public int getPositionalRange(ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ModConfig.augments.range.blocksPerTier;

        return (stack.getMetadata() + 2) * ModConfig.augments.range.blocksPerTier;
    }

    public void addSlotLockExplanation(List<String> tooltip, TileEntity entity, Slot slot, ItemStack stack) {
        super.addSlotLockExplanation(tooltip, entity, slot, stack);
        addLocalizedLines(tooltip, getTranslationKey() + ".lock");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        super.getSubItems(tab, items);

        if ( ModConfig.augments.range.enableInterdimensional )
            items.add(new ItemStack(this, 1, INTERDIMENSIONAL_VALUE));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = getTranslationKey(stack);

        return new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.name",
                new TextComponentTranslation(name + ".name"),
                isInterdimensional(stack) ?
                        new TextComponentTranslation(name + ".interdimensional") :
                        Level.fromAugment(stack).getName()
        ).getUnformattedText();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = stack.getTranslationKey();
        int metadata = stack.getMetadata();
        boolean isInterdimensional = isInterdimensional(stack);
        int positionalRange = getPositionalRange(stack);
        int directionalArea = 1 + (2 * (1 + metadata));

        if ( isInterdimensional && !ModConfig.augments.range.enableInterdimensional ) {
            tooltip.add(
                    new TextComponentTranslation("item." + WirelessUtils.MODID + ".disabled")
                            .setStyle(new Style().setColor(TextFormatting.RED)).getFormattedText());

        } else if ( !isInterdimensional && metadata < ModConfig.augments.range.maxTierDirectional ) {
            tooltip.add(new TextComponentTranslation(
                    name + ".tip.directional",
                    directionalArea, directionalArea, directionalArea
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

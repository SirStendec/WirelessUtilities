package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISlotAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSlotAugment extends ItemAugment {
    public ItemSlotAugment() {
        super();
        setName("slot_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        double[] multipliers = ModConfig.augments.slot.energyMultiplier;
        if ( multipliers.length == 0 )
            return 1;

        int idx = getLevel(stack).toInt();
        if ( idx >= multipliers.length )
            idx = multipliers.length - 1;

        return multipliers[idx];
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] additions = ModConfig.augments.slot.energyAddition;
        if ( additions.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= additions.length )
            idx = additions.length - 1;

        return additions[idx];
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] drain = ModConfig.augments.slot.energyDrain;
        if ( drain.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= drain.length )
            idx = drain.length - 1;

        return drain[idx];
    }

    @Override
    public int getTiers() {
        return Math.min(ModConfig.augments.slot.availableTiers, Level.values().length);
    }

    public int getAvailableSlots(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return getAvailableSlots(0, 1, ModConfig.augments.slot.slotsPerTier);

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Slots") )
                return tag.getByte("Slots");
        }

        return getAvailableSlots(stack.getMetadata() + 1, 1, ModConfig.augments.slot.slotsPerTier);
    }

    public int getAvailableSlots(int tier, int baseSlots, int slotsPerTier) {
        if ( tier == 0 )
            return baseSlots;

        return slotsPerTier * tier;
    }

    @Override
    public void addSlotLockExplanation(@Nonnull List<String> tooltip, @Nonnull TileEntity tile, @Nonnull Slot slot, @Nonnull ItemStack stack) {
        super.addSlotLockExplanation(tooltip, tile, slot, stack);
        addLocalizedLines(tooltip, getTranslationKey() + ".lock");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ISlotAugmentable ) {
            ISlotAugmentable slots = (ISlotAugmentable) augmentable;
            int tier = (stack.isEmpty() || stack.getItem() != this) ? 0 : stack.getMetadata() + 1;
            slots.setUnlockedSlots(getAvailableSlots(stack), tier);
        }
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ISlotAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ISlotAugmentable;
    }
}
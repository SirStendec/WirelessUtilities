package com.lordmau5.wirelessutils.item;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockMachine extends ItemBlockExplainable {

    private final BlockBaseMachine block;

    public ItemBlockMachine(BlockBaseMachine block) {
        super(block);
        this.block = block;
    }

    @Override
    @Nonnull
    public EnumRarity getRarity(ItemStack stack) {
        return Level.fromItemStack(stack).rarity;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if ( StringHelper.isControlKeyDown() ) {
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".machine_ctrl.active"));
            boolean had_augments = false;

            Class<? extends TileEntity> klass = block.getTileEntityClass();
            if ( klass != null && IAugmentable.class.isAssignableFrom(klass) ) {
                Class<? extends IAugmentable> augmentable = klass.asSubclass(IAugmentable.class);

                for (ItemAugment item : ItemAugment.AUGMENT_TYPES) {
                    if ( item.canApplyTo(new ItemStack(item), augmentable) ) {
                        had_augments = true;
                        tooltip.add(new TextComponentTranslation(
                                "item." + WirelessUtils.MODID + ".machine_ctrl.entry",
                                new TextComponentTranslation(item.getTranslationKey() + ".name").setStyle(TextHelpers.WHITE)
                        ).setStyle(TextHelpers.GRAY).getFormattedText());
                    }
                }
            }

            if ( !had_augments )
                tooltip.add(new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".machine_ctrl.entry",
                        new TextComponentTranslation("item." + WirelessUtils.MODID + ".machine_ctrl.none").setStyle(TextHelpers.WHITE)
                ).setStyle(TextHelpers.GRAY).getFormattedText());
        } else
            tooltip.add(StringHelper.localizeFormat("item." + WirelessUtils.MODID + ".machine_ctrl.inactive"));
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        String out = new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.name",
                new TextComponentTranslation(getTranslationKey(stack) + ".name"),
                Level.fromItemStack(stack).getName()
        ).getUnformattedText();

        if ( stack.hasTagCompound() && stack.getTagCompound().hasKey("Configured") )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".configured",
                    out).getUnformattedText();

        return out;
    }
}

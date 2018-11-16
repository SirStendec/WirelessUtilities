package com.lordmau5.wirelessutils.item;

import cofh.core.block.ItemBlockCore;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.item.base.IExplainableItem;
import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockMachine extends ItemBlockCore implements IExplainableItem {

    public ItemBlockMachine(BlockBaseMachine block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addExplanation(tooltip, getTranslationKey(stack) + ".info");
    }

    public Level getLevel(ItemStack stack) {
        return Level.fromInt(stack.getMetadata());
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return getLevel(stack).rarity;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String out = new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.name",
                new TextComponentTranslation(getTranslationKey(stack) + ".name"),
                getLevel(stack).getName()
        ).getUnformattedText();

        if ( stack.hasTagCompound() && stack.getTagCompound().hasKey("Configured") )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".configured",
                    out).getUnformattedText();

        return out;
    }
}

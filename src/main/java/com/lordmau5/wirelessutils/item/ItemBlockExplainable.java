package com.lordmau5.wirelessutils.item;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.item.base.IExplainableItem;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockExplainable extends cofh.core.block.ItemBlockCore implements IExplainableItem {

    public ItemBlockExplainable(Block block) {
        super(block);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StringHelper.localize(getTranslationKey(stack) + ".name");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addExplanation(tooltip, getTranslationKey(stack) + ".info");
    }


}

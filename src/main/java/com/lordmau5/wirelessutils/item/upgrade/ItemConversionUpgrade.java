package com.lordmau5.wirelessutils.item.upgrade;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBaseLevelUpgrade;
import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemConversionUpgrade extends ItemBaseLevelUpgrade {

    public ItemConversionUpgrade() {
        super();

        setName("conversion_upgrade");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) || Level.values().length < 3 )
            return;

        Level[] levels = Level.values();
        for (int i = 2; i < levels.length; i++) {
            Level level = levels[i];
            if ( level == Level.getMinLevel() )
                continue;

            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addExplanation(tooltip, "info." + WirelessUtils.MODID + ".tiered.conversion.info", getLevel(stack).getTextComponent());
    }
}

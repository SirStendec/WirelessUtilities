package com.lordmau5.wirelessutils.item.upgrade;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBaseLevelUpgrade;
import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemLevelUpgrade extends ItemBaseLevelUpgrade {

    public ItemLevelUpgrade() {
        super();

        setName("level_upgrade");
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Level level = getLevel(stack);
        if ( level == Level.getMinLevel() )
            return;

        Level prevLevel = Level.fromInt(level.toInt() - 1);
        addExplanation(tooltip, "info." + WirelessUtils.MODID + ".tiered.upgrade.info", prevLevel.getTextComponent(), level.getTextComponent());
    }
}

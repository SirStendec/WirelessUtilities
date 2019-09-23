package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBaseEntityPositionalCard extends ItemBasePositionalCard {

    public BlockPosDimension getTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin) {
        return null;
    }

    @Nullable
    public BlockArea getHandRenderArea(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nullable EnumHand hand, int color) {
        return null;
    }

    public abstract Entity getEntityTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin);

    @Override
    public void addTooltipContext(@Nonnull List<String> tooltip, @Nonnull TileEntity tile, @Nonnull Slot slot, @Nonnull ItemStack stack) {
        if ( tile instanceof IPositionalMachine && tile.hasWorld() ) {
            if ( !isCardConfigured(stack) )
                tooltip.add(1,
                        new TextComponentTranslation(getTranslationKey() + ".invalid.unset")
                                .setStyle(TextHelpers.GRAY).getFormattedText());
        }
    }
}

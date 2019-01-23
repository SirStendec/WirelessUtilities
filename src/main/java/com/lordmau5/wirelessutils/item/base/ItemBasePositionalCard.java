package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.utils.crafting.INBTPreservingIngredient;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

import static com.lordmau5.wirelessutils.utils.constants.TextHelpers.*;

public abstract class ItemBasePositionalCard extends ItemBase implements ISlotContextTooltip, INBTPreservingIngredient {

    public ItemBasePositionalCard() {
        super();

        setMaxStackSize(16);
    }

    public boolean shouldIgnoreDistance(@Nonnull ItemStack stack) {
        if ( stack.getItem() != this || !stack.hasTagCompound() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        return tag.getBoolean("IgnoreDistance");
    }

    public int getCost(@Nonnull ItemStack stack) {
        if ( stack.getItem() != this || !stack.hasTagCompound() )
            return -1;

        NBTTagCompound tag = stack.getTagCompound();
        return tag.hasKey("Cost") ? tag.getInteger("Cost") : -1;
    }

    public boolean updateCard(@Nonnull ItemStack stack, TileEntity container) {
        return false;
    }

    public abstract boolean isCardConfigured(@Nonnull ItemStack stack);

    public abstract BlockPosDimension getTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin);

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        if ( isCardConfigured(stack) )
            return 1;
        //if ( stack.hasTagCompound() && BlockPosDimension.fromTag(stack.getTagCompound()) != null )
        //    return 1;

        return super.getItemStackLimit(stack);
    }

    @Override
    public boolean isValidForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        return output.getItem() == this && stack.hasTagCompound();
    }

    @Override
    public void addTooltipContext(@Nonnull List<String> tooltip, @Nonnull TileEntity tile, @Nonnull Slot slot, @Nonnull ItemStack stack) {
        if ( tile instanceof IPositionalMachine && tile.hasWorld() ) {
            IPositionalMachine machine = (IPositionalMachine) tile;
            BlockPosDimension target = getTarget(stack, machine.getPosition());
            if ( target == null ) {
                tooltip.add(1,
                        new TextComponentTranslation(getTranslationKey() + ".invalid.unset")
                                .setStyle(GRAY)
                                .getFormattedText());
                return;
            }

            World world = tile.getWorld();
            if ( world != null ) {
                ITextComponent distance;
                int dimension = world.provider.getDimension();

                if ( dimension != target.getDimension() ) {
                    distance = new TextComponentString("999").setStyle(
                            getStyle(TextFormatting.WHITE, true)
                    );

                } else {
                    BlockPos pos = tile.getPos();
                    int blockDistance = (int) Math.floor(target.getDistance(pos.getX(), pos.getY(), pos.getZ()));
                    distance = getComponent(blockDistance);
                }

                tooltip.add(1, new TextComponentTranslation(
                        getTranslationKey() + ".distance",
                        distance
                ).setStyle(GRAY).getFormattedText());

                if ( !machine.isTargetInRange(target) )
                    tooltip.add(1, new TextComponentTranslation(
                            getTranslationKey() + ".invalid.range")
                            .setStyle(RED)
                            .getFormattedText());
            }
        }
    }
}

package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.item.module.ItemTeleportModule;
import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
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

import static com.lordmau5.wirelessutils.utils.constants.TextHelpers.GRAY;
import static com.lordmau5.wirelessutils.utils.constants.TextHelpers.RED;
import static com.lordmau5.wirelessutils.utils.constants.TextHelpers.getComponent;
import static com.lordmau5.wirelessutils.utils.constants.TextHelpers.getStyle;

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
        BlockPosDimension target;
        int distance = 0;
        boolean interdimensional = false;
        boolean inRange = false;

        if ( tile instanceof TileBaseVaporizer && tile.hasWorld() ) {
            TileBaseVaporizer vaporizer = (TileBaseVaporizer) tile;
            if ( vaporizer.getBehavior() instanceof ItemTeleportModule.TeleportBehavior ) {
                ItemTeleportModule.TeleportBehavior behavior = (ItemTeleportModule.TeleportBehavior) vaporizer.getBehavior();
                target = getTarget(stack, vaporizer.getPosition());
                if ( target != null ) {
                    World world = vaporizer.getWorld();
                    if ( world != null ) {
                        int dimension = world.provider.getDimension();

                        if ( dimension != target.getDimension() )
                            interdimensional = true;
                        else {
                            BlockPos pos = vaporizer.getPos();
                            distance = (int) Math.floor(target.getDistance(pos.getX(), pos.getY(), pos.getZ()));
                        }

                        inRange = behavior.isTargetInRange(stack);
                    }
                }
            } else
                return;

        } else if ( tile instanceof IPositionalMachine && tile.hasWorld() ) {
            IPositionalMachine machine = (IPositionalMachine) tile;
            target = getTarget(stack, machine.getPosition());

            if ( target != null ) {
                World world = tile.getWorld();
                if ( world != null ) {
                    int dimension = world.provider.getDimension();

                    if ( dimension != target.getDimension() )
                        interdimensional = true;
                    else {
                        BlockPos pos = tile.getPos();
                        distance = (int) Math.floor(target.getDistance(pos.getX(), pos.getY(), pos.getZ()));
                    }

                    inRange = !shouldIgnoreDistance(stack) && machine.isTargetInRange(target);
                }
            }

        } else
            return;

        if ( target == null ) {
            tooltip.add(1,
                    new TextComponentTranslation(getTranslationKey() + ".invalid.unset")
                            .setStyle(GRAY)
                            .getFormattedText());
            return;
        }

        ITextComponent dist;
        if ( interdimensional )
            dist = new TextComponentString("999").setStyle(getStyle(TextFormatting.WHITE, true));
        else
            dist = getComponent(distance);

        tooltip.add(1, new TextComponentTranslation(
                getTranslationKey() + ".distance",
                dist
        ).setStyle(GRAY).getFormattedText());

        if ( !inRange )
            tooltip.add(1, new TextComponentTranslation(
                    getTranslationKey() + ".invalid.range")
                    .setStyle(RED)
                    .getFormattedText());
    }
}

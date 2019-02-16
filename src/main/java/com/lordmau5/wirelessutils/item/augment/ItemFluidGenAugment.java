package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFluidGenAugmentable;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFluidGenAugment extends ItemAugment {

    public ItemFluidGenAugment() {
        super();
        setName("fluid_gen_augment");
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( !(augmentable instanceof IFluidGenAugmentable) )
            return;

        IFluidGenAugmentable machine = (IFluidGenAugmentable) augmentable;

        if ( stack.isEmpty() || stack.getItem() != this ) {
            machine.setFluidGenAugmented(false, null, 0);
            return;
        }

        machine.setFluidGenAugmented(true, getFluid(stack), getEnergy(stack));
    }

    public int getEnergy(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Energy") )
                return tag.getInteger("Energy");
        }

        return ModConfig.augments.fluidGen.energyCost;
    }

    public FluidStack getFluid(@Nonnull ItemStack stack) {
        FluidStack fluid = null;
        if ( !stack.isEmpty() && stack.getItem() == this ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Fluid") ) {
                fluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("Fluid"));
            }
        }

        if ( fluid == null ) {
            int amount = ModConfig.augments.fluidGen.fluidRate;
            fluid = FluidRegistry.getFluidStack(ModConfig.augments.fluidGen.fluidName, amount);
            if ( fluid == null )
                fluid = new FluidStack(FluidRegistry.WATER, amount);
        }

        return fluid;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getHighlightTip(@Nonnull ItemStack item, @Nonnull String displayName) {
        displayName = super.getHighlightTip(item, displayName);
        FluidStack fluid = getFluid(item);

        if ( fluid != null ) {
            displayName = new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".tiered.name",
                    displayName,
                    getStackComponent(fluid)
            ).getFormattedText();
        }

        return displayName;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        FluidStack fluid = getFluid(stack);
        int energy = getEnergy(stack);

        if ( fluid != null ) {
            String name = stack.getTranslationKey();

            tooltip.add(new TextComponentTranslation(
                    name + ".fluid",
                    new TextComponentTranslation(
                            name + ".fluid_amount",
                            fluid.amount,
                            getStackComponent(fluid)
                    )
            ).getFormattedText());

            if ( energy > 0 )
                tooltip.add(new TextComponentTranslation(
                        name + ".energy",
                        TextHelpers.getScaledNumber(energy, "RF/t", true)
                ).getFormattedText());
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public ITextComponent getStackComponent(FluidStack stack) {
        if ( stack == null )
            return null;

        Fluid fluid = stack.getFluid();
        if ( fluid == null )
            return null;

        EnumRarity rarity = fluid.getRarity(stack);
        return new TextComponentString(stack.getLocalizedName())
                .setStyle(TextHelpers.getStyle(rarity.color));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if ( !worldIn.isRemote ) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag == null )
                tag = new NBTTagCompound();

            if ( tag.hasKey("Fluid") )
                tag.removeTag("Fluid");
            else {
                FluidStack fluid = new FluidStack(FluidRegistry.LAVA, 100);
                NBTTagCompound ftag = new NBTTagCompound();
                fluid.writeToNBT(ftag);
                tag.setTag("Fluid", ftag);
            }

            stack.setTagCompound(tag);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IFluidGenAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IFluidGenAugmentable;
    }
}

package com.lordmau5.wirelessutils.plugins.JEI.condenser;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CondenserRecipeCategory implements IRecipeCategory<CondenserRecipeWrapper> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/jei_machine.png");

    public static final String UID = WirelessUtils.MODID + ".condenser";

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable tankOverlay;
    private final IDrawableStatic energyBackground;

    public CondenserRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(TEXTURE, 0, 42, 104, 42)
                .addPadding(0, 0, 14 + 6, 0).build();

        energyBackground = guiHelper.createDrawable(TEXTURE, 104, 0, 14, 42);
        tankOverlay = guiHelper.createDrawable(TEXTURE, 150, 1, 16, 40);
        icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.blockDirectionalCondenser));
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return StringHelper.localize("jei." + WirelessUtils.MODID + ".condenser");
    }

    @Nonnull
    @Override
    public String getModName() {
        return StringHelper.localize("itemGroup." + WirelessUtils.MODID);
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        energyBackground.draw(minecraft, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CondenserRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 44, 2);
        guiItemStacks.init(1, false, 102, 12);
        guiItemStacks.init(2, true, 44, 22);

        List<List<FluidStack>> manyFluids = ingredients.getInputs(VanillaTypes.FLUID);
        List<FluidStack> fluids = manyFluids == null ? null : manyFluids.get(0);
        FluidStack fluid = fluids != null ? fluids.get(0) : null;
        int capacity = fluid == null ? 1000 : fluid.amount * 4;

        guiFluidStacks.init(0, true, 21, 1, 16, 40, capacity, true, tankOverlay);

        guiItemStacks.set(ingredients);
        guiFluidStacks.set(ingredients);

        guiItemStacks.set(2, new ItemStack(ModItems.itemInventoryAugment));
    }

    public static List<CondenserRecipeWrapper> getRecipes(IGuiHelper guiHelper) {
        List<CondenserRecipeWrapper> out = new ArrayList<>();
        for (CondenserRecipeManager.CondenserRecipe recipe : CondenserRecipeManager.getRecipeList()) {
            if ( recipe != null )
                out.add(new CondenserRecipeWrapper(guiHelper, recipe));
        }

        return out;
    }
}

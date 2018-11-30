package com.lordmau5.wirelessutils.plugins.JEI.condenser;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.Level;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CondenserRecipeWrapper implements IRecipeWrapper {

    private final CondenserRecipeManager.CondenserRecipe recipe;

    private final IDrawableAnimated energy;
    private final IDrawableAnimated progress;

    protected CondenserRecipeWrapper(@Nonnull IGuiHelper guiHelper, @Nonnull CondenserRecipeManager.CondenserRecipe recipe) {
        this.recipe = recipe;
        int duration = recipe.ticks / Level.getMinLevel().craftingTPT;

        energy = guiHelper.drawableBuilder(CondenserRecipeCategory.TEXTURE, 118, 21, 14, 21)
                .buildAnimated(duration, IDrawableAnimated.StartDirection.TOP, true);

        progress = guiHelper.drawableBuilder(CondenserRecipeCategory.TEXTURE, 80, 0, 24, 17)
                .buildAnimated(duration, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.fluid);
        ingredients.setInput(VanillaTypes.ITEM, recipe.input);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltip = new ArrayList<>();
        if ( mouseX > 0 && mouseX < 14 )
            tooltip.add(StringHelper.localize("info.cofh.energy") + ": " + StringHelper.formatNumber(recipe.cost) + " RF");

        return tooltip;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        progress.draw(minecraft, 68, 14);
        energy.draw(minecraft, 0, 21);
    }
}

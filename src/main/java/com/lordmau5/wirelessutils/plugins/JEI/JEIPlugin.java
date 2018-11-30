package com.lordmau5.wirelessutils.plugins.JEI;

import com.lordmau5.wirelessutils.plugins.JEI.charger.ChargerRecipeCategory;
import com.lordmau5.wirelessutils.plugins.JEI.condenser.CondenserRecipeCategory;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        final IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        final IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registry.addRecipeCategories(
                new ChargerRecipeCategory(guiHelper),
                new CondenserRecipeCategory(guiHelper)
        );
    }

    @Override
    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registry.addRecipes(ChargerRecipeCategory.getRecipes(guiHelper), ChargerRecipeCategory.UID);
        registry.addRecipes(CondenserRecipeCategory.getRecipes(guiHelper), CondenserRecipeCategory.UID);

        for (Level level : Level.values()) {
            int meta = level.toInt();
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockDirectionalCharger, 1, meta), ChargerRecipeCategory.UID);
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockPositionalCharger, 1, meta), ChargerRecipeCategory.UID);

            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockDirectionalCondenser, 1, meta), CondenserRecipeCategory.UID);
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockPositionalCondenser, 1, meta), CondenserRecipeCategory.UID);
        }
    }
}

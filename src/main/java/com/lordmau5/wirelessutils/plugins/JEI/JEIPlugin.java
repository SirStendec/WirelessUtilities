package com.lordmau5.wirelessutils.plugins.JEI;

import com.google.common.collect.ImmutableList;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.item.base.IJEIInformationItem;
import com.lordmau5.wirelessutils.plugins.JEI.charger.ChargerRecipeCategory;
import com.lordmau5.wirelessutils.plugins.JEI.condenser.CondenserRecipeCategory;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

    private static IJeiRuntime jeiRuntime = null;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JEIPlugin.jeiRuntime = jeiRuntime;
    }

    public static boolean hasJEI() {
        return jeiRuntime != null;
    }

    public static boolean showRecipeCategory(@Nonnull String category) {
        if ( jeiRuntime == null )
            return false;

        IRecipesGui gui = jeiRuntime.getRecipesGui();
        if ( gui == null )
            return false;

        gui.showCategories(ImmutableList.of(category));
        return true;
    }

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

        registry.addAdvancedGuiHandlers(new BaseGuiHandler());
        registry.addGhostIngredientHandler(BaseGuiContainer.class, new GhostIngredientHandler());

        for (Item item : CommonProxy.ITEMS) {
            if ( item instanceof IJEIInformationItem ) {
                ((IJEIInformationItem) item).registerJEI(registry);
            } else {
                Block block = Block.getBlockFromItem(item);
                if ( block instanceof IJEIInformationItem )
                    ((IJEIInformationItem) block).registerJEI(registry);
            }
        }

        registry.addRecipes(ChargerRecipeCategory.getRecipes(guiHelper), ChargerRecipeCategory.UID);
        registry.addRecipes(CondenserRecipeCategory.getRecipes(guiHelper), CondenserRecipeCategory.UID);

        Level[] levels = Level.values();
        for (int i = 0; i < levels.length; i++) {
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockDirectionalCharger, 1, i), ChargerRecipeCategory.UID);
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockPositionalCharger, 1, i), ChargerRecipeCategory.UID);

            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockDirectionalCondenser, 1, i), CondenserRecipeCategory.UID);
            registry.addRecipeCatalyst(new ItemStack(ModBlocks.blockPositionalCondenser, 1, i), CondenserRecipeCategory.UID);
        }
    }
}

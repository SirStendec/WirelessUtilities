package com.lordmau5.wirelessutils.plugins.JEI.charger;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChargerRecipeCategory implements IRecipeCategory<ChargerRecipeWrapper> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/jei_machine.png");
    public static final String UID = WirelessUtils.MODID + ".charger";

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic energyBackground;

    public ChargerRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 80, 42)
                .addPadding(0, 0, 14 + 6, 0).build();

        energyBackground = guiHelper.createDrawable(TEXTURE, 104, 0, 14, 42);
        icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.blockDirectionalCharger));
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return StringHelper.localize("jei." + WirelessUtils.MODID + ".charger");
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
    public void setRecipe(IRecipeLayout recipeLayout, ChargerRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 20, 2);
        guiItemStacks.init(1, false, 78, 12);
        guiItemStacks.init(2, true, 20, 22);

        guiItemStacks.set(ingredients);
        guiItemStacks.set(2, new ItemStack(ModItems.itemInventoryAugment));
    }

    public static List<ChargerRecipeWrapper> getRecipes(IGuiHelper guiHelper) {
        List<ChargerRecipeWrapper> out = new ArrayList<>();

        for (ChargerRecipeManager.ChargerRecipe recipe : ChargerRecipeManager.getRecipeList()) {
            if ( recipe != null )
                out.add(new ChargerRecipeWrapper(guiHelper, recipe));
        }

        return out;
    }
}

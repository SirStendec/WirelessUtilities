package com.lordmau5.wirelessutils.utils.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public interface INBTPreservingIngredient {

    default boolean isValidForCraft(IRecipe recipe, InventoryCrafting craft, ItemStack stack, ItemStack output) {
        return false;
    }

}

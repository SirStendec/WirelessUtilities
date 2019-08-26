package com.lordmau5.wirelessutils.utils.crafting;

import com.google.gson.JsonObject;
import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class CopyNBTShapelessRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);

        return new CopyNBTShapelessRecipe(
                new ResourceLocation(WirelessUtils.MODID, "copy_nbt_shapeless_crafting"),
                recipe.getIngredients(),
                recipe.getRecipeOutput()
        );
    }

    public static class CopyNBTShapelessRecipe extends ShapelessOreRecipe {
        public CopyNBTShapelessRecipe(ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result) {
            super(group, input, result);
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting craft) {
            ItemStack output = this.output.copy();
            NBTTagCompound outTag = null;

            for (int i = 0; i < craft.getSizeInventory(); i++) {
                ItemStack stack = craft.getStackInSlot(i);
                if ( !stack.isEmpty() ) {
                    Item item = stack.getItem();
                    if ( item instanceof INBTPreservingIngredient && ((INBTPreservingIngredient) item).isValidForCraft(this, craft, stack, output) ) {
                        outTag = ((INBTPreservingIngredient) item).getNBTTagForCraft(this, craft, stack, output);
                        if ( outTag != null )
                            break;
                    } else {
                        Block block = Block.getBlockFromItem(item);
                        if ( block instanceof INBTPreservingIngredient && ((INBTPreservingIngredient) block).isValidForCraft(this, craft, stack, output) ) {
                            outTag = ((INBTPreservingIngredient) block).getNBTTagForCraft(this, craft, stack, output);
                            if ( outTag != null )
                                break;
                        }
                    }
                }
            }

            output.setTagCompound(outTag);
            return output;
        }
    }
}

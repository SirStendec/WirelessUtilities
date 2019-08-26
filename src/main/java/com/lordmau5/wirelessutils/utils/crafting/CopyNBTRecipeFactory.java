package com.lordmau5.wirelessutils.utils.crafting;

import com.google.gson.JsonObject;
import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class CopyNBTRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
        primer.height = recipe.getRecipeHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new CopyNBTRecipe(new ResourceLocation(WirelessUtils.MODID, "copy_nbt_crafting"), recipe.getRecipeOutput(), primer);
    }

    public static class CopyNBTRecipe extends ShapedOreRecipe {
        public CopyNBTRecipe(ResourceLocation group, ItemStack result, CraftingHelper.ShapedPrimer primer) {
            super(group, result, primer);
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

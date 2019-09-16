package com.lordmau5.wirelessutils.utils;

import cofh.core.inventory.ComparableItemStackValidated;
import com.google.common.base.MoreObjects;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.crafting.IWUCraftingMachine;
import com.lordmau5.wirelessutils.utils.crafting.IWURecipe;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChargerRecipeManager {
    private static Map<ComparableItemStackValidated, ChargerRecipe> allRecipes = new Object2ObjectOpenHashMap<>();
    private static final Set<ItemStack> allBlocks = new ObjectOpenHashSet<>();
    private static final Set<ComparableItemStackValidated> blocks = new ObjectOpenHashSet<>();

    public static Map<ComparableItemStackValidated, ItemStack> smelterOutput = new Object2ObjectOpenHashMap<>();

    private static final Map<ComparableItemStackValidated, ChargerRecipe> recipeMap = new Object2ObjectOpenHashMap<>();
    private static final Map<ComparableItemStackValidated, Set<ChargerRecipe>> outputMap = new Object2ObjectOpenHashMap<>();

    public static ItemStack getSmeltingResult(@Nonnull ItemStack input) {
        if ( input.isEmpty() )
            return null;

        ComparableItemStackValidated query = new ComparableItemStackValidated(input);
        ItemStack output = smelterOutput.get(query);

        if ( output == null ) {
            output = FurnaceRecipes.instance().getSmeltingResult(input);
            smelterOutput.put(query, output);
        }

        return output;
    }

    public static ChargerRecipe getRecipe(@Nonnull ItemStack input) {
        if ( input.isEmpty() )
            return null;

        ComparableItemStackValidated query = new ComparableItemStackValidated(input);
        ChargerRecipe recipe = recipeMap.get(query);

        if ( recipe == null ) {
            query.metadata = OreDictionary.WILDCARD_VALUE;
            recipe = recipeMap.get(query);
        }

        return recipe;
    }

    public static Set<ChargerRecipe> getOutputs(Item output) {
        return getOutputs(new ItemStack(output));
    }

    public static Set<ChargerRecipe> getOutputs(@Nonnull ItemStack output) {
        ComparableItemStackValidated query = new ComparableItemStackValidated(output);
        Set<ChargerRecipe> out = outputMap.get(query);

        if ( out == null ) {
            query.metadata = OreDictionary.WILDCARD_VALUE;
            out = outputMap.get(query);
        }

        return out;
    }

    public static boolean recipeExists(@Nonnull ItemStack input) {
        return getRecipe(input) != null;
    }

    public static boolean recipeForExists(@Nonnull ItemStack output) {
        return getOutputs(output) != null;
    }

    public static ChargerRecipe[] getRecipeList() {
        return recipeMap.values().toArray(new ChargerRecipe[0]);
    }

    public static Map<ComparableItemStackValidated, ChargerRecipe> getAllRecipes() {
        return allRecipes;
    }

    public static ChargerRecipe addRecipe(Item input, Item output, int cost) {
        return addRecipe(new ItemStack(input), new ItemStack(output), cost);
    }

    public static ChargerRecipe addRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output, int cost) {
        return addRecipe(input, output, cost, 200);
    }

    public static ChargerRecipe addRecipe(Item input, Item output, int cost, int ticks) {
        return addRecipe(new ItemStack(input), new ItemStack(output), cost, ticks);
    }

    public static ChargerRecipe addRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output, int cost, int ticks) {
        return addRecipe(new ChargerRecipe(input, output, cost, ticks));
    }

    public static ChargerRecipe addRecipe(ChargerRecipe recipe) {
        if ( recipe == null || recipe.input == null || recipe.input.isEmpty() || recipe.output == null || recipe.output.isEmpty() )
            return null;

        ComparableItemStackValidated input = new ComparableItemStackValidated(recipe.input);
        allRecipes.put(input, recipe);

        if ( blocks.contains(input) )
            return recipe;

        recipeMap.put(input, recipe);

        ComparableItemStackValidated out = new ComparableItemStackValidated(recipe.output);
        Set<ChargerRecipe> outs = outputMap.get(out);
        if ( outs == null ) {
            outs = new ObjectOpenHashSet<>();
            outputMap.put(out, outs);
        }

        outs.add(recipe);
        return recipe;
    }

    public static void removeRecipe(@Nonnull ItemStack input) {
        if ( input.isEmpty() )
            return;

        ComparableItemStackValidated query = new ComparableItemStackValidated(input);
        ChargerRecipe recipe = allRecipes.get(query);
        if ( recipe == null )
            return;

        allRecipes.remove(query);
        recipeMap.remove(query);

        Set<ChargerRecipe> outs = outputMap.get(new ComparableItemStackValidated(recipe.output));
        if ( outs != null )
            outs.remove(recipe);
    }

    public static void addBlock(@Nonnull ItemStack inputIn) {
        if ( inputIn.isEmpty() )
            return;

        if ( allBlocks.contains(inputIn) )
            return;

        allBlocks.add(inputIn);

        ComparableItemStackValidated input = new ComparableItemStackValidated(inputIn);
        if ( blocks.contains(input) )
            return;

        blocks.add(input);
        ChargerRecipe recipe = recipeMap.get(input);
        if ( recipe != null ) {
            recipeMap.remove(input);
            Set<ChargerRecipe> outs = outputMap.get(new ComparableItemStackValidated(recipe.output));
            if ( outs != null )
                outs.remove(recipe);
        }
    }

    public static void removeBlock(@Nonnull ItemStack inputIn) {
        if ( inputIn.isEmpty() )
            return;

        if ( !allBlocks.contains(inputIn) )
            return;

        allBlocks.remove(inputIn);

        ComparableItemStackValidated input = new ComparableItemStackValidated(inputIn);
        if ( !blocks.contains(input) )
            return;

        blocks.remove(input);
        ChargerRecipe recipe = allRecipes.get(input);
        if ( recipe != null ) {
            recipeMap.put(input, recipe);
            ComparableItemStackValidated output = new ComparableItemStackValidated(recipe.output);
            Set<ChargerRecipe> outs = outputMap.get(output);
            if ( outs == null ) {
                outs = new ObjectOpenHashSet<>();
                outputMap.put(output, outs);
            }

            outs.add(recipe);
        }
    }

    public static void refresh() {
        recipeMap.clear();
        outputMap.clear();
        blocks.clear();
        smelterOutput.clear();

        for (ItemStack stack : allBlocks)
            blocks.add(new ComparableItemStackValidated(stack));

        Map<ComparableItemStackValidated, ChargerRecipe> newAll = new Object2ObjectOpenHashMap<>(allRecipes.size());

        for (ChargerRecipe recipe : allRecipes.values()) {
            ComparableItemStackValidated input = new ComparableItemStackValidated(recipe.input);
            newAll.put(input, recipe);

            if ( blocks.contains(input) )
                continue;

            recipeMap.put(input, recipe);
            ComparableItemStackValidated output = new ComparableItemStackValidated(recipe.output);
            Set<ChargerRecipe> outs = outputMap.get(output);
            if ( outs == null ) {
                outs = new ObjectOpenHashSet<>();
                outputMap.put(output, outs);
            }

            outs.add(recipe);
        }

        allRecipes.clear();
        allRecipes = newAll;
    }

    public static class ChargerRecipe implements IWURecipe {
        public final ItemStack input;
        public final ItemStack output;
        public final int cost;
        public final int ticks;

        public ChargerRecipe(ItemStack input, ItemStack output, int cost, int ticks) {
            this.input = input;
            this.output = output;
            this.cost = cost;
            this.ticks = ticks;
        }

        @Override
        public void addTooltip(List<String> list, IWUCraftingMachine machine) {
            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".crafting.output",
                    output.getTextComponent()
            ).setStyle(TextHelpers.GRAY).getFormattedText());

            list.add("");

            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".crafting.input",
                    input.getTextComponent()
            ).setStyle(TextHelpers.GRAY).getFormattedText());

            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".crafting.power",
                    TextHelpers.getComponent(cost).setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("input", input)
                    .add("output", output)
                    .add("cost", cost)
                    .add("ticks", ticks)
                    .toString();
        }
    }
}

package com.lordmau5.wirelessutils.utils;

import cofh.core.inventory.ComparableItemStackValidated;
import cofh.core.util.helpers.FluidHelper;
import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class CondenserRecipeManager {

    private static Map<CondenserRecipeInput, CondenserRecipe> allRecipes = new Object2ObjectOpenHashMap<>();
    private static Set<CondenserRecipeInput> blocks = new ObjectOpenHashSet<>();

    private static final Map<CondenserRecipeInput, CondenserRecipe> recipeMap = new Object2ObjectOpenHashMap<>();
    private static final Map<ComparableItemStackValidated, Set<CondenserRecipe>> outputMap = new Object2ObjectOpenHashMap<>();

    public static CondenserRecipe getRecipe(FluidStack fluid, @Nonnull ItemStack input) {
        if ( fluid == null )
            return null;

        return getRecipe(new CondenserRecipeInput(fluid, input));
    }

    public static CondenserRecipe getRecipe(CondenserRecipeInput input) {
        if ( input == null )
            return null;

        CondenserRecipe recipe = recipeMap.get(input);
        if ( recipe == null ) {
            input.item.metadata = OreDictionary.WILDCARD_VALUE;
            recipe = recipeMap.get(input);
        }

        return recipe;
    }

    public static Set<CondenserRecipe> getOutputs(@Nonnull Item output) {
        return getOutputs(new ItemStack(output));
    }

    public static Set<CondenserRecipe> getOutputs(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() )
            return null;

        ComparableItemStackValidated query = new ComparableItemStackValidated(stack);
        Set<CondenserRecipe> out = outputMap.get(query);

        if ( out == null ) {
            query.metadata = OreDictionary.WILDCARD_VALUE;
            out = outputMap.get(query);
        }

        return out;
    }

    public static boolean recipeExists(FluidStack fluid, @Nonnull ItemStack input) {
        return recipeExists(new CondenserRecipeInput(fluid, input));
    }

    public static boolean recipeExists(CondenserRecipeInput input) {
        return getRecipe(input) != null;
    }

    public static boolean recipeForExists(@Nonnull ItemStack output) {
        return getOutputs(output) != null;
    }

    public static CondenserRecipe[] getRecipeList() {
        return recipeMap.values().toArray(new CondenserRecipe[0]);
    }

    public static Map<CondenserRecipeInput, CondenserRecipe> getAllRecipes() {
        return allRecipes;
    }

    public static CondenserRecipe addRecipe(FluidStack fluid, @Nonnull ItemStack input, @Nonnull ItemStack output, int cost) {
        return addRecipe(fluid, input, output, cost, 200);
    }

    public static CondenserRecipe addRecipe(FluidStack fluid, @Nonnull ItemStack input, @Nonnull ItemStack output, int cost, int ticks) {
        return addRecipe(new CondenserRecipe(fluid, input, output, cost, ticks));
    }

    public static CondenserRecipe addRecipe(CondenserRecipe recipe) {
        if ( recipe == null || recipe.input == null || recipe.input.isEmpty() || recipe.output == null || recipe.output.isEmpty() || recipe.fluid == null || recipe.fluid.amount == 0 || recipe.fluid.getFluid() == null )
            return null;

        allRecipes.put(recipe.recipeInput, recipe);

        if ( blocks.contains(recipe.recipeInput) )
            return recipe;

        recipeMap.put(recipe.recipeInput, recipe);

        ComparableItemStackValidated out = new ComparableItemStackValidated(recipe.output);
        Set<CondenserRecipe> outs = outputMap.get(out);
        if ( outs == null ) {
            outs = new ObjectOpenHashSet<>();
            outputMap.put(out, outs);
        }

        outs.add(recipe);
        return recipe;
    }

    public static void removeRecipe(FluidStack fluid, @Nonnull ItemStack input) {
        if ( input.isEmpty() || fluid == null )
            return;

        removeRecipe(new CondenserRecipeInput(fluid, input));
    }

    public static void removeRecipe(CondenserRecipeInput input) {
        if ( input == null )
            return;

        CondenserRecipe recipe = allRecipes.get(input);
        if ( recipe == null )
            return;

        allRecipes.remove(input);
        recipeMap.remove(input);

        ComparableItemStackValidated output = new ComparableItemStackValidated(recipe.output);
        Set<CondenserRecipe> outs = outputMap.get(output);
        if ( outs != null ) {
            outs.remove(recipe);
            if ( outs.isEmpty() )
                outputMap.remove(output);
        }
    }

    public static void addBlock(FluidStack fluid, @Nonnull ItemStack input) {
        if ( input.isEmpty() || fluid == null )
            return;

        addBlock(new CondenserRecipeInput(fluid, input));
    }

    public static void addBlock(CondenserRecipeInput input) {
        if ( blocks.contains(input) )
            return;

        blocks.add(input);

        CondenserRecipe recipe = recipeMap.get(input);
        if ( recipe != null ) {
            recipeMap.remove(input);
            ComparableItemStackValidated output = new ComparableItemStackValidated(recipe.output);
            Set<CondenserRecipe> outs = outputMap.get(output);
            if ( outs != null ) {
                outs.remove(recipe);
                if ( outs.isEmpty() )
                    outputMap.remove(output);
            }
        }
    }

    public static void removeBlock(FluidStack fluid, @Nonnull ItemStack input) {
        if ( input.isEmpty() || fluid == null )
            return;

        removeBlock(new CondenserRecipeInput(fluid, input));
    }

    public static void removeBlock(CondenserRecipeInput input) {
        if ( !blocks.contains(input) )
            return;

        blocks.remove(input);

        CondenserRecipe recipe = allRecipes.get(input);
        if ( recipe != null ) {
            recipeMap.put(input, recipe);
            ComparableItemStackValidated output = new ComparableItemStackValidated(recipe.output);
            Set<CondenserRecipe> outs = outputMap.get(output);
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
        Map<CondenserRecipeInput, CondenserRecipe> tempAll = allRecipes;
        Set<CondenserRecipeInput> tempBlocks = blocks;
        allRecipes = new Object2ObjectOpenHashMap<>(tempAll.size());
        blocks = new ObjectOpenHashSet<>(tempBlocks.size());

        blocks.addAll(tempBlocks);

        for (CondenserRecipe recipe : tempAll.values()) {
            allRecipes.put(recipe.recipeInput, recipe);
            if ( blocks.contains(recipe.recipeInput) )
                continue;

            recipeMap.put(recipe.recipeInput, recipe);
            ComparableItemStackValidated output = new ComparableItemStackValidated(recipe.output);
            Set<CondenserRecipe> outs = outputMap.get(output);
            if ( outs == null ) {
                outs = new ObjectOpenHashSet<>();
                outputMap.put(output, outs);
            }

            outs.add(recipe);
        }
    }


    public static class CondenserRecipeInput {
        public final FluidStack fluid;
        public final ComparableItemStackValidated item;

        public CondenserRecipeInput(FluidStack fluid, @Nonnull ItemStack item) {
            this.fluid = fluid;
            this.item = new ComparableItemStackValidated(item);
        }

        public CondenserRecipeInput(FluidStack fluid, ComparableItemStackValidated item) {
            this.fluid = fluid;
            this.item = item;
        }

        @Override
        public boolean equals(Object o) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;
            CondenserRecipeInput that = (CondenserRecipeInput) o;
            return FluidHelper.getFluidHash(fluid) == FluidHelper.getFluidHash(that.fluid) &&
                    (item == null ? that.item == null : item.isEqual(that.item));
        }

        @Override
        public int hashCode() {
            return FluidHelper.getFluidHash(fluid) * 31 + item.hashCode();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("fluid", fluid)
                    .add("item", item)
                    .toString();
        }
    }

    public static class CondenserRecipe {
        public final CondenserRecipeInput recipeInput;
        public final FluidStack fluid;
        public final ItemStack input;
        public final ItemStack output;
        public final int cost;
        public final int ticks;

        public CondenserRecipe(FluidStack fluid, ItemStack input, ItemStack output, int cost, int ticks) {
            recipeInput = new CondenserRecipeInput(fluid, input);
            this.fluid = fluid;
            this.input = input;
            this.output = output;
            this.cost = cost;
            this.ticks = ticks;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("fluid", fluid)
                    .add("input", input)
                    .add("output", output)
                    .add("cost", cost)
                    .add("ticks", ticks)
                    .toString();
        }
    }

}

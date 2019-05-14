package com.lordmau5.wirelessutils.plugins.CraftTweaker;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.entity.pearl.EntityFluxedPearl;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.crops.BehaviorManager;
import com.lordmau5.wirelessutils.utils.crops.CropBehavior;
import com.lordmau5.wirelessutils.utils.crops.MetaBreakBehavior;
import com.lordmau5.wirelessutils.utils.crops.MetaTallBehavior;
import com.lordmau5.wirelessutils.utils.crops.SimpleBreakBehavior;
import com.lordmau5.wirelessutils.utils.crops.TallBehavior;
import com.lordmau5.wirelessutils.utils.crops.TallCropBehavior;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class CraftTweakerPlugin implements IPlugin {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CraftTweakerAPI.registerClass(ChargerIntegration.class);
        CraftTweakerAPI.registerClass(CondenserIntegration.class);
        CraftTweakerAPI.registerClass(LevelIntegration.class);
        CraftTweakerAPI.registerClass(PearlReactions.class);
        CraftTweakerAPI.registerClass(FluidColors.class);
        CraftTweakerAPI.registerClass(CropBehaviors.class);
    }

    private static FluidStack getFluidStack(ILiquidStack stack) {
        if ( stack == null )
            return null;

        Object internal = stack.getInternal();
        if ( internal instanceof FluidStack )
            return (FluidStack) internal;

        return null;
    }

    @Nonnull
    private static ItemStack getItemStack(IItemStack stack) {
        if ( stack == null )
            return ItemStack.EMPTY;

        Object internal = stack.getInternal();
        if ( internal instanceof ItemStack )
            return (ItemStack) internal;

        return ItemStack.EMPTY;
    }

    @Nullable
    private static Set<Block> getBlockList(IItemStack[] blocks) {
        Set<Block> targets = new HashSet<>();
        if ( blocks != null )
            for (IItemStack iStack : blocks) {
                ItemStack stack = getItemStack(iStack);
                Item item = stack.getItem();
                Block block = item instanceof ItemBlockSpecial ? ((ItemBlockSpecial) item).getBlock() : Block.getBlockFromItem(item);
                if ( block != null && block != Blocks.AIR )
                    targets.add(block);
            }

        if ( targets.isEmpty() ) {
            CraftTweakerAPI.logError("Invalid Crop Behavior: No valid blocks found. Blocks must be instances of BlockCrop");
            return null;
        }

        return targets;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    private static Set<IBlockState> getBlockStateList(IItemStack[] blocks) {
        Set<IBlockState> targets = new HashSet<>();
        if ( blocks != null )
            for (IItemStack iStack : blocks) {
                ItemStack stack = getItemStack(iStack);
                Item item = stack.getItem();
                Block block = item instanceof ItemBlockSpecial ? ((ItemBlockSpecial) item).getBlock() : Block.getBlockFromItem(item);
                if ( block != null && block != Blocks.AIR )
                    targets.add(block.getStateFromMeta(item.getMetadata(stack)));
            }

        if ( targets.isEmpty() ) {
            CraftTweakerAPI.logError("Invalid Crop Behavior: No valid blocks found. Blocks must be instances of BlockCrop");
            return null;
        }

        return targets;
    }

    @SuppressWarnings("unused")
    @ZenClass("mods.wirelessutils.crop_behaviors")
    public static class CropBehaviors {
        @ZenMethod
        public static void addBreak(int priority, IItemStack[] blocks) {
            Set<Block> targets = getBlockList(blocks);
            if ( targets == null )
                return;

            SimpleBreakBehavior behavior = new SimpleBreakBehavior(targets);
            behavior.priority = priority;
            BehaviorManager.addBehavior(behavior);
        }

        @ZenMethod
        public static void addTall(int priority, IItemStack[] blocks, @Optional boolean breakBottom) {
            Set<Block> targets = getBlockList(blocks);
            if ( targets == null )
                return;

            TallBehavior behavior = new TallBehavior(targets, breakBottom);
            behavior.priority = priority;
            BehaviorManager.addBehavior(behavior);
        }

        @ZenMethod
        public static void addMetaBreak(int priority, IItemStack[] blocks) {
            Set<IBlockState> targets = getBlockStateList(blocks);
            if ( targets == null )
                return;

            MetaBreakBehavior behavior = new MetaBreakBehavior(targets);
            behavior.priority = priority;
            BehaviorManager.addBehavior(behavior);
        }

        @ZenMethod
        public static void addMetaTall(int priority, IItemStack[] blocks, @Optional boolean breakBottom) {
            Set<IBlockState> targets = getBlockStateList(blocks);
            if ( targets == null )
                return;

            MetaTallBehavior behavior = new MetaTallBehavior(targets, breakBottom);
            behavior.priority = priority;
            BehaviorManager.addBehavior(behavior);
        }

        @ZenMethod
        public static void addCrop(int priority, IItemStack[] blocks) {
            Set<Block> targets = getBlockList(blocks);
            if ( targets == null )
                return;

            CropBehavior behavior = new CropBehavior(targets);
            behavior.priority = priority;
            BehaviorManager.addBehavior(behavior);
        }

        @ZenMethod
        public static void addTallCrop(int priority, IItemStack[] blocks, @Optional boolean silkTouchAll) {
            Set<Block> targets = getBlockList(blocks);
            if ( targets == null )
                return;

            TallCropBehavior behavior = new TallCropBehavior(targets, silkTouchAll);
            behavior.priority = priority;
            BehaviorManager.addBehavior(behavior);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @ZenClass("mods.wirelessutils.pearl_reactions")
    public static class PearlReactions {
        @SuppressWarnings("unused")
        @ZenMethod
        public static void clear() {
            EntityFluxedPearl.clearReactions();
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void remove(IItemStack inputIn) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Remove Pearl Reaction: input is empty");
                return;
            }

            Block block = Block.getBlockFromItem(input.getItem());
            if ( block == null || block == Blocks.AIR ) {
                CraftTweakerAPI.logError("Invalid Remove Pearl Reaction: input has no associated block");
                return;
            }

            EntityFluxedPearl.removeReaction(block);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void add(IItemStack inputIn, String type, @Optional double scale) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Add Pearl Reaction: input is empty");
                return;
            }

            Block block = Block.getBlockFromItem(input.getItem());
            if ( block == null || block == Blocks.AIR ) {
                CraftTweakerAPI.logError("Invalid Add Pearl Reaction: input has no associated block");
                return;
            }

            EntityBaseThrowable.HitReactionType hrtype = EntityBaseThrowable.HitReactionType.valueOf(type);
            if ( hrtype == null ) {
                CraftTweakerAPI.logError("Invalid Add Pearl Reaction: hit reaction type is invalid");
                return;
            }

            if ( scale != 0 )
                EntityFluxedPearl.addReaction(block, hrtype, scale);
            else
                EntityFluxedPearl.addReaction(block, hrtype);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @ZenClass("mods.wirelessutils.levels")
    public static class LevelIntegration {
        @SuppressWarnings("unused")
        @ZenMethod
        public static void clear() {
            Level.clearLevels();
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static int size() {
            return Level.values().length;
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static ILevelWrapper get(int index) {
            Level level = Level.getLevel(index);
            if ( level == null )
                return null;

            return new LevelWrapper(level);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void remove(int index) {
            Level level = Level.getLevel(index);
            if ( level != null )
                Level.removeLevel(level);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static ILevelWrapper add(@Optional String name, @Optional int augmentSlots, @Optional int rarity,
                                        @Optional int color, @Optional long maxChargerTransfer,
                                        @Optional long maxChargerCapacity, @Optional int craftingTPT,
                                        @Optional int baseEnergyPerOperation, @Optional long maxEnergyCapacity,
                                        @Optional int maxCondenserTransfer, @Optional int maxCondenserCapacity,
                                        @Optional int maxItemsPerTick, @Optional int gatherTicks) {
            augmentSlots = Math.min(9, Math.max(0, augmentSlots));
            rarity = Math.min(EnumRarity.values().length, Math.max(0, rarity));

            if ( gatherTicks == 0 )
                gatherTicks = 10;

            gatherTicks = Math.min(0, Math.max(127, gatherTicks));

            Level level = new Level(name, augmentSlots, EnumRarity.values()[rarity], color, maxChargerTransfer, maxChargerCapacity, craftingTPT, baseEnergyPerOperation, (byte) gatherTicks, maxEnergyCapacity, maxCondenserTransfer, maxCondenserCapacity, maxItemsPerTick, maxItemsPerTick, 1);
            Level.addLevel(level);
            return new LevelWrapper(level);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static ILevelWrapper add(@Optional String name, @Optional int augmentSlots, @Optional int rarity,
                                        @Optional int color, @Optional long maxChargerTransfer,
                                        @Optional long maxChargerCapacity, @Optional int craftingTPT,
                                        @Optional int baseEnergyPerOperation, @Optional long maxEnergyCapacity,
                                        @Optional int maxCondenserTransfer, @Optional int maxCondenserCapacity,
                                        @Optional int budgetPerTick, @Optional int maxBudget, @Optional int costPerItem, @Optional int gatherTicks) {
            augmentSlots = Math.min(9, Math.max(0, augmentSlots));
            rarity = Math.min(EnumRarity.values().length, Math.max(0, rarity));

            if ( gatherTicks == 0 )
                gatherTicks = 10;

            gatherTicks = Math.min(0, Math.max(127, gatherTicks));

            if ( budgetPerTick == 0 && maxBudget == 0 && costPerItem == 0 ) {
                budgetPerTick = 1;
                maxBudget = 1;
                costPerItem = 1;
            }

            Level level = new Level(name, augmentSlots, EnumRarity.values()[rarity], color, maxChargerTransfer, maxChargerCapacity, craftingTPT, baseEnergyPerOperation, (byte) gatherTicks, maxEnergyCapacity, maxCondenserTransfer, maxCondenserCapacity, budgetPerTick, maxBudget, costPerItem);
            Level.addLevel(level);
            return new LevelWrapper(level);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @ZenClass("mods.wirelessutils.fluid_colors")
    public static class FluidColors {
        @SuppressWarnings("unused")
        @ZenMethod
        public static void clear() {
            ColorHandler.fluidColorMap.clear();
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void remove(ILiquidStack fluidIn) {
            FluidStack stack = getFluidStack(fluidIn);
            if ( stack == null ) {
                CraftTweakerAPI.logError("Invalid Fluid: " + fluidIn.toString());
                return;
            }

            Fluid fluid = stack.getFluid();
            if ( fluid == null ) {
                CraftTweakerAPI.logError("Invalid Fluid: " + fluidIn.toString());
                return;
            }

            ColorHandler.fluidColorMap.remove(fluid.getName());
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void add(ILiquidStack fluidIn, int color) {
            FluidStack stack = getFluidStack(fluidIn);
            if ( stack == null ) {
                CraftTweakerAPI.logError("Invalid Fluid: " + fluidIn.toString());
                return;
            }

            Fluid fluid = stack.getFluid();
            if ( fluid == null ) {
                CraftTweakerAPI.logError("Invalid Fluid: " + fluidIn.toString());
                return;
            }

            ColorHandler.fluidColorMap.put(fluid.getName(), color);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @ZenClass("mods.wirelessutils.condenser")
    public static class CondenserIntegration {
        @SuppressWarnings("unused")
        @ZenMethod
        public static void addRecipe(ILiquidStack fluidIn, IItemStack inputIn, IItemStack outputIn, @Optional int energyCost, @Optional int craftTicks) {
            ItemStack input = getItemStack(inputIn);
            ItemStack output = getItemStack(outputIn);
            FluidStack fluid = getFluidStack(fluidIn);

            if ( fluid == null ) {
                CraftTweakerAPI.logError("Invalid Condenser Recipe: fluid is null");
                return;
            }

            if ( input.isEmpty() || output.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Condenser Recipe: input or output is empty");
                return;
            }

            if ( energyCost == 0 )
                energyCost = 400;

            if ( craftTicks != 0 )
                CondenserRecipeManager.addRecipe(fluid, input, output, energyCost, craftTicks);
            else
                CondenserRecipeManager.addRecipe(fluid, input, output, energyCost);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void blockRecipe(ILiquidStack fluidIn, IItemStack inputIn) {
            FluidStack fluid = getFluidStack(fluidIn);
            ItemStack input = getItemStack(inputIn);

            if ( fluid == null ) {
                CraftTweakerAPI.logError("Invalid Condenser Block: fluid is null");
                return;
            }

            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Condenser Block: input is empty");
                return;
            }

            CondenserRecipeManager.addBlock(fluid, input);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void unblockRecipe(ILiquidStack fluidIn, IItemStack inputIn) {
            FluidStack fluid = getFluidStack(fluidIn);
            ItemStack input = getItemStack(inputIn);

            if ( fluid == null ) {
                CraftTweakerAPI.logError("Invalid Condenser Unblock: fluid is null");
                return;
            }

            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Condenser Unblock: input is empty");
                return;
            }

            CondenserRecipeManager.removeBlock(fluid, input);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void removeRecipe(ILiquidStack fluidIn, IItemStack inputIn) {
            FluidStack fluid = getFluidStack(fluidIn);
            ItemStack input = getItemStack(inputIn);

            if ( fluid == null ) {
                CraftTweakerAPI.logError("Invalid Condenser Remove Recipe: fluid is null");
                return;
            }

            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Condenser Remove Recipe: input is empty");
                return;
            }

            CondenserRecipeManager.removeRecipe(fluid, input);
        }
    }


    @SuppressWarnings("WeakerAccess")
    @ZenClass("mods.wirelessutils.charger")
    public static class ChargerIntegration {
        @SuppressWarnings("unused")
        @ZenMethod
        public static void addRecipe(IItemStack inputIn, IItemStack outputIn, @Optional int energyCost, @Optional int craftTicks) {
            ItemStack input = getItemStack(inputIn);
            ItemStack output = getItemStack(outputIn);

            if ( input.isEmpty() || output.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Charger Recipe: input or output is empty");
                return;
            }

            if ( energyCost == 0 )
                energyCost = ModConfig.items.fluxedPearl.chargeEnergy;

            if ( craftTicks != 0 )
                ChargerRecipeManager.addRecipe(input, output, energyCost, craftTicks);
            else
                ChargerRecipeManager.addRecipe(input, output, energyCost);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void blockRecipe(IItemStack inputIn) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Charger Block: input is empty");
                return;
            }

            ChargerRecipeManager.addBlock(input);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void unblockRecipe(IItemStack inputIn) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Charger Unblock: input is empty");
                return;
            }

            ChargerRecipeManager.removeBlock(input);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void removeRecipe(IItemStack inputIn) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() ) {
                CraftTweakerAPI.logError("Invalid Charger Remove Recipe: input is empty");
                return;
            }

            ChargerRecipeManager.removeRecipe(input);
        }
    }
}

package com.lordmau5.wirelessutils.plugins.CraftTweaker;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.entity.pearl.EntityFluxedPearl;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IItemStack;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

public class CraftTweakerPlugin implements IPlugin {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CraftTweakerAPI.registerClass(ChargerIntegration.class);
        CraftTweakerAPI.registerClass(LevelIntegration.class);
        CraftTweakerAPI.registerClass(PearlReactions.class);
    }

    public static ItemStack getItemStack(IItemStack stack) {
        if ( stack == null )
            return ItemStack.EMPTY;

        Object internal = stack.getInternal();
        if ( internal instanceof ItemStack )
            return (ItemStack) internal;

        return ItemStack.EMPTY;
    }

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
            if ( input.isEmpty() )
                return;

            Block block = Block.getBlockFromItem(input.getItem());
            if ( block == null || block == Blocks.AIR )
                return;

            EntityFluxedPearl.removeReaction(block);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void add(IItemStack inputIn, String type, @Optional double scale) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() )
                return;

            Block block = Block.getBlockFromItem(input.getItem());
            if ( block == null || block == Blocks.AIR )
                return;

            EntityBaseThrowable.HitReactionType hrtype = EntityBaseThrowable.HitReactionType.valueOf(type);
            if ( hrtype == null )
                return;

            if ( scale != 0 )
                EntityFluxedPearl.addReaction(block, hrtype, scale);
            else
                EntityFluxedPearl.addReaction(block, hrtype);
        }
    }

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
                                        @Optional int maxItemsPerTick) {
            augmentSlots = Math.min(9, Math.max(0, augmentSlots));
            rarity = Math.min(EnumRarity.values().length, Math.max(0, rarity));

            Level level = new Level(name, augmentSlots, EnumRarity.values()[rarity], color, maxChargerTransfer, maxChargerCapacity, craftingTPT, baseEnergyPerOperation, maxEnergyCapacity, maxCondenserTransfer, maxCondenserCapacity, maxItemsPerTick);
            Level.addLevel(level);
            return new LevelWrapper(level);
        }
    }

    @ZenClass("mods.wirelessutils.charger")
    public static class ChargerIntegration {
        @SuppressWarnings("unused")
        @ZenMethod
        public static void addRecipe(IItemStack inputIn, IItemStack outputIn, @Optional int energyCost, @Optional int craftTicks) {
            ItemStack input = getItemStack(inputIn);
            ItemStack output = getItemStack(outputIn);

            if ( input.isEmpty() || output.isEmpty() )
                return;

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
            if ( input.isEmpty() )
                return;

            ChargerRecipeManager.addBlock(input);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void unblockRecipe(IItemStack inputIn) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() )
                return;

            ChargerRecipeManager.removeBlock(input);
        }

        @SuppressWarnings("unused")
        @ZenMethod
        public static void removeRecipe(IItemStack inputIn) {
            ItemStack input = getItemStack(inputIn);
            if ( input.isEmpty() )
                return;

            ChargerRecipeManager.removeRecipe(input);
        }
    }
}

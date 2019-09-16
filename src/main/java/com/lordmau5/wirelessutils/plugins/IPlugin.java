package com.lordmau5.wirelessutils.plugins;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPlugin {
    default void preInit(FMLPreInitializationEvent event) {

    }

    default void init(FMLInitializationEvent event) {

    }

    default void postInit(FMLPostInitializationEvent event) {

    }

    default void registerBlocks(RegistryEvent.Register<Block> event) {

    }

    default void registerItems(RegistryEvent.Register<Item> event) {

    }

    default void registerRecipes(RegistryEvent.Register<IRecipe> event) {

    }

    @SideOnly(Side.CLIENT)
    default void registerModels(ModelRegistryEvent event) {

    }

    @SideOnly(Side.CLIENT)
    default void initColors(BlockColors blockColors) {

    }

    @SideOnly(Side.CLIENT)
    default void initColors(ItemColors itemColors) {

    }
}

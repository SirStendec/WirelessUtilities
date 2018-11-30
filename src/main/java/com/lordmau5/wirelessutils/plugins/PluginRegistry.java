package com.lordmau5.wirelessutils.plugins;

import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.plugins.CraftTweaker.CraftTweakerPlugin;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.plugins.TConstruct.TConstructPlugin;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class PluginRegistry {
    private static final Map<String, IPlugin> plugins = new HashMap<>();

    private static void addPlugin(String modid, IPlugin plugin) {
        plugins.put(modid, plugin);
    }

    private static void findPlugins() {
        if ( Loader.isModLoaded("refinedstorage") ) {
            addPlugin("refinedstorage", new RefinedStoragePlugin());
        }

        if ( Loader.isModLoaded("tconstruct") ) {
            addPlugin("tconstruct", new TConstructPlugin());
        }

        if ( Loader.isModLoaded("crafttweaker") ) {
            addPlugin("crafttweaker", new CraftTweakerPlugin());
        }

        if ( Loader.isModLoaded("appliedenergistics2") ) {
            addPlugin("appliedenergistics2", new AppliedEnergistics2Plugin());
        }

//        if ( Loader.isModLoaded("xnet") ) {
//            addPlugin("xnet", new XNetPlugin());
//        }
    }

    public static void preInit(FMLPreInitializationEvent event) {
        findPlugins();

        plugins.values().forEach(plugin -> plugin.preInit(event));
    }

    public static void init(FMLInitializationEvent event) {
        plugins.values().forEach(plugin -> plugin.init(event));
    }

    public static void postInit(FMLPostInitializationEvent event) {
        plugins.values().forEach(plugin -> plugin.postInit(event));
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        plugins.values().forEach(plugin -> plugin.registerBlocks(event));
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        plugins.values().forEach(plugin -> plugin.registerItems(event));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        plugins.values().forEach(plugin -> plugin.registerModels(event));
    }

    @SideOnly(Side.CLIENT)
    public static void initColors(BlockColors blockColors) {
        plugins.values().forEach(plugin -> plugin.initColors(blockColors));
    }

    @SideOnly(Side.CLIENT)
    public static void initColors(ItemColors itemColors) {
        plugins.values().forEach(plugin -> plugin.initColors(itemColors));
    }
}

package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.ItemBlockMachine;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional.BlockDirectionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional.TileDirectionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional.BlockPositionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional.TilePositionalAENetwork;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AppliedEnergistics2Plugin implements IPlugin {
    public static BlockDirectionalAENetwork blockDirectionalRSNetwork;

    public static BlockPositionalAENetwork blockPositionalRSNetwork;

    @GameRegistry.ObjectHolder("appliedenergistics2:quartz_ore")
    public static Item itemQuartzOre;

    @GameRegistry.ObjectHolder("appliedenergistics2:charged_quartz_ore")
    public static Item itemChargedQuartzOre;

    @GameRegistry.ObjectHolder("appliedenergistics2:material")
    public static Item itemMaterial;

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        blockDirectionalRSNetwork = new BlockDirectionalAENetwork();
        blockPositionalRSNetwork = new BlockPositionalAENetwork();

        event.getRegistry().register(blockDirectionalRSNetwork);
        event.getRegistry().register(blockPositionalRSNetwork);

        GameRegistry.registerTileEntity(TileDirectionalAENetwork.class, new ResourceLocation(WirelessUtils.MODID, "directional_ae_network"));
        GameRegistry.registerTileEntity(TilePositionalAENetwork.class, new ResourceLocation(WirelessUtils.MODID, "positional_ae_network"));
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        CommonProxy.registerItem(event, new ItemBlockMachine(blockDirectionalRSNetwork).setRegistryName(blockDirectionalRSNetwork.getRegistryName()));
        CommonProxy.registerItem(event, new ItemBlockMachine(blockPositionalRSNetwork).setRegistryName(blockPositionalRSNetwork.getRegistryName()));
    }

    @Override
    public void registerRecipes() {
        if ( itemQuartzOre != null && itemChargedQuartzOre != null )
            ChargerRecipeManager.addRecipe(itemQuartzOre, itemChargedQuartzOre, 8000);

        if ( itemMaterial != null )
            ChargerRecipeManager.addRecipe(new ItemStack(itemMaterial, 1, 0), new ItemStack(itemMaterial, 1, 1), 8000);
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        blockDirectionalRSNetwork.initModel();
        blockPositionalRSNetwork.initModel();
    }

    @Override
    public void initColors(BlockColors blockColors) {
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockDirectionalRSNetwork);
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockPositionalRSNetwork);
    }

    @Override
    public void initColors(ItemColors itemColors) {
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(blockDirectionalRSNetwork));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(blockPositionalRSNetwork));
    }
}

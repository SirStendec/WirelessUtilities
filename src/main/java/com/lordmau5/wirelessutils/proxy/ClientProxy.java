package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.repack.net.covers1624.model.LayeredTemplateModel;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.plugins.PluginRegistry;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.utils.Textures;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import com.lordmau5.wirelessutils.utils.mod.ModEntities;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private static final Minecraft minecraft = Minecraft.getMinecraft();

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        ModelLoaderRegistry.registerLoader(LayeredTemplateModel.Loader.INSTANCE);
        if ( RenderManager.INSTANCE.isEnabled() )
            MinecraftForge.EVENT_BUS.register(RenderManager.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        ModBlocks.initColors(minecraft.getBlockColors());
        ModItems.initColors(minecraft.getItemColors());

        PluginRegistry.initColors(minecraft.getBlockColors());
        PluginRegistry.initColors(minecraft.getItemColors());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void preStitch(TextureStitchEvent.Pre event) {
        Textures.registerIcons(event.getMap());

        event.getMap().registerSprite(new ResourceLocation(WirelessUtils.MODID, "block/side_input"));
        event.getMap().registerSprite(new ResourceLocation(WirelessUtils.MODID, "block/side_output"));
        event.getMap().registerSprite(new ResourceLocation(WirelessUtils.MODID, "block/side_disabled"));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.initModels();
        ModItems.initModels();
        ModEntities.initModels();

        PluginRegistry.registerModels(event);
    }
}

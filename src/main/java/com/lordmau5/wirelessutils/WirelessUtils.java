package com.lordmau5.wirelessutils;

import cofh.CoFHCore;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@Mod(modid = WirelessUtils.MODID, name = WirelessUtils.NAME, version = WirelessUtils.VERSION, dependencies = WirelessUtils.DEPENDENCIES, updateJSON = WirelessUtils.UPDATE_URL)
public class WirelessUtils {
    public static final String MODID = "wirelessutils";
    public static final String NAME = "Wireless Utilities";

    public static final String VERSION = "1.1";
    public static final String UPDATE_URL = "https://raw.github.com/sirstendec/wirelessutilities/master/update.json";

    public static final String DEPENDENCIES = CoFHCore.VERSION_GROUP;

    @SidedProxy(clientSide = "com.lordmau5.wirelessutils.proxy.ClientProxy", serverSide = "com.lordmau5.wirelessutils.proxy.ServerProxy")
    private static CommonProxy proxy;

    @Mod.Instance
    public static WirelessUtils instance;

    public static CreativeTabs creativeTabCU = new CreativeTabs(MODID) {
        @Override
        public int getSearchbarWidth() {
            return 70;
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }

        @Override
        @Nonnull
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            Block block = ModBlocks.blockDirectionalCharger;
            return block == null ? ItemStack.EMPTY : new ItemStack(block);
        }
    }.setBackgroundImageName("wirelessutils.png");

    @SuppressWarnings("unused")
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void handleIdMapping(FMLModIdMappingEvent event) {
        proxy.handleIdMapping(event);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        proxy.serverLoad(event);
    }
}


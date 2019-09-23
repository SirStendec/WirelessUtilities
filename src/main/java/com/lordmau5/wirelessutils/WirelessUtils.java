package com.lordmau5.wirelessutils;

import cofh.CoFHCore;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Mod(modid = WirelessUtils.MODID, name = WirelessUtils.NAME, version = WirelessUtils.VERSION, dependencies = WirelessUtils.DEPENDENCIES, updateJSON = WirelessUtils.UPDATE_URL)
public class WirelessUtils {
    public static final String MODID = "wirelessutils";
    public static final String NAME = "Wireless Utilities";
    public static final int DATA_VERSION = 1;

    public static final String VERSION = "1.9";
    public static final String UPDATE_URL = "https://raw.github.com/sirstendec/wirelessutilities/master/update.json";

    public static final String DEPENDENCIES = CoFHCore.VERSION_GROUP;

    public static final Logger logger = LogManager.getLogManager().getLogger(MODID);
    public static final Profiler profiler = new Profiler();

    @SidedProxy(clientSide = "com.lordmau5.wirelessutils.proxy.ClientProxy", serverSide = "com.lordmau5.wirelessutils.proxy.ServerProxy")
    private static CommonProxy proxy;

    @Mod.Instance
    public static WirelessUtils instance;

    public static final CreativeTabs creativeTabCU = new CreativeTabs(MODID) {
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

    // Permissions
    public static final String PERM_FLUIDGEN = MODID + ".commands.fluidgen";
    public static final String PERM_DEBUG = MODID + ".commands.debug";
    public static final String PERM_EDIT_ITEM = MODID + ".commands.edit_item";

    // GUIs
    private static int guiIndex = 10;

    public static final int GUI_ITEM = guiIndex++;
    public static final int GUI_ADMIN_ITEM = guiIndex++;

    static {
        if ( !FluidRegistry.isUniversalBucketEnabled() )
            FluidRegistry.enableUniversalBucket();
    }

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


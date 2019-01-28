package com.lordmau5.wirelessutils.plugins.Waila;

import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class WailaPlugin implements IPlugin {

    @Override
    public void init(FMLInitializationEvent event) {
        FMLInterModComms.sendMessage("waila", "register", this.getClass().getName() + ".register");
    }

    public static void register(IWailaRegistrar registrar)
    {
        IWailaDataProvider provider = new WirelessUtilitiesWailaDataProvider();
        registrar.registerBodyProvider(provider, ITileInfoProvider.class);
        registrar.registerNBTProvider(provider, ITileInfoProvider.class);
    }

}

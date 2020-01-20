package com.lordmau5.wirelessutils.plugins.Waila;

import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class WailaPlugin implements IPlugin {

    @Override
    public void init(FMLInitializationEvent event) {
        FMLInterModComms.sendMessage("waila", "register", this.getClass().getName() + ".register");
    }

    public static void register(IWailaRegistrar registrar) {
        IWailaDataProvider tileProvider = new WailaInfoProvider(true);
        IWailaDataProvider workProvider = new WailaInfoProvider(false);

        registrar.registerBodyProvider(tileProvider, ITileInfoProvider.class);
        registrar.registerNBTProvider(tileProvider, ITileInfoProvider.class);

        registrar.registerBodyProvider(workProvider, IWorkInfoProvider.class);
        registrar.registerNBTProvider(workProvider, IWorkInfoProvider.class);
    }

}

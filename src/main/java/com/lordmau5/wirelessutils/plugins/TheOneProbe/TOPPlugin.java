package com.lordmau5.wirelessutils.plugins.TheOneProbe;

import com.lordmau5.wirelessutils.plugins.IPlugin;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.function.Function;

public class TOPPlugin implements IPlugin, Function<ITheOneProbe, Void> {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", this.getClass().getName());
    }

    @Override
    public Void apply(ITheOneProbe input) {
        input.registerProvider(new WirelessUtilitiesProbeInfoProvider());

        return null;
    }
}
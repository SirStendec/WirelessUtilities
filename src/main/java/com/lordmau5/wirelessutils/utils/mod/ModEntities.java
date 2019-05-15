package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.entity.pearl.EntityChargedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityFluxedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityQuenchedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityScorchedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityStabilizedEnderPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityVoidPearl;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEntities {
    @SideOnly(Side.CLIENT)
    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(EntityChargedPearl.class, new EntityChargedPearl.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityFluxedPearl.class, new EntityFluxedPearl.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityQuenchedPearl.class, new EntityQuenchedPearl.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityScorchedPearl.class, new EntityScorchedPearl.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityStabilizedEnderPearl.class, new EntityStabilizedEnderPearl.Factory());
        RenderingRegistry.registerEntityRenderingHandler(EntityVoidPearl.class, new EntityVoidPearl.Factory());
    }
}
package com.lordmau5.wirelessutils.plugins.SimpleCorn;

import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.utils.crops.BehaviorManager;
import com.lordmau5.wirelessutils.utils.crops.TallCropBehavior;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SimpleCornPlugin implements IPlugin {
    @GameRegistry.ObjectHolder("simplecorn:corn")
    public static Block blockCorn;

    @GameRegistry.ObjectHolder("simplecorn:corn_mid")
    public static Block blockCornMid;

    @GameRegistry.ObjectHolder("simplecorn:corn_top")
    public static Block blockCornTop;

    @Override
    public void init(FMLInitializationEvent event) {
        if ( blockCorn instanceof BlockCrops ) {
            TallCropBehavior behavior = new TallCropBehavior(blockCorn, blockCornMid, blockCornTop);
            behavior.priority = 1;
            BehaviorManager.addBehavior(behavior);
        }
    }
}

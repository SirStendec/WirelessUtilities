package com.lordmau5.wirelessutils.plugins.TConstruct;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TConstructPlugin implements IPlugin {
    @GameRegistry.ObjectHolder("tconstruct:slime")
    public static Block blockSlime;

    @GameRegistry.ObjectHolder("tconstruct:slime_congealed")
    public static Block blockSlimeCongealed;

    @Override
    public void init(FMLInitializationEvent event) {
        if ( blockSlime != null )
            EntityBaseThrowable.addReaction(blockSlime, EntityBaseThrowable.HitReactionType.BOUNCE, 0.8);

        if ( blockSlimeCongealed != null )
            EntityBaseThrowable.addReaction(blockSlimeCongealed, EntityBaseThrowable.HitReactionType.BOUNCE, 1);
    }
}

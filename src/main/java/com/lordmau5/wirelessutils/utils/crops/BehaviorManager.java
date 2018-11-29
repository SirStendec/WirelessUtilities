package com.lordmau5.wirelessutils.utils.crops;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BehaviorManager {

    private static Map<Block, IHarvestBehavior> behaviorCache = new Object2ObjectOpenHashMap<>();
    private static List<IHarvestBehavior> behaviors = new ArrayList<>();

    static {
        addBehavior(new CropBehavior());
        addBehavior(new NetherWartBehavior());
        addBehavior(new PumpkinMelonBehavior());
        addBehavior(new TallBehavior());

        // TODO: Balance trees.
        //addBehavior(new TreeBehavior());
    }

    public static IHarvestBehavior getBehavior(Block block) {
        if ( block == null )
            return null;

        if ( behaviorCache.containsKey(block) )
            return behaviorCache.get(block);

        for (IHarvestBehavior behavior : behaviors) {
            if ( behavior.appliesTo(block) ) {
                behaviorCache.put(block, behavior);
                return behavior;
            }
        }

        behaviorCache.put(block, null);

        return null;
    }

    public static void addBehavior(IHarvestBehavior behavior) {
        behaviors.add(behavior);
        behaviors.sort(Comparator.comparingInt(IHarvestBehavior::getPriority));
        behaviorCache.clear();
    }

    public static void removeBehavior(IHarvestBehavior behavior) {
        if ( behaviors.contains(behavior) ) {
            behaviors.remove(behavior);
            behaviorCache.clear();
        }
    }
}

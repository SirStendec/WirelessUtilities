package com.lordmau5.wirelessutils.utils.crops;

import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BehaviorManager {

    private static final Map<IBlockState, IHarvestBehavior> behaviorCache = new Object2ObjectOpenHashMap<>();
    private static final List<IHarvestBehavior> behaviors = new ArrayList<>();

    static {
        addBehavior(new CropBehavior());
        addBehavior(new NetherWartBehavior());
        addBehavior(new SimpleBreakBehavior());
        addBehavior(new TallBehavior());

        if ( ModConfig.augments.crop.processTrees )
            addBehavior(new TreeBehavior());
    }

    public static IHarvestBehavior getBehavior(IBlockState state) {
        if ( state == null )
            return null;

        if ( behaviorCache.containsKey(state) )
            return behaviorCache.get(state);

        for (IHarvestBehavior behavior : behaviors) {
            if ( behavior.appliesTo(state) ) {
                behaviorCache.put(state, behavior);
                return behavior;
            }
        }

        behaviorCache.put(state, null);

        return null;
    }

    public static void addBehavior(IHarvestBehavior behavior) {
        behaviors.add(behavior);
        behaviors.sort(Comparator.comparingInt(IHarvestBehavior::getPriority).reversed());
        behaviorCache.clear();
    }

    public static void removeBehavior(IHarvestBehavior behavior) {
        if ( behaviors.contains(behavior) ) {
            behaviors.remove(behavior);
            behaviorCache.clear();
        }
    }
}

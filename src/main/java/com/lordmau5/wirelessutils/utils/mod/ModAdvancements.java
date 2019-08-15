package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.advancements.CustomTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class ModAdvancements {

    public static final CustomTrigger STORM_CHASER = new CustomTrigger("storm_chaser");
    public static final CustomTrigger REFRESHING = new CustomTrigger("refreshing");
    public static final CustomTrigger SO_HOT_RIGHT_NOW = new CustomTrigger("so_hot_right_now");
    public static final CustomTrigger UPGRADED = new CustomTrigger("upgraded");
    public static final CustomTrigger AUGMENTED = new CustomTrigger("augmented");
    public static final CustomTrigger SET_POSITIONAL_CARD = new CustomTrigger("set_positional_card");
    public static final CustomTrigger REPULSION = new CustomTrigger("repulsion");
    public static final CustomTrigger LONG_DISTANCE = new CustomTrigger("long_distance");
    public static final CustomTrigger ROUNDABOUT = new CustomTrigger("roundabout");
    public static final CustomTrigger ENLIGHTENED = new CustomTrigger("enlightened");
    public static final CustomTrigger THE_VOID_TOLLS = new CustomTrigger("the_void_tolls");
    public static final CustomTrigger FOR_THEE = new CustomTrigger("for_thee");

    public static void initTriggers() {
        CriteriaTriggers.register(STORM_CHASER);
        CriteriaTriggers.register(REFRESHING);
        CriteriaTriggers.register(SO_HOT_RIGHT_NOW);
        CriteriaTriggers.register(UPGRADED);
        CriteriaTriggers.register(AUGMENTED);
        CriteriaTriggers.register(SET_POSITIONAL_CARD);
        CriteriaTriggers.register(REPULSION);
        CriteriaTriggers.register(LONG_DISTANCE);
        CriteriaTriggers.register(ROUNDABOUT);
        CriteriaTriggers.register(ENLIGHTENED);
        CriteriaTriggers.register(THE_VOID_TOLLS);
        CriteriaTriggers.register(FOR_THEE);
    }
}

package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.text.TextComponentTranslation;

public class ModStatistics {

    public static final StatBase CAPTURED_MOBS = (new StatBasic("stat." + WirelessUtils.MODID + ".captured_mobs", new TextComponentTranslation("stat." + WirelessUtils.MODID + ".captured_mobs")).initIndependentStat().registerStat());

}

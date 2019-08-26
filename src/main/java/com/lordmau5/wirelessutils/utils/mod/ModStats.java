package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public enum ModStats {

    ThrownPearls,
    CrystallizedPearls,
    CapturedEntities;

    private final StatBasic stat;

    ModStats() {
        final String key = "stat." + WirelessUtils.MODID + "." + this.name();
        stat = new StatBasic(
                key,
                new TextComponentTranslation(key)
        );

        stat.registerStat();
    }

    public void addToPlayer(@Nonnull EntityPlayer player) {
        addToPlayer(player, 1);
    }

    public void addToPlayer(@Nonnull EntityPlayer player, int count) {
        player.addStat(stat, count);
    }
}
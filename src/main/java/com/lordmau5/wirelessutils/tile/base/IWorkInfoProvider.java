package com.lordmau5.wirelessutils.tile.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.GuiScreen;

public interface IWorkInfoProvider {

    String getWorkUnit();

    default String formatWorkUnit(long value) {
        if ( value < 1000000000 || GuiScreen.isShiftKeyDown() )
            return String.format("%s %s", StringHelper.formatNumber(value), getWorkUnit());

        return TextHelpers.getScaledNumber(value, getWorkUnit(), true);
    }

    long getWorkMaxRate();

    long getWorkLastTick();

    int getValidTargetCount();

    int getActiveTargetCount();

}

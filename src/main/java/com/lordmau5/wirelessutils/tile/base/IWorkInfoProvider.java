package com.lordmau5.wirelessutils.tile.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.GuiScreen;

public interface IWorkInfoProvider {

    String getWorkUnit();

    default String formatWorkUnit(double value) {
        long val = (long) value;
        if ( val < 1000000000 || GuiScreen.isShiftKeyDown() )
            return String.format("%s %s", StringHelper.formatNumber(val), getWorkUnit());

        return TextHelpers.getScaledNumber(val, getWorkUnit(), true);
    }

    double getWorkMaxRate();

    double getWorkLastTick();

    int getValidTargetCount();

    int getActiveTargetCount();

}

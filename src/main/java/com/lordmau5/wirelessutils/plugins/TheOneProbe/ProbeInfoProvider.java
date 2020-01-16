package com.lordmau5.wirelessutils.plugins.TheOneProbe;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ProbeInfoProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return WirelessUtils.MODID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity tile = world.getTileEntity(data.getPos());
        boolean processWork = tile instanceof IWorkInfoProvider;

        if ( tile instanceof ITileInfoProvider ) {
            ITileInfoProvider provider = (ITileInfoProvider) tile;

            List<String> tooltip = new ArrayList<>();
            provider.getInfoTooltip(tooltip, null);

            for (String string : tooltip)
                probeInfo.text(string);

            if ( processWork )
                processWork = !provider.skipWorkInfo();
        }

        if ( processWork ) {
            IWorkInfoProvider provider = (IWorkInfoProvider) tile;

            if ( !provider.getWorkConfigured() ) {
                IProbeInfo group = probeInfo.vertical(probeInfo.defaultLayoutStyle().borderColor(0xFFFF0000).spacing(2));
                group.text(TextStyleClass.ERROR + StringHelper.localize("info." + WirelessUtils.MODID + ".work_info.unconfigured.tooltip"));

                final String key = provider.getWorkUnconfiguredExplanation();
                final String[] lines = key == null ? null : TextHelpers.getLocalizedLines(key);
                if ( lines != null ) {
                    for (String line : lines)
                        group.text(line);
                }

            } else {
                probeInfo.text(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".work_info.tooltip.rate",
                        TextHelpers.getComponent(provider.formatWorkUnit(provider.getWorkLastTick()))
                ).getFormattedText());

                probeInfo.text(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".work_info.tooltip.targets",
                        TextHelpers.getComponent(provider.getActiveTargetCount()),
                        TextHelpers.getComponent(provider.getValidTargetCount())
                ).getFormattedText());
            }
        }
    }
}

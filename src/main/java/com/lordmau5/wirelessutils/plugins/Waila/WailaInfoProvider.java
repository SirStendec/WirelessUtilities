package com.lordmau5.wirelessutils.plugins.Waila;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WailaInfoProvider implements IWailaDataProvider {

    private static boolean shouldProcessTile(TileEntity tile) {
        if ( tile instanceof ITileInfoProvider && ((ITileInfoProvider) tile).skipWorkInfo() )
            return false;

        return tile instanceof IWorkInfoProvider;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if ( tooltip == null )
            tooltip = new ArrayList<>();

        boolean processWork = tile instanceof IWorkInfoProvider;
        if ( tile instanceof ITileInfoProvider ) {
            ITileInfoProvider provider = (ITileInfoProvider) tile;
            provider.getInfoTooltip(tooltip, tag);

            if ( processWork )
                processWork = !provider.skipWorkInfo();
        }

        if ( processWork ) {
            IWorkInfoProvider provider = (IWorkInfoProvider) tile;

            if ( tag.hasKey("work:r", Constants.NBT.TAG_STRING) ) {
                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".work_info.unconfigured.tooltip"
                ).setStyle(TextHelpers.RED).getFormattedText());
                TextHelpers.addLocalizedLines(tooltip, tag.getString("work:r"), null);

            } else {
                final int activeTargets = tag.getInteger("targets:a");
                final int validTargets = tag.getInteger("targets:v");

                final double work = tag.getDouble("work:l");

                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".work_info.tooltip.rate",
                        TextHelpers.getComponent(provider.formatWorkUnit(work))
                ).getFormattedText());

                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".work_info.tooltip.targets",
                        TextHelpers.getComponent(activeTargets),
                        TextHelpers.getComponent(validTargets)
                ).getFormattedText());
            }
        }

        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, BlockPos pos) {
        boolean processWork = tile instanceof IWorkInfoProvider;
        if ( tile instanceof ITileInfoProvider ) {
            ITileInfoProvider provider = (ITileInfoProvider) tile;
            tag = provider.getInfoNBT(tag, player);

            if ( processWork )
                processWork = !provider.skipWorkInfo();
        }

        if ( processWork ) {
            IWorkInfoProvider provider = (IWorkInfoProvider) tile;

            tag.setInteger("targets:a", provider.getActiveTargetCount());
            tag.setInteger("targets:v", provider.getValidTargetCount());
            tag.setDouble("work:l", provider.getWorkLastTick());
            if ( !provider.getWorkConfigured() ) {
                final String reason = provider.getWorkUnconfiguredExplanation();
                if ( reason != null )
                    tag.setString("work:r", reason);
            }
        }

        return tag;
    }
}

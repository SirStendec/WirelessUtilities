package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base;

import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cofh.core.gui.element.tab.TabBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class TabChannels extends TabBase {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");
    public static final String INTL_KEY = "info." + WirelessUtils.MODID + ".ae_channels";

    public static final int defaultSide = 0;
    public static final int defaultHeaderColor = 0xE1C92F;
    public static final int defaultSubHeaderColor = 0xAAAFB8;
    public static final int defaultTextColor = 0x000000;
    public static final int defaultBackgroundColor = 0x089e4c;

    private final BaseGuiContainer gui;
    private final TileAENetworkBase provider;

    public TabChannels(BaseGuiContainer gui, TileAENetworkBase provider) {
        this(gui, defaultSide, provider);
    }

    public TabChannels(BaseGuiContainer gui, int side, TileAENetworkBase provider) {
        super(gui, side);
        this.gui = gui;
        this.provider = provider;

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        maxHeight = 64;
        maxWidth = 100;

        if ( !AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) )
            setVisible(false);
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !isFullyOpened() ) {
            final boolean dense = ModConfig.plugins.appliedEnergistics.denseCableConnection;
            final int channels = provider.getUsedChannels();

            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".ae2.channels",
                    channels, dense ? 32 : 8
            ).getFormattedText());

            return;
        }
    }
}

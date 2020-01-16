package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base;

import appeng.api.util.AEColor;
import cofh.core.gui.GuiContainerCore;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicButton;
import com.lordmau5.wirelessutils.utils.Textures;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class ElementColorButton extends ElementDynamicButton {

    private static final int ALPHA = 0xFF000000;

    private final TileAENetworkBase provider;
    private AEColor lastColor = null;

    public ElementColorButton(GuiContainerCore gui, TileAENetworkBase tile, int posX, int posY) {
        super(gui, posX, posY, 16, 16);
        this.provider = tile;

        setIcon(Textures.COLOR);
        update();
    }

    @Override
    public void update() {
        super.update();

        AEColor color = provider.getAEColor();
        if ( color == lastColor )
            return;

        lastColor = color;

        setForegroundColor(ALPHA | color.getVariantByTintIndex(AEColor.TINTINDEX_MEDIUM));
        setForegroundHoverColor(ALPHA | color.getVariantByTintIndex(AEColor.TINTINDEX_MEDIUM_BRIGHT));
    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);

        if ( lastColor == null )
            return;

        list.add(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + ".ae2.color",
                TextHelpers.getComponent(lastColor.toString()).setStyle(TextHelpers.YELLOW)
        ).setStyle(TextHelpers.WHITE).getFormattedText());

        final String key;
        if ( lastColor == AEColor.TRANSPARENT )
            key = "none";
        else if ( ModConfig.plugins.appliedEnergistics.colorsWireless )
            key = "no_sides";
        else
            key = "sides";

        TextHelpers.addLocalizedLines(
                list,
                "btn." + WirelessUtils.MODID + ".ae2.color." + key,
                TextHelpers.GRAY
        );
    }

    @Override
    public void onClick() {
        provider.setAEColor(provider.getAEColor().ordinal() + 1);
        provider.sendModePacket();
        GuiContainerCore.playClickSound(1F);
    }

    @Override
    public void onRightClick() {
        provider.setAEColor(provider.getAEColor().ordinal() - 1);
        provider.sendModePacket();
        GuiContainerCore.playClickSound(0.7F);
    }
}

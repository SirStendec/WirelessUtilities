package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBase;
import com.lordmau5.wirelessutils.utils.Textures;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

public class ElementModeButton extends ElementDynamicButton {

    private final IWorkProvider provider;

    public ElementModeButton(GuiContainerCore gui, IWorkProvider provider, int posX, int posY) {
        super(gui, posX, posY, 16, 16);
        this.provider = provider;
    }

    @Override
    public void update() {
        super.update();

        IWorkProvider.IterationMode mode = provider.getIterationMode();
        if ( mode == IWorkProvider.IterationMode.ROUND_ROBIN ) {
            setIcon(Textures.ROUND_ROBIN);
        } else if ( mode == IWorkProvider.IterationMode.NEAREST_FIRST ) {
            setIcon(Textures.NEAREST_FIRST);
        } else if ( mode == IWorkProvider.IterationMode.FURTHEST_FIRST ) {
            setIcon(Textures.FURTHEST_FIRST);
        } else if ( mode == IWorkProvider.IterationMode.RANDOM ) {
            setIcon(Textures.RANDOM);
        }
    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);

        IWorkProvider.IterationMode mode = provider.getIterationMode();

        String key = "btn." + WirelessUtils.MODID + ".mode." + mode.toString();

        if ( !StringHelper.canLocalize(key + ".name") )
            return;

        list.add(StringHelper.localize(key + ".name"));
        addLocalizedLines(list, key + ".info");
    }

    void addLocalizedLines(@Nonnull List<String> tooltip, @Nonnull String name, Object... args) {
        int i = 0;
        String path = name + "." + i;
        while ( StringHelper.canLocalize(path) ) {
            tooltip.add(new TextComponentTranslation(path, args).setStyle(TextHelpers.GRAY).getFormattedText());
            i++;
            path = name + "." + i;
        }
    }

    @Override
    public void onClick() {
        provider.setIterationMode(provider.getIterationMode().next());
        if ( provider instanceof TileEntityBase )
            ((TileEntityBase) provider).sendModePacket();
    }

    @Override
    public void onRightClick() {
        GuiContainerCore.playClickSound(0.7F);
        provider.setIterationMode(provider.getIterationMode().previous());
        if ( provider instanceof TileEntityBase )
            ((TileEntityBase) provider).sendModePacket();
    }
}

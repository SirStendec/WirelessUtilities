package com.lordmau5.wirelessutils.gui.client.elements;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.utils.Textures;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;

import java.util.List;

public class TabLock extends TabButton {

    public static final int defaultSide = 0;

    public static final int LOCKED_COLOR = 0xd0230a;
    public static final int UNLOCKED_COLOR = 0x089e4c;

    private final BaseContainerItem container;

    public TabLock(BaseGuiContainer gui, BaseContainerItem container) {
        this(gui, defaultSide, container);
    }

    public TabLock(BaseGuiContainer gui, int side, BaseContainerItem container) {
        super(gui, side);
        this.container = container;
    }

    public void onClick(int mouseX, int mouseY, int mouseButton) {
        container.setLocked(!container.isLocked());
    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);
        final boolean locked = container.isLocked();

        if ( locked )
            TextHelpers.addLocalizedLines(list, "tab." + WirelessUtils.MODID + ".lock.locked", null);
        else
            TextHelpers.addLocalizedLines(list, "tab." + WirelessUtils.MODID + ".lock.unlocked", null);
    }

    @Override
    protected void drawForeground() {
        drawTabIcon(container.isLocked() ? Textures.LOCK : Textures.UNLOCK);
    }

    @Override
    public void update() {
        super.update();

        final boolean locked = container.isLocked();
        backgroundColor = locked ? LOCKED_COLOR : UNLOCKED_COLOR;
    }
}

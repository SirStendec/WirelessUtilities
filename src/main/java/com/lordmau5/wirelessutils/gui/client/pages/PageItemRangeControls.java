package com.lordmau5.wirelessutils.gui.client.pages;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.ElementRangeControls;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.items.ContainerAreaCard;

import javax.annotation.Nonnull;

public class PageItemRangeControls extends PageBase {

    private final ContainerAreaCard container;
    private final BaseGuiItem gui;

    //private ElementDynamicContainedButton btnMode;
    private ElementRangeControls rangeControls;
    //private ElementOffsetControls offsetControls;

    public PageItemRangeControls(@Nonnull BaseGuiItem gui, @Nonnull ContainerAreaCard container) {
        super(gui);
        this.gui = gui;
        this.container = container;

        setLabel("btn." + WirelessUtils.MODID + ".mode.range");

        rangeControls = new ElementRangeControls(gui, container, 160, 18);
        addElement(rangeControls);

        /*offsetControls = new ElementOffsetControls(gui, container, 160, 18);
        addElement(offsetControls);

        btnMode = new ElementDynamicContainedButton(this, "Mode", sizeX - (16 + 8), 69, 16, 16, Textures.SIZE);
        addElement(btnMode);*/
    }

    @Override
    public boolean wantsSlots() {
        return true;
    }

    /*@Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        switch (buttonName) {
            case "Mode":
                SharedState.offsetMode = !SharedState.offsetMode;
                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(1F);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        btnMode.setIcon(SharedState.offsetMode ? Textures.OFFSET : Textures.SIZE);
        btnMode.setToolTip("btn." + WirelessUtils.MODID + ".mode." + (SharedState.offsetMode ? "offset" : "range"));
        btnMode.setVisible(container.getRange() > 0);

        rangeControls.setVisible(!SharedState.offsetMode);
        offsetControls.setVisible(SharedState.offsetMode);
    }*/
}

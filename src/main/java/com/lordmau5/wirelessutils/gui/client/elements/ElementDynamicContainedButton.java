package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.List;

public class ElementDynamicContainedButton extends ElementDynamicButton {

    private final IContainsButtons container;
    private boolean managedClicks;

    private String tooltip;
    private boolean tooltipLocalized = false;

    public ElementDynamicContainedButton(IContainsButtons container, int posX, int posY, int sizeX, int sizeY, String label) {
        super(container.getGui(), posX, posY, sizeX, sizeY, label);
        this.container = container;

        setGuiManagedClicks(false);
    }

    public ElementDynamicContainedButton(IContainsButtons container, int posX, int posY, int sizeX, int sizeY, TextureAtlasSprite icon) {
        super(container.getGui(), posX, posY, sizeX, sizeY, icon);
        this.container = container;

        setGuiManagedClicks(false);
    }

    public ElementDynamicContainedButton(IContainsButtons container, String name, int posX, int posY, int sizeX, int sizeY, String label) {
        super(container.getGui(), posX, posY, sizeX, sizeY, label);
        this.container = container;

        setName(name);
        setGuiManagedClicks(true);
    }

    public ElementDynamicContainedButton(IContainsButtons container, String name, int posX, int posY, int sizeX, int sizeY, TextureAtlasSprite icon) {
        super(container.getGui(), posX, posY, sizeX, sizeY, icon);
        this.container = container;

        setName(name);
        setGuiManagedClicks(true);
    }

    public ElementDynamicContainedButton setGuiManagedClicks(boolean managed) {
        managedClicks = managed;
        return this;
    }

    public ElementDynamicContainedButton clearToolTip() {
        tooltip = null;
        return this;
    }

    public ElementDynamicContainedButton setToolTip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public ElementDynamicContainedButton setToolTipLocalized(boolean localized) {
        tooltipLocalized = localized;
        return this;
    }

    public ElementDynamicContainedButton setActive() {
        setEnabled(true);
        return this;
    }

    public ElementDynamicContainedButton setDisabled() {
        setEnabled(false);
        return this;
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( tooltip != null ) {
            if ( tooltipLocalized )
                list.add(tooltip);
            else
                list.add(StringHelper.localize(tooltip));
        }
    }

    @Override
    public void onClick() {

    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
        if ( !managedClicks )
            return super.onMousePressed(mouseX, mouseY, mouseButton);
        if ( isEnabled() ) {
            container.handleElementButtonClick(getName(), mouseButton);
            return true;
        }

        return false;
    }
}

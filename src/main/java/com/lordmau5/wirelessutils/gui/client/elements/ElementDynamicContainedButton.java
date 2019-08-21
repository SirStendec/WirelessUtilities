package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class ElementDynamicContainedButton extends ElementDynamicButton {

    private final IContainsButtons container;
    private boolean managedClicks;

    private String tooltip;
    private String tooltipExtra;
    private boolean tooltipLocalized = false;
    private boolean tooltipLines = false;

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

    public ElementDynamicContainedButton(IContainsButtons container, int posX, int posY, int sizeX, int sizeY, @Nonnull ItemStack item) {
        super(container.getGui(), posX, posY, sizeX, sizeY, item);
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

    public ElementDynamicContainedButton(IContainsButtons container, String name, int posX, int posY, int sizeX, int sizeY, @Nonnull ItemStack item) {
        super(container.getGui(), posX, posY, sizeX, sizeY, item);
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
        tooltipExtra = null;
        return this;
    }

    public ElementDynamicContainedButton setToolTip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public ElementDynamicContainedButton setToolTipExtra(String extra) {
        this.tooltipExtra = extra;
        return this;
    }

    public ElementDynamicContainedButton setToolTipLocalized(boolean localized) {
        tooltipLocalized = localized;
        return this;
    }

    public ElementDynamicContainedButton setToolTipLines(String tooltip) {
        this.tooltip = tooltip;
        tooltipLines = true;
        return this;
    }

    public ElementDynamicContainedButton setToolTipLines(boolean lines) {
        tooltipLines = lines;
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
            if ( tooltipLines ) {
                String[] lines = TextHelpers.getLocalizedLines(tooltip);
                if ( lines != null )
                    Collections.addAll(list, lines);

                /*int i = 0;
                int blank = 0;
                String path = tooltip + "." + i;
                while ( StringHelper.canLocalize(path) ) {
                    String tip = StringHelper.localize(path);
                    if ( tip.isEmpty() )
                        blank++;
                    else {
                        while ( blank > 0 ) {
                            list.add("");
                            blank--;
                        }

                        list.add(tip);
                    }

                    i++;
                    path = tooltip + "." + i;
                }*/
            } else if ( tooltipLocalized ) {
                list.add(tooltip);
            } else
                list.add(StringHelper.localize(tooltip));
        }

        if ( tooltipExtra != null ) {
            String[] lines = TextHelpers.getLocalizedLines(tooltipExtra);
            if ( lines != null )
                Collections.addAll(list, lines);
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

package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.element.ElementButton;

import java.util.Collection;
import java.util.List;

public class ElementContainedButton extends ElementButton {

    private final IContainsButtons container;
    private boolean managedClicks;

    private Collection<String> tooltip;

    public ElementContainedButton(IContainsButtons container, int posX, int posY, int sizeX, int sizeY, int sheetX, int sheetY, int hoverX, int hoverY, String texture) {
        super(container.getGui(), posX, posY, sizeX, sizeY, sheetX, sheetY, hoverX, hoverY, texture);
        this.container = container;
    }

    public ElementContainedButton(IContainsButtons container, int posX, int posY, int sizeX, int sizeY, int sheetX, int sheetY, int hoverX, int hoverY, int disabledX, int disabledY, String texture) {
        super(container.getGui(), posX, posY, sizeX, sizeY, sheetX, sheetY, hoverX, hoverY, disabledX, disabledY, texture);
        this.container = container;
    }

    public ElementContainedButton(IContainsButtons container, int posX, int posY, String name, int sheetX, int sheetY, int hoverX, int hoverY, int sizeX, int sizeY, String texture) {
        super(container.getGui(), posX, posY, name, sheetX, sheetY, hoverX, hoverY, sizeX, sizeY, texture);
        this.container = container;
    }

    public ElementContainedButton(IContainsButtons container, int posX, int posY, String name, int sheetX, int sheetY, int hoverX, int hoverY, int disabledX, int disabledY, int sizeX, int sizeY, String texture) {
        super(container.getGui(), posX, posY, name, sheetX, sheetY, hoverX, hoverY, disabledX, disabledY, sizeX, sizeY, texture);
        this.container = container;
    }

    @Override
    public ElementContainedButton setGuiManagedClicks(boolean managed) {
        managedClicks = managed;
        return this;
    }

    @Override
    public boolean onMousePressed(int x, int y, int mouseButton) {
        if ( !managedClicks )
            return super.onMousePressed(x, y, mouseButton);
        if ( isEnabled() ) {
            container.handleElementButtonClick(getName(), mouseButton);
            return true;
        }

        return false;
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( tooltip != null )
            list.addAll(tooltip);

        super.addTooltip(list);
    }

    public void setTooltipList(Collection<String> list) {
        tooltip = list;
    }
}

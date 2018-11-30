package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ElementContainer extends ElementBase implements IContainsButtons {

    private final ArrayList<ElementBase> elements = new ArrayList<>();

    public ElementContainer(GuiContainerCore gui, int posX, int posY) {
        super(gui, posX, posY);
    }

    public ElementContainer(GuiContainerCore gui, int posX, int posY, int width, int height) {
        super(gui, posX, posY, width, height);
    }

    public GuiContainerCore getGui() {
        return gui;
    }

    /* Basic Updates */

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        updateElementInformation();
        if ( !isVisible() || !isEnabled() )
            return;

        mouseX -= posX;
        mouseY -= posY;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0F);

        for (ElementBase element : elements)
            if ( element.isVisible() )
                element.drawBackground(mouseX, mouseY, gameTicks);

        GlStateManager.popMatrix();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        mouseX -= posX;
        mouseY -= posY;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0F);

        for (ElementBase element : elements)
            if ( element.isVisible() )
                element.drawForeground(mouseX, mouseY);

        GlStateManager.popMatrix();
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        mouseX -= posX;
        mouseY -= posY;

        for (ElementBase element : elements)
            if ( element.isVisible() && element.isEnabled() )
                element.update(mouseX, mouseY);
    }

    @Override
    public void addTooltip(List<String> list) {
        int mouseX = gui.getMouseX() - posX;
        int mouseY = gui.getMouseY() - posY;

        for (ElementBase element : elements)
            if ( element.isVisible() && element.intersectsWith(mouseX, mouseY) )
                element.addTooltip(list);
    }

    /* Elements */

    public ElementBase addElement(ElementBase element) {
        elements.add(element);
        return element;
    }

    protected ElementBase getElementAtPosition(int mouseX, int mouseY) {
        for (ElementBase element : elements)
            if ( element.isVisible() && element.intersectsWith(mouseX, mouseY) )
                return element;

        return null;
    }

    public void handleElementButtonClick(String buttonName, int mouseButton) {

    }

    public void updateElementInformation() {

    }

    /* Event Handlers */

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        if ( !isVisible() || !isEnabled() )
            return false;

        mouseX -= posX;
        mouseY -= posY;

        for (ElementBase element : elements) {
            if ( !element.isVisible() || !element.isEnabled() || !element.intersectsWith(mouseX, mouseY) )
                continue;
            if ( element.onMousePressed(mouseX, mouseY, mouseButton) )
                return true;
        }

        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY) {
        if ( !isVisible() || !isEnabled() )
            return;

        mouseX -= posX;
        mouseY -= posY;

        for (ElementBase element : elements) {
            if ( !element.isVisible() || !element.isEnabled() )
                continue;
            element.onMouseReleased(mouseX, mouseY);
        }
    }

    @Override
    public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
        if ( !isVisible() || !isEnabled() )
            return false;

        mouseX -= posX;
        mouseY -= posY;

        for (ElementBase element : elements) {
            if ( !element.isVisible() || !element.isEnabled() || !element.intersectsWith(mouseX, mouseY) )
                continue;
            if ( element.onMouseWheel(mouseX, mouseY, movement) )
                return true;
        }

        return false;
    }

    @Override
    public boolean onKeyTyped(char characterTyped, int keyPressed) {
        if ( !isVisible() || !isEnabled() )
            return false;

        for (ElementBase element : elements) {
            if ( !element.isEnabled() || !element.isVisible() )
                continue;

            if ( element.onKeyTyped(characterTyped, keyPressed) )
                return true;
        }

        return super.onKeyTyped(characterTyped, keyPressed);
    }
}

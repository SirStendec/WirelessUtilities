package com.lordmau5.wirelessutils.gui.client.pages;

import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.ElementTextFieldLimited;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.item.GuiAdminAugment;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.items.ContainerAdminAugment;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class PageItemColor extends PageBase {

    private final ContainerAdminAugment container;
    private final BaseGuiContainer gui;

    private final ElementDynamicContainedButton btnEffect;
    private final ElementTextField txtLayer0;
    private final ElementTextField txtLayer1;
    private final ElementTextField txtLayer2;

    public PageItemColor(GuiAdminAugment gui) {
        super(gui);
        this.gui = gui;
        this.container = gui.getContainer();

        setLabel("Color");

        txtLayer0 = new ElementTextFieldLimited(gui, sizeX - 83, 32, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.removeTag("WUTint:0");
                    else
                        container.setInteger("WUTint:0", Integer.parseInt(text, 16));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(GuiAdminAugment.HEX_CHARACTERS, true);

        txtLayer1 = new ElementTextFieldLimited(gui, sizeX - 83, 47, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.removeTag("WUTint:1");
                    else
                        container.setInteger("WUTint:1", Integer.parseInt(text, 16));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(GuiAdminAugment.HEX_CHARACTERS, true);

        txtLayer2 = new ElementTextFieldLimited(gui, sizeX - 83, 62, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.removeTag("WUTint:2");
                    else
                        container.setInteger("WUTint:2", Integer.parseInt(text, 16));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(GuiAdminAugment.HEX_CHARACTERS, true);

        btnEffect = new ElementDynamicContainedButton(this, "Effect", sizeX - 84, 77, 77, 16, "");

        addElement(txtLayer0);
        addElement(txtLayer1);
        addElement(txtLayer2);
        addElement(btnEffect);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        final boolean locked = container.isLocked();
        txtLayer0.setEnabled(!locked);
        txtLayer1.setEnabled(!locked);
        txtLayer2.setEnabled(!locked);
        btnEffect.setEnabled(!locked);

        if ( !txtLayer0.isFocused() )
            txtLayer0.setText(container.hasTag("WUTint:0") ? Integer.toHexString(container.getInteger("WUTint:0", 0)) : "");

        if ( !txtLayer1.isFocused() )
            txtLayer1.setText(container.hasTag("WUTint:1") ? Integer.toHexString(container.getInteger("WUTint:1", 0)) : "");

        if ( !txtLayer2.isFocused() )
            txtLayer2.setText(container.hasTag("WUTint:2") ? Integer.toHexString(container.getInteger("WUTint:2", 0)) : "");

        btnEffect.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + getEffect()));
    }

    public int getEffect() {
        if ( !container.hasTag("ForceEffect") )
            return 0;

        return container.getBoolean("ForceEffect", false) ? 2 : 1;
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "Effect":
                int idx = getEffect() + amount;
                if ( idx < 0 )
                    idx = 2;
                else if ( idx > 2 )
                    idx = 0;

                if ( idx == 0 )
                    container.removeTag("ForceEffect");
                else
                    container.setBoolean("ForceEffect", idx == 2);

                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        gui.drawRightAlignedText("Layer 0:", sizeX - 91, 32, container.hasTag("WUTint:0") ? 0x008000 : 0x404040);
        gui.drawRightAlignedText("Layer 1:", sizeX - 91, 47, container.hasTag("WUTint:1") ? 0x008000 : 0x404040);
        gui.drawRightAlignedText("Layer 2:", sizeX - 91, 62, container.hasTag("WUTint:2") ? 0x008000 : 0x404040);
        gui.drawRightAlignedText("Effect:", sizeX - 91, 80, getEffect() == 0 ? 0x404040 : 0x008000);

        RenderHelper.enableGUIStandardItemLighting();
        gui.drawItemStack(container.getItemStack(), (sizeX - 16) / 2, 8, true, "");
        GlStateManager.disableLighting();
    }
}

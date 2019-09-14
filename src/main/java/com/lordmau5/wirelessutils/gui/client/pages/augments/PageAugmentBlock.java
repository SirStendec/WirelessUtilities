package com.lordmau5.wirelessutils.gui.client.pages.augments;

import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.ElementTextFieldLimited;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.item.GuiAdminAugment;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.items.ContainerAdminAugment;

public class PageAugmentBlock extends PageBase {

    private final ContainerAdminAugment container;
    private final BaseGuiContainer gui;

    private ElementDynamicContainedButton btnSilkTouch;
    private ElementTextField txtFortune;

    public PageAugmentBlock(GuiAdminAugment gui) {
        super(gui);
        this.gui = gui;
        this.container = gui.getContainer();

        setLabel("Specific");

        txtFortune = new ElementTextFieldLimited(gui, sizeX - 83, 8, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.removeTag("Fortune");
                    else
                        container.setByte("Fortune", (byte) Integer.parseInt(text));

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
        }.setFilter(GuiAdminAugment.INT_CHARACTERS, true);

        btnSilkTouch = new ElementDynamicContainedButton(this, "SilkTouch", sizeX - 84, 23, 77, 16, "");

        addElement(txtFortune);
        addElement(btnSilkTouch);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        final boolean locked = container.isLocked();
        txtFortune.setEnabled(!locked);
        btnSilkTouch.setEnabled(!locked);

        if ( !txtFortune.isFocused() )
            txtFortune.setText(container.hasTag("Fortune") ? String.valueOf(container.getByte("Fortune", (byte) 0)) : "");

        btnSilkTouch.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + getSilkTouch()));
    }

    public int getSilkTouch() {
        if ( !container.hasTag("SilkTouch") )
            return 0;

        return container.getBoolean("SilkTouch", false) ? 2 : 1;
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "SilkTouch":
                int idx = getSilkTouch() + amount;
                if ( idx < 0 )
                    idx = 2;
                else if ( idx > 2 )
                    idx = 0;

                if ( idx == 0 )
                    container.removeTag("SilkTouch");
                else
                    container.setBoolean("SilkTouch", idx == 2);

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

        gui.drawRightAlignedText("Fortune:", sizeX - 91, 9, container.hasTag("Fortune") ? 0x008000 : 0x404040);
        gui.drawRightAlignedText("Silk Touch:", sizeX - 91, 27, container.hasTag("SilkTouch") ? 0x008000 : 0x404040);
    }
}

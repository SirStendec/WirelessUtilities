package com.lordmau5.wirelessutils.gui.client.pages;

import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.ElementTextFieldLimited;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.gui.container.items.IRelativeCardConfig;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class PageItemRelativeControls extends PageBase {

    private static final String VALID_CHARACTERS = "-1234567890";

    private final BaseContainerItem container;
    private final IRelativeCardConfig config;

    private ElementTextField txtX;
    private ElementTextField txtY;
    private ElementTextField txtZ;

    private ElementDynamicContainedButton btnClear;
    private ElementDynamicContainedButton btnFacing;

    public PageItemRelativeControls(BaseGuiContainer gui, BaseContainerItem container, IRelativeCardConfig config) {
        super(gui);
        this.container = container;
        this.config = config;

        setLabel("btn." + WirelessUtils.MODID + ".mode.offset");

        txtX = new ElementTextFieldLimited(gui, sizeX - 108, 18, 100, 10, (short) 10) {
            boolean changed = false;

            void updateValue() {
                if ( !changed )
                    return;

                changed = false;
                try {
                    config.setX(Integer.parseInt(getText()));
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
            public ElementTextField setFocused(boolean focused) {
                super.setFocused(focused);
                if ( !isFocused() && changed )
                    updateValue();
                return this;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }

            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                if ( isFocused() )
                    setFocused(false);

                config.setX(config.getX() + (movement > 0 ? 1 : -1));
                return true;
            }
        }.setFilter(VALID_CHARACTERS, true);

        txtY = new ElementTextFieldLimited(gui, sizeX - 108, 33, 100, 10, (short) 10) {
            boolean changed = false;

            void updateValue() {
                if ( !changed )
                    return;

                changed = false;
                try {
                    config.setY(Integer.parseInt(getText()));
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
            public ElementTextField setFocused(boolean focused) {
                super.setFocused(focused);
                if ( !isFocused() && changed )
                    updateValue();
                return this;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }

            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                if ( isFocused() )
                    setFocused(false);

                config.setY(config.getY() + (movement > 0 ? 1 : -1));
                return true;
            }
        }.setFilter(VALID_CHARACTERS, true);

        txtZ = new ElementTextFieldLimited(gui, sizeX - 108, 48, 100, 10, (short) 10) {
            boolean changed = false;

            void updateValue() {
                if ( !changed )
                    return;

                changed = false;
                try {
                    config.setZ(Integer.parseInt(getText()));
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
            public ElementTextField setFocused(boolean focused) {
                super.setFocused(focused);
                if ( !isFocused() && changed )
                    updateValue();
                return this;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }

            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                if ( isFocused() )
                    setFocused(false);

                config.setZ(config.getZ() + (movement > 0 ? 1 : -1));
                return true;
            }
        }.setFilter(VALID_CHARACTERS, true);

        btnFacing = new ElementDynamicContainedButton(this, "Facing", sizeX - 109, 63, 102, 16, "");
        btnClear = new ElementDynamicContainedButton(this, "Clear", 30, 63, sizeX - 139, 16, StringHelper.localize("btn." + WirelessUtils.MODID + ".clear"));

        btnClear.setToolTipLines("btn." + WirelessUtils.MODID + ".clear.info");

        txtX.setText(Integer.toString(config.getX()));
        txtY.setText(Integer.toString(config.getY()));
        txtZ.setText(Integer.toString(config.getZ()));

        addElement(txtX);
        addElement(txtY);
        addElement(txtZ);

        addElement(btnFacing);
        addElement(btnClear);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        txtX.setFocused(false);
        txtY.setFocused(false);
        txtZ.setFocused(false);
    }

    @Override
    public void onPageFocusLost() {
        super.onPageFocusLost();

        txtX.setFocused(false);
        txtY.setFocused(false);
        txtZ.setFocused(false);
    }

    @Override
    public boolean wantsSlots() {
        return true;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        FontRenderer fontRenderer = getFontRenderer();

        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".x"), 30, 19, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".y"), 30, 34, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".z"), 30, 49, 0x404040);

        Vec3d vector = config.getVector();
        if ( vector == null )
            return;

        final String range = new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".relative_positional_card.distance",
                new TextComponentString(StringHelper.formatNumber((int) Math.floor(vector.length()))).setStyle(TextHelpers.BLACK)
        ).getFormattedText();

        getGui().drawRightAlignedText(range, sizeX - 6, sizeY - 96 + 3, 0x404040);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        final boolean locked = container.isLocked();

        btnClear.setVisible(container.canClearItemStack());

        txtX.setEnabled(!locked);
        txtY.setEnabled(!locked);
        txtZ.setEnabled(!locked);
        btnFacing.setEnabled(!locked);
        btnClear.setEnabled(!locked && StringHelper.isControlKeyDown());

        if ( !txtX.isFocused() )
            txtX.setText(Integer.toString(config.getX()));

        if ( !txtY.isFocused() )
            txtY.setText(Integer.toString(config.getY()));

        if ( !txtZ.isFocused() )
            txtZ.setText(Integer.toString(config.getZ()));

        EnumFacing facing = config.getFacing();
        btnFacing.setText(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".blockpos.side",
                TextHelpers.getComponent(facing == null ? null : facing.getName())
        ).getFormattedText());
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "Facing":
                EnumFacing facing = config.getFacing();
                int index = facing == null ? -1 : facing.ordinal();
                index += amount;

                if ( config.allowNullFacing() ) {
                    if ( index < -1 )
                        index = EnumFacing.VALUES.length - 1;
                    else if ( index >= EnumFacing.VALUES.length )
                        index = -1;

                    facing = index == -1 ? null : EnumFacing.byIndex(index);
                } else
                    facing = EnumFacing.byIndex(index);

                if ( !config.setFacing(facing) )
                    return;

                break;
            case "Clear":
                if ( mouseButton == 0 && StringHelper.isControlKeyDown() ) {
                    if ( container.clearItemStack() )
                        Minecraft.getMinecraft().player.closeScreen();
                    break;
                }
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
    }
}

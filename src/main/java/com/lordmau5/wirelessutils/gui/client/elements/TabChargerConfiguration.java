package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.init.CoreTextures;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.charger.TileEntityBaseCharger;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.List;

public class TabChargerConfiguration extends TabBase implements IContainsButtons {

    public static ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public static int defaultSide = 0;
    public static int defaultHeaderColor = 0xE1C92F;
    public static int defaultSubHeaderColor = 0xAAAFB8;
    public static int defaultTextColor = 0x000000;
    public static int defaultBackgroundColor = 0x226688;

    private final TileEntityBaseCharger charger;

    static final String UNIT_INSTANT = "RF/t";

    private boolean editMode = false;

    private ElementContainedButton decButton;
    private ElementContainedButton incButton;
    private ElementContainedButton editButton;
    private ElementContainedButton saveButton;

    private ElementTextField editor;

    public TabChargerConfiguration(GuiContainerCore gui, TileEntityBaseCharger container) {
        this(gui, defaultSide, container);
    }

    public TabChargerConfiguration(GuiContainerCore gui, int side, TileEntityBaseCharger charger) {
        super(gui, side);
        this.charger = charger;

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        maxHeight = 76;
        maxWidth = 100;

        decButton = new ElementContainedButton(this, 25, 52, "DecTrans", 176, 42, 176, 56, 176, 70, 14, 14, TEXTURE.toString());
        incButton = new ElementContainedButton(this, 61, 52, "IncTrans", 190, 42, 190, 56, 190, 70, 14, 14, TEXTURE.toString());
        editButton = new ElementContainedButton(this, 43, 52, "Edit", 176, 84, 176, 98, 176, 112, 14, 14, TEXTURE.toString());
        saveButton = new ElementContainedButton(this, sideOffset() + 6 + (maxWidth - 32), 34, "Save", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());

        editor = new ElementTextField(gui, sideOffset() + 6, 35, maxWidth - 34, 12) {
            @Override
            public boolean isAllowedCharacter(char charTyped) {
                return charTyped >= '0' && charTyped <= '9';
            }

            @Override
            protected boolean onEnter() {
                setEditMode(false);
                return false;
            }
        };

        editor.setTextColor(0, 0xFFFFFF);

        addElement(decButton);
        addElement(incButton);
        addElement(editButton.setToolTip("btn." + WirelessUtils.MODID + ".edit"));
        addElement(saveButton.setToolTip("btn." + WirelessUtils.MODID + ".save"));
        addElement(editor);
    }

    public GuiContainerCore getGui() {
        return gui;
    }

    public void handleElementButtonClick(String name, int button) {
        boolean increment = false;

        switch (name) {
            case "IncTrans":
                increment = true;
            case "DecTrans":
                break;
            case "Edit":
                GuiContainerCore.playClickSound(1F);
                setEditMode(true);
                return;
            case "Save":
                GuiContainerCore.playClickSound(1F);
                setEditMode(false);
                return;
            default:
                return;
        }

        Tuple<Long, Long> rates = getRates();
        long value = button == 0 ? rates.getFirst() : rates.getSecond();
        if ( !increment )
            value = -value;

        long newEnergy = charger.getFullMaxEnergyPerTick() + value;
        if ( newEnergy >= charger.getMaxPossibleTransfer() ) {
            newEnergy = -1;
        } else if ( newEnergy < 0 ) {
            if ( value < 0 )
                newEnergy = 0;
            else
                newEnergy = -1;
        }

        GuiContainerCore.playClickSound(increment ? 1F : 0.7F);
        charger.setTransferLimit(newEnergy);
        charger.sendModePacket();
    }

    public Tuple<Long, Long> getRates() {
        long change;
        long change2;

        if ( GuiScreen.isShiftKeyDown() ) {
            change = 1000;
            change2 = 100;

            if ( GuiScreen.isCtrlKeyDown() ) {
                change *= 10;
                change2 *= 10;

                if ( GuiScreen.isAltKeyDown() ) {
                    change *= 10;
                    change2 *= 10;
                }
            }
        } else if ( GuiScreen.isCtrlKeyDown() ) {
            change = 5;
            change2 = 1;
        } else if ( GuiScreen.isAltKeyDown() ) {
            change = charger.getMaxPossibleTransfer();
            change2 = Math.floorDiv(change, 10);
        } else {
            change = 50;
            change2 = 10;
        }

        return new Tuple<Long, Long>(change, change2);
    }

    @Override
    protected void updateElements() {
        super.updateElements();

        if ( !isFullyOpened() )
            return;

        decButton.setVisible(!editMode);
        incButton.setVisible(!editMode);
        editButton.setVisible(!editMode);
        editor.setVisible(editMode);
        saveButton.setVisible(editMode);

        Tuple<Long, Long> rates = getRates();

        long maxEnergy = charger.getFullMaxEnergyPerTick();
        long maxPossible = charger.getMaxPossibleTransfer();

        if ( maxEnergy > 0 )
            decButton.setToolTipLocalized(getTooltip(false, rates.getFirst(), rates.getSecond())).setActive();
        else
            decButton.clearToolTip().setDisabled();

        if ( maxEnergy < maxPossible )
            incButton.setToolTipLocalized(getTooltip(true, rates.getFirst(), rates.getSecond())).setActive();
        else
            incButton.clearToolTip().setDisabled();
    }

    protected String getTooltip(boolean increment, long change, long change2) {
        return new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + "." + (increment ? "inc" : "dec") + "_area.one",
                StringHelper.formatNumber(change) + " / " + StringHelper.formatNumber(change2)
        ).getFormattedText();
    }

    public void setEditMode(boolean enabled) {
        if ( enabled == editMode )
            return;

        editMode = enabled;

        if ( enabled ) {
            editor.setMaxLength((short) ("10" + charger.getMaxPossibleTransfer()).length());
            long limit = charger.getTransferLimit();
            if ( limit == -1 )
                limit = charger.getMaxPossibleTransfer();

            editor.setText("" + limit);
            editor.setFocused(true);

        } else {
            try {
                charger.setTransferLimit(Long.parseLong(editor.getText()));
                charger.sendModePacket();
            } catch (NumberFormatException ex) {
                System.out.println("Number Format Error: " + ex);
            }
        }
    }

    @Override
    protected void drawBackground() {
        super.drawBackground();

        if ( !isFullyOpened() || editMode )
            return;

        float colorR = (backgroundColor >> 16 & 255) / 255.0F * 0.6F;
        float colorG = (backgroundColor >> 8 & 255) / 255.0F * 0.6F;
        float colorB = (backgroundColor & 255) / 255.0F * 0.6F;
        GlStateManager.color(colorR, colorG, colorB, 1.0F);
        gui.drawTexturedModalRect(21, 48, 16, 20, 58, 22);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean shouldStayOpen = super.onMousePressed(mouseX, mouseY, mouseButton);

        mouseX -= this.posX();
        mouseY -= this.posY;

        if ( mouseX >= 21 && mouseX < 79 && mouseY >= 48 && mouseY < 70 )
            return true;

        return shouldStayOpen;
    }

    protected String formatNumber(long value, String postfix) {
        if ( value < 1000000000 || GuiScreen.isShiftKeyDown() )
            return String.format("%s %s", StringHelper.formatNumber(value), postfix);

        return TextHelpers.getScaledNumber(value, postfix, true);
    }

    @Override
    protected void drawForeground() {
        drawTabIcon(CoreTextures.ICON_CONFIG);
        if ( !isFullyOpened() )
            return;

        FontRenderer fontRenderer = getFontRenderer();

        fontRenderer.drawStringWithShadow(StringHelper.localize("info.cofh.configuration"), sideOffset() + 20, 6, headerColor);
        fontRenderer.drawStringWithShadow(StringHelper.localize("info.cofh.energyMax") + ":", sideOffset() + 6, 22, subheaderColor);
        if ( editMode ) {
            fontRenderer.drawString(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".max",
                    formatNumber(charger.getMaxPossibleTransfer(), UNIT_INSTANT)
            ).getUnformattedText(), sideOffset() + 6, 56, textColor);

        } else {
            fontRenderer.drawString(formatNumber(charger.getFullMaxEnergyPerTick(), UNIT_INSTANT), sideOffset() + 14, 34, textColor);
        }
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !isFullyOpened() ) {
            list.add(StringHelper.localize("info.cofh.configuration"));
            return;
        }

        int mouseX = gui.getMouseX() - posX();
        int mouseY = gui.getMouseY() - posY;

        for (int i = 0; i < this.elements.size(); i++) {
            ElementBase c = elements.get(i);
            if ( !c.isVisible() || !c.isEnabled() || !c.intersectsWith(mouseX, mouseY) )
                continue;

            c.addTooltip(list);
        }
    }
}

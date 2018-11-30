package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementRangeControls extends ElementContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public static final String INTL_KEY = "btn." + WirelessUtils.MODID + ".range.";

    private final BaseGuiContainer gui;
    private final TileEntityBaseMachine machine;

    private final ElementContainedButton decHeight;
    private final ElementContainedButton incHeight;

    private final ElementContainedButton decLength;
    private final ElementContainedButton incLength;

    private final ElementContainedButton decWidth;
    private final ElementContainedButton incWidth;

    public ElementRangeControls(BaseGuiContainer gui, TileEntityBaseMachine machine, int posX, int posY) {
        super(gui, posX, posY, 30, 46);
        this.gui = gui;
        this.machine = machine;

        decHeight = new ElementContainedButton(this, 0, 0, "DecHeight", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incHeight = new ElementContainedButton(this, 16, 0, "IncHeight", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        decLength = new ElementContainedButton(this, 0, 16, "DecLength", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incLength = new ElementContainedButton(this, 16, 16, "IncLength", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        decWidth = new ElementContainedButton(this, 0, 32, "DecWidth", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incWidth = new ElementContainedButton(this, 16, 32, "IncWidth", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        addElement(decHeight);
        addElement(incHeight);

        addElement(decLength);
        addElement(incLength);

        addElement(decWidth);
        addElement(incWidth);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        if ( !(machine instanceof IDirectionalMachine) )
            return;

        IDirectionalMachine dir = (IDirectionalMachine) machine;

        drawRange(dir.getRangeHeight(), posX - 22, posY + 4, 0);
        drawRange(dir.getRangeLength(), posX - 22, posY + 20, 0);
        drawRange(dir.getRangeWidth(), posX - 22, posY + 36, 0);

        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "height"), posX - 28, posY + 4, 0x404040);
        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "length"), posX - 28, posY + 20, 0x404040);
        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "width"), posX - 28, posY + 36, 0x404040);
    }

    protected void drawRange(int range, int x, int y, int color) {
        gui.getFontRenderer().drawString(StringHelper.formatNumber((range * 2) + 1), x, y, color);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        IDirectionalMachine dir = machine instanceof IDirectionalMachine ? (IDirectionalMachine) machine : null;
        int range = dir == null ? 0 : dir.getRange();
        setVisible(range != 0);
        if ( range == 0 )
            return;

        int rangeHeight = dir.getRangeHeight();
        int rangeLength = dir.getRangeLength();
        int rangeWidth = dir.getRangeWidth();
        int remaining = range - (rangeHeight + rangeLength + rangeWidth);

        boolean isMulti = GuiScreen.isCtrlKeyDown();
        boolean isAll = GuiScreen.isShiftKeyDown();

        if ( isAll ) {
            int minimum = Math.min(Math.min(rangeHeight, rangeLength), rangeWidth);

            boolean decEnabled = minimum > 0;
            boolean incEnabled = remaining >= 3;

            String decTooltip = decEnabled ? generateTooltip(false, true, isMulti ? minimum : 1) : null;
            String incTooltip = incEnabled ? generateTooltip(true, true, isMulti ? Math.floorDiv(remaining, 3) : 1) : null;

            decHeight.setToolTipLocalized(decTooltip).setEnabled(decEnabled);
            decLength.setToolTipLocalized(decTooltip).setEnabled(decEnabled);
            decWidth.setToolTipLocalized(decTooltip).setEnabled(decEnabled);

            incHeight.setToolTipLocalized(incTooltip).setEnabled(incEnabled);
            incLength.setToolTipLocalized(incTooltip).setEnabled(incEnabled);
            incWidth.setToolTipLocalized(incTooltip).setEnabled(incEnabled);

        } else {
            updateButton(decHeight, rangeHeight, false, isMulti);
            updateButton(incHeight, remaining, true, isMulti);

            updateButton(decLength, rangeLength, false, isMulti);
            updateButton(incLength, remaining, true, isMulti);

            updateButton(decWidth, rangeWidth, false, isMulti);
            updateButton(incWidth, remaining, true, isMulti);
        }
    }

    public void updateButton(ElementContainedButton button, int value, boolean increment, boolean isMulti) {
        boolean enabled = value > 0;
        String tooltip = enabled ? generateTooltip(increment, false, isMulti ? value : 1) : null;
        button.setToolTipLocalized(tooltip).setEnabled(enabled);
    }

    public String generateTooltip(boolean increment, boolean all, int amount) {
        return new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + (increment ? ".inc_area." : ".dec_area.") + (all ? "all" : "one"),
                (amount * 2)
        ).getFormattedText();
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = 0.7F;

        if ( !(machine instanceof IDirectionalMachine) )
            return;

        IDirectionalMachine dir = (IDirectionalMachine) machine;
        int amount = 1;

        if ( GuiScreen.isShiftKeyDown() ) {
            int rangeHeight = dir.getRangeHeight();
            int rangeLength = dir.getRangeLength();
            int rangeWidth = dir.getRangeWidth();
            int remaining = dir.getRange() - (rangeHeight + rangeLength + rangeWidth);

            if ( GuiScreen.isCtrlKeyDown() ) {
                amount = Math.floorDiv(remaining, 3);
            }

            switch (buttonName) {
                case "DecHeight":
                case "DecLength":
                case "DecWidth":
                    amount = -1;
                    if ( GuiScreen.isCtrlKeyDown() )
                        amount = -(Math.min(Math.min(rangeHeight, rangeLength), rangeWidth));

                case "IncHeight":
                case "IncLength":
                case "IncWidth":
                    dir.setRanges(dir.getRangeHeight() + amount, dir.getRangeLength() + amount, dir.getRangeWidth() + amount);
                    break;
            }

        } else {
            if ( GuiScreen.isCtrlKeyDown() )
                amount = dir.getRange();

            switch (buttonName) {
                case "DecHeight":
                    dir.setRangeHeight(dir.getRangeHeight() - amount);
                    break;

                case "IncHeight":
                    dir.setRangeHeight(dir.getRangeHeight() + amount);
                    break;

                case "DecLength":
                    dir.setRangeLength(dir.getRangeLength() - amount);
                    break;

                case "IncLength":
                    dir.setRangeLength(dir.getRangeLength() + amount);
                    break;

                case "DecWidth":
                    dir.setRangeWidth(dir.getRangeWidth() - amount);
                    break;

                case "IncWidth":
                    dir.setRangeWidth(dir.getRangeWidth() + amount);
                    break;
            }
        }

        BaseGuiContainer.playClickSound(pitch);
        machine.sendModePacket();
    }
}

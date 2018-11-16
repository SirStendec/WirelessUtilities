package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementOffsetControls extends ElementContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public static final String INTL_KEY = "btn." + WirelessUtils.MODID + ".offset.";

    private final BaseGuiContainer gui;
    private final TileEntityBaseMachine machine;

    private final ElementContainedButton decVert;
    private final ElementContainedButton incVert;

    private final ElementContainedButton decHoriz;
    private final ElementContainedButton incHoriz;


    public ElementOffsetControls(BaseGuiContainer gui, TileEntityBaseMachine machine, int posX, int posY) {
        super(gui, posX, posY, 30, 30);
        this.gui = gui;
        this.machine = machine;

        if ( !(machine instanceof IDirectionalMachine) ) {
            setEnabled(false);
            setVisible(false);
        }

        decVert = new ElementContainedButton(this, 0, 0, "DecVert", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incVert = new ElementContainedButton(this, 16, 0, "IncVert", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        decHoriz = new ElementContainedButton(this, 0, 16, "DecHoriz", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incHoriz = new ElementContainedButton(this, 16, 16, "IncHoriz", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        addElement(decVert);
        addElement(incVert);

        addElement(decHoriz);
        addElement(incHoriz);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        if ( !(machine instanceof IDirectionalMachine) )
            return;

        IDirectionalMachine dir = (IDirectionalMachine) machine;
        if ( dir.getRange() == 0 )
            return;

        FontRenderer fontRenderer = gui.getFontRenderer();

        fontRenderer.drawString(StringHelper.formatNumber(dir.getOffsetVertical()), posX - 22, posY + 4, 0);
        fontRenderer.drawString(StringHelper.formatNumber(dir.getOffsetHorizontal()), posX - 22, posY + 20, 0);

        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "vertical"), posX - 28, posY + 4, 0x404040);
        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "horizontal"), posX - 28, posY + 20, 0x404040);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        IDirectionalMachine dir = machine instanceof IDirectionalMachine ? (IDirectionalMachine) machine : null;
        int range = dir == null ? 0 : dir.getRange();
        setVisible(range != 0);
        if ( range == 0 )
            return;

        boolean facingY = dir.getEnumFacing().getAxis() == EnumFacing.Axis.Y;
        int rangeHorizontal = dir.getRangeWidth();
        int rangeVertical = facingY ? dir.getRangeLength() : dir.getRangeHeight();

        boolean isMulti = GuiScreen.isCtrlKeyDown();

        updateButton(decHoriz, rangeHorizontal, dir.getOffsetHorizontal(), false, isMulti);
        updateButton(incHoriz, rangeHorizontal, dir.getOffsetHorizontal(), true, isMulti);

        updateButton(decVert, rangeVertical, dir.getOffsetVertical(), false, isMulti);
        updateButton(incVert, rangeVertical, dir.getOffsetVertical(), true, isMulti);
    }

    public void updateButton(ElementContainedButton button, int max, int value, boolean increment, boolean isMulti) {
        boolean enabled = increment ? (value < max) : (value > -max);
        String tooltip = enabled ? generateTooltip(increment, isMulti ? (increment ? max - value : max + value) : 1) : null;
        button.setToolTipLocalized(tooltip).setEnabled(enabled);
    }

    public String generateTooltip(boolean increment, int amount) {
        return new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + (increment ? ".inc_area.one" : ".dec_area.one"),
                amount
        ).getFormattedText();
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = 0.7F;

        if ( !(machine instanceof IDirectionalMachine) )
            return;

        IDirectionalMachine dir = (IDirectionalMachine) machine;

        boolean isMulti = GuiScreen.isCtrlKeyDown();
        boolean facingY = dir.getEnumFacing().getAxis() == EnumFacing.Axis.Y;
        int rangeHorizontal = dir.getRangeWidth();
        int rangeVertical = facingY ? dir.getRangeLength() : dir.getRangeHeight();

        int amount = 1;

        switch (buttonName) {
            case "DecVert":
                amount = -1;
            case "IncVert":
                if ( isMulti )
                    amount *= rangeVertical * 2;
                dir.setOffsetVertical(dir.getOffsetVertical() + amount);
                break;
            case "DecHoriz":
                amount = -1;
            case "IncHoriz":
                if ( isMulti )
                    amount *= rangeHorizontal * 2;
                dir.setOffsetHorizontal(dir.getOffsetHorizontal() + amount);
                break;
            default:
                return;
        }

        gui.playClickSound(pitch);
        machine.sendModePacket();
    }
}

package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IConfigurableRange;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementOffsetControls extends ElementContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public static final String INTL_KEY = "btn." + WirelessUtils.MODID + ".offset.";

    private final BaseGuiContainer gui;
    private final IConfigurableRange config;

    private final ElementContainedButton decVert;
    private final ElementContainedButton incVert;

    private final ElementContainedButton decHoriz;
    private final ElementContainedButton incHoriz;


    public ElementOffsetControls(BaseGuiContainer gui, IConfigurableRange config, int posX, int posY) {
        super(gui, posX, posY, 30, 30);
        this.gui = gui;
        this.config = config;

        if ( config == null ) {
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

        if ( config == null || config.getRange() == 0 )
            return;

        FontRenderer fontRenderer = gui.getFontRenderer();

        fontRenderer.drawString(StringHelper.formatNumber(config.getOffsetVertical()), posX - 22, posY + 4, 0);
        fontRenderer.drawString(StringHelper.formatNumber(config.getOffsetHorizontal()), posX - 22, posY + 20, 0);

        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "vertical"), posX - 28, posY + 4, 0x404040);
        gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "horizontal"), posX - 28, posY + 20, 0x404040);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        int range = config == null ? 0 : config.getRange();
        setVisible(range != 0);
        if ( range == 0 )
            return;

        int rangeHorizontal = config.getRangeWidth();
        int rangeVertical = config.isFacingY() ? config.getRangeLength() : config.getRangeHeight();

        boolean isMulti = GuiScreen.isCtrlKeyDown();

        updateButton(decHoriz, rangeHorizontal, config.getOffsetHorizontal(), false, isMulti);
        updateButton(incHoriz, rangeHorizontal, config.getOffsetHorizontal(), true, isMulti);

        updateButton(decVert, rangeVertical, config.getOffsetVertical(), false, isMulti);
        updateButton(incVert, rangeVertical, config.getOffsetVertical(), true, isMulti);
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
        if ( config == null )
            return;

        float pitch = 0.7F;
        boolean isMulti = GuiScreen.isCtrlKeyDown();
        int rangeHorizontal = config.getRangeWidth();
        int rangeVertical = config.isFacingY() ? config.getRangeLength() : config.getRangeHeight();

        int amount = 1;

        switch (buttonName) {
            case "DecVert":
                amount = -1;
            case "IncVert":
                if ( isMulti )
                    amount *= rangeVertical * 2;
                config.setOffsetVertical(config.getOffsetVertical() + amount);
                break;
            case "DecHoriz":
                amount = -1;
            case "IncHoriz":
                if ( isMulti )
                    amount *= rangeHorizontal * 2;
                config.setOffsetHorizontal(config.getOffsetHorizontal() + amount);
                break;
            default:
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
        config.saveRanges();
    }
}

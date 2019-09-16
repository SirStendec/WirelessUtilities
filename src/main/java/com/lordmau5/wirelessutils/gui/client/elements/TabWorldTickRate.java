package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.util.helpers.RenderHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IConfigurableWorldTickRate;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.List;

public class TabWorldTickRate extends TabBase implements IContainsButtons {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");
    public static final String INTL_KEY = "info." + WirelessUtils.MODID + ".world_tick";

    public static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

    public static final int defaultSide = 0;
    public static final int defaultHeaderColor = 0xE1C92F;
    public static final int defaultSubHeaderColor = 0xAAAFB8;
    public static final int defaultTextColor = 0x000000;
    public static final int defaultBackgroundColor = 0x0a76d0;

    private final BaseGuiContainer gui;
    private final TileEntityBaseMachine machine;

    private final ElementContainedButton decButton;
    private final ElementContainedButton incButton;
    private final ElementContainedButton editButton;
    private final ElementContainedButton saveButton;
    private final ElementTextField editor;

    private boolean editMode = false;

    public TabWorldTickRate(BaseGuiContainer gui, TileEntityBaseMachine machine) {
        this(gui, defaultSide, machine);
    }

    public TabWorldTickRate(BaseGuiContainer gui, int side, TileEntityBaseMachine machine) {
        super(gui, side);

        this.gui = gui;
        this.machine = machine;

        if ( !(machine instanceof IConfigurableWorldTickRate) ) {
            setEnabled(false);
            setVisible(false);
        }

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        maxHeight = 76;
        maxWidth = 100;

        decButton = new ElementContainedButton(this, 25, 52, "DecRR", 176, 42, 176, 56, 176, 70, 14, 14, TEXTURE.toString());
        incButton = new ElementContainedButton(this, 61, 52, "IncRR", 190, 42, 190, 56, 190, 70, 14, 14, TEXTURE.toString());
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

        updateElements();
    }

    public GuiContainerCore getGui() {
        return gui;
    }

    public void handleElementButtonClick(String name, int button) {
        if ( !(machine instanceof IConfigurableWorldTickRate) )
            return;

        boolean increment = false;

        switch (name) {
            case "DecRR":
                break;
            case "IncRR":
                increment = true;
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

        IConfigurableWorldTickRate machine = (IConfigurableWorldTickRate) this.machine;
        int newValue = machine.getWorldTickRate();
        if ( newValue == -1 )
            newValue = machine.getMinWorldTickRate();

        newValue += value;
        if ( newValue < machine.getMinWorldTickRate() )
            newValue = -1;
        else if ( newValue > machine.getMaxWorldTickRate() )
            newValue = machine.getMaxWorldTickRate();

        GuiContainerCore.playClickSound(increment ? 1F : 0.7F);
        machine.setWorldTickRate(newValue);
        this.machine.sendModePacket();
    }

    public Tuple<Long, Long> getRates() {
        long change;
        long change2;

        if ( GuiScreen.isShiftKeyDown() ) {
            change = 50;
            change2 = 10;
        } else if ( GuiScreen.isAltKeyDown() ) {
            change = (machine instanceof IConfigurableWorldTickRate) ? ((IConfigurableWorldTickRate) machine).getMaxWorldTickRate() : 0;
            change2 = Math.floorDiv(change, 10);
        } else {
            change = 5;
            change2 = 1;
        }

        return new Tuple<>(change, change2);
    }

    @Override
    protected void updateElements() {
        super.updateElements();

        if ( !isFullyOpened() || !(machine instanceof IConfigurableWorldTickRate) )
            return;

        IConfigurableWorldTickRate machine = (IConfigurableWorldTickRate) this.machine;

        decButton.setVisible(!editMode);
        incButton.setVisible(!editMode);
        editButton.setVisible(!editMode);
        editor.setVisible(editMode);
        saveButton.setVisible(editMode);

        Tuple<Long, Long> rates = getRates();

        int value = machine.getWorldTickRate();
        int maxValue = machine.getMaxWorldTickRate();
        int minValue = machine.getMinWorldTickRate();

        if ( value == -1 )
            value = minValue;

        if ( value > minValue )
            decButton.setToolTipLocalized(getTooltip(false, rates.getFirst(), rates.getSecond())).setActive();
        else
            decButton.clearToolTip().setDisabled();

        if ( value < maxValue )
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

        if ( !(machine instanceof IConfigurableWorldTickRate) )
            return;

        IConfigurableWorldTickRate machine = (IConfigurableWorldTickRate) this.machine;

        editMode = enabled;

        if ( enabled ) {
            editor.setMaxLength((short) ("10" + machine.getMaxWorldTickRate()).length());
            int value = machine.getWorldTickRate();
            if ( value == -1 )
                value = machine.getMinWorldTickRate();

            editor.setText("" + value);
            editor.setFocused(true);

        } else {
            try {
                machine.setWorldTickRate(Integer.parseInt(editor.getText()));
                this.machine.sendModePacket();
            } catch (NumberFormatException ex) {
                System.out.println("Number Format Error: " + ex);
            }
        }
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !(machine instanceof IConfigurableWorldTickRate) )
            return;

        IConfigurableWorldTickRate machine = (IConfigurableWorldTickRate) this.machine;

        if ( !isFullyOpened() ) {
            list.add(StringHelper.localize(INTL_KEY + ".name"));

            int value = machine.getWorldTickRate();
            if ( value == -1 )
                value = machine.getMinWorldTickRate();

            list.add(new TextComponentTranslation(INTL_KEY + ".ticks", value).setStyle(TextHelpers.YELLOW).getFormattedText());

            return;
        }

        int mouseX = gui.getMouseX() - posX();
        int mouseY = gui.getMouseY() - posY;

        for (ElementBase c : elements) {
            if ( !c.isVisible() || !c.isEnabled() || !c.intersectsWith(mouseX, mouseY) )
                continue;

            c.addTooltip(list);
        }
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
    public boolean intersectsWith(int mouseX, int mouseY) {
        return mouseX >= this.posX() && mouseX < this.posX() + this.sizeX && mouseY >= this.posY && mouseY < this.posY + this.sizeY;
    }

    @Override
    protected void drawForeground() {
        RenderHelper.enableGUIStandardItemLighting();
        gui.getItemRenderer().renderItemAndEffectIntoGUI(CLOCK, sideOffset(), 3);
        GlStateManager.disableLighting();

        if ( !isFullyOpened() || !(machine instanceof IConfigurableWorldTickRate) )
            return;

        IConfigurableWorldTickRate machine = (IConfigurableWorldTickRate) this.machine;
        FontRenderer fontRenderer = getFontRenderer();

        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".name"), sideOffset() + 17, 8, headerColor);
        fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".frequency"), sideOffset() + 6, 22, subheaderColor);

        if ( editMode ) {
            fontRenderer.drawString(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".range",
                    machine.getMinWorldTickRate(),
                    machine.getMaxWorldTickRate()
            ).getUnformattedText(), sideOffset() + 6, 56, textColor);

        } else {
            int value = machine.getWorldTickRate();
            if ( value == -1 )
                value = machine.getMinWorldTickRate();

            fontRenderer.drawString(new TextComponentTranslation(INTL_KEY + ".ticks", value).getUnformattedText(), sideOffset() + 14, 34, textColor);
        }
    }

    @Override
    public void update() {
        super.update();

        if ( machine instanceof IConfigurableWorldTickRate )
            setVisible(((IConfigurableWorldTickRate) machine).hasWorldTick());
    }
}

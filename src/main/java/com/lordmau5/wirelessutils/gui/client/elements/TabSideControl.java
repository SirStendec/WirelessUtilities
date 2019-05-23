package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.init.CoreTextures;
import cofh.core.util.helpers.StringHelper;
import com.google.common.collect.Lists;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.ISidedTransfer;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import com.lordmau5.wirelessutils.utils.Textures;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.List;

public class TabSideControl extends TabBase implements IContainsButtons {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public static final int defaultSide = 1;
    public static final int defaultHeaderColor = 0xE1C92F;
    public static final int defaultSubHeaderColor = 0xAAAFB8;
    public static final int defaultTextColor = 0x000000;
    public static final int defaultBackgroundColor = 0x226688;

    private final TileEntityBaseMachine machine;
    private final ElementContainedButton[] buttons = new ElementContainedButton[6];

    public TabSideControl(GuiContainerCore gui, TileEntityBaseMachine machine) {
        this(gui, defaultSide, machine);
    }

    public TabSideControl(GuiContainerCore gui, int side, TileEntityBaseMachine machine) {
        super(gui, side);
        this.machine = machine;

        if ( !(this.machine instanceof ISidedTransfer) ) {
            setEnabled(false);
            setVisible(false);
            return;
        }

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        maxHeight = 92;
        maxWidth = 100;

        // Front, Back, Left, Right, Top, Bottom
        buttons[4] = new ElementContainedButton(this, 42, 24, "Top", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());
        buttons[2] = new ElementContainedButton(this, 24, 42, "Left", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());
        buttons[0] = new ElementContainedButton(this, 42, 42, "Front", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());
        buttons[3] = new ElementContainedButton(this, 60, 42, "Right", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());
        buttons[5] = new ElementContainedButton(this, 42, 60, "Bottom", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());
        buttons[1] = new ElementContainedButton(this, 60, 60, "Back", 190, 84, 190, 98, 190, 112, 14, 14, TEXTURE.toString());

        for (int i = 0; i < 6; i++)
            addElement(buttons[i]);
    }

    public GuiContainerCore getGui() {
        return gui;
    }

    @Override
    protected void updateElements() {
        super.updateElements();

        if ( !isFullyOpened() )
            return;

        if ( !(machine instanceof ISidedTransfer) )
            return;

        ISidedTransfer transfer = (ISidedTransfer) machine;

        for (ISidedTransfer.TransferSide side : ISidedTransfer.TransferSide.values()) {
            ElementContainedButton btn = buttons[side.ordinal()];
            ISidedTransfer.Mode mode = transfer.getSideTransferMode(side);

            btn.setTooltipList(Lists.newArrayList(
                    new TextComponentTranslation(
                            "tab." + WirelessUtils.MODID + ".auto_transfer.side",
                            new TextComponentTranslation(
                                    "tab." + WirelessUtils.MODID + ".auto_transfer.side." + side.name()
                            ).setStyle(TextHelpers.YELLOW)
                    ).getFormattedText(),
                    new TextComponentTranslation(
                            "tab." + WirelessUtils.MODID + ".auto_transfer.mode",
                            new TextComponentTranslation(
                                    "tab." + WirelessUtils.MODID + ".auto_transfer.mode." + mode.name()
                            ).setStyle(TextHelpers.YELLOW)
                    ).getFormattedText()
            ));

            btn.setVisible(transfer.canSideTransfer(side));
            int offsetY = getYOffset(mode);
            int offsetX = getXOffset(mode);
            btn.setSheetX(offsetX);
            btn.setSheetY(offsetY);
            btn.setHoverX(offsetX);
            btn.setHoverY(offsetY);
            btn.setDisabledX(offsetX);
            btn.setDisabledY(offsetY);
        }
    }

    public static int getYOffset(ISidedTransfer.Mode mode) {
        switch (mode) {
            case INPUT:
            case OUTPUT:
                return 126;
            default:
                return 84;
        }
    }

    public static int getXOffset(ISidedTransfer.Mode mode) {
        switch (mode) {
            case INPUT:
            case PASSIVE:
                return 204;
            case ACTIVE:
                return 190;
            case OUTPUT:
            case DISABLED:
            default:
                return 218;
        }
    }

    public void handleElementButtonClick(String name, int button) {
        if ( !(machine instanceof ISidedTransfer) )
            return;

        ISidedTransfer transfer = (ISidedTransfer) machine;
        ISidedTransfer.TransferSide side;

        switch (name) {
            case "Front":
                side = ISidedTransfer.TransferSide.FRONT;
                break;
            case "Top":
                side = ISidedTransfer.TransferSide.TOP;
                break;
            case "Left":
                side = ISidedTransfer.TransferSide.LEFT;
                break;
            case "Right":
                side = ISidedTransfer.TransferSide.RIGHT;
                break;
            case "Bottom":
                side = ISidedTransfer.TransferSide.BOTTOM;
                break;
            case "Back":
                side = ISidedTransfer.TransferSide.BACK;
                break;
            default:
                return;
        }

        ISidedTransfer.Mode mode = transfer.getSideTransferMode(side);
        if ( button == 0 )
            mode = mode.next(transfer.isModeSpecific());
        else
            mode = mode.prev(transfer.isModeSpecific());

        GuiContainerCore.playClickSound(button == 0 ? 1F : 0.7F);
        transfer.setSideTransferMode(side, mode);
        machine.sendModePacket();
    }

    @Override
    protected void drawBackground() {
        super.drawBackground();

        if ( !isFullyOpened() )
            return;

        float colorR = (backgroundColor >> 16 & 255) / 255.0F * 0.6F;
        float colorG = (backgroundColor >> 8 & 255) / 255.0F * 0.6F;
        float colorB = (backgroundColor & 255) / 255.0F * 0.6F;
        GlStateManager.color(colorR, colorG, colorB, 1.0F);
        gui.drawTexturedModalRect(18, 18, 16, 20, 62, 62);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean shouldStayOpen = super.onMousePressed(mouseX, mouseY, mouseButton);

        mouseX -= this.posX;
        mouseY -= this.posY;

        if ( mouseX >= 18 && mouseX < 80 && mouseY >= 18 && mouseY < 80 )
            return true;

        return shouldStayOpen;
    }

    @Override
    protected void drawForeground() {
        if ( !(machine instanceof ISidedTransfer) )
            return;

        ISidedTransfer transfer = (ISidedTransfer) machine;

        if ( transfer.isModeSpecific() )
            drawTabIcon(Textures.INPUT_OUTPUT);
        else {
            boolean inverted = machine instanceof IInvertAugmentable && ((IInvertAugmentable) machine).isInverted();
            drawTabIcon(inverted ? CoreTextures.ICON_OUTPUT : CoreTextures.ICON_INPUT);
        }

        if ( !isFullyOpened() )
            return;

        FontRenderer fontRenderer = getFontRenderer();
        fontRenderer.drawStringWithShadow(StringHelper.localize("tab." + WirelessUtils.MODID + ".auto_transfer"), sideOffset() + 20, 6, headerColor);
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !(machine instanceof ISidedTransfer) )
            return;

        ISidedTransfer transfer = (ISidedTransfer) machine;

        if ( !isFullyOpened() ) {
            list.add(StringHelper.localize("tab." + WirelessUtils.MODID + ".auto_transfer"));
            if ( !transfer.isModeSpecific() && machine instanceof IInvertAugmentable ) {
                boolean inverted = ((IInvertAugmentable) machine).isInverted();
                list.add(new TextComponentTranslation(
                        "tab." + WirelessUtils.MODID + ".auto_transfer.mode",
                        new TextComponentTranslation(
                                "tab." + WirelessUtils.MODID + ".auto_transfer.mode." + (inverted ? "output" : "input")
                        ).setStyle(TextHelpers.YELLOW)
                ).getFormattedText());
            }

            return;
        }

        int mouseX = gui.getMouseX() - posX();
        int mouseY = gui.getMouseY() - posY;

        for (ElementBase element : elements) {
            if ( element.isVisible() && element.isEnabled() && element.intersectsWith(mouseX, mouseY) )
                element.addTooltip(list);
        }
    }
}

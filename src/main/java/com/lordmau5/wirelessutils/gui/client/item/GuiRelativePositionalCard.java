package com.lordmau5.wirelessutils.gui.client.item;

import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.ElementTextFieldLimited;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.container.items.ContainerRelativePositionalCard;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiRelativePositionalCard extends BaseGuiItem {

    private static final String VALID_CHARACTERS = "-1234567890";

    private final ContainerRelativePositionalCard container;

    private ElementTextField txtX;
    private ElementTextField txtY;
    private ElementTextField txtZ;

    private BlockPosDimension target;
    private int renderArea = -1;

    private ElementDynamicContainedButton btnClear;
    private ElementDynamicContainedButton btnFacing;

    public GuiRelativePositionalCard(ContainerRelativePositionalCard container) {
        super(container);
        this.container = container;
        name = container.getItemStack().getDisplayName();
    }

    @Override
    public void initGui() {
        super.initGui();
        RenderManager.INSTANCE.disableHeldAreas();

        txtX = new ElementTextFieldLimited(this, xSize - 108, 18, 100, 10, (short) 10) {
            void updateValue() {
                try {
                    container.setX(Integer.parseInt(getText()));
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                if ( success )
                    updateValue();

                super.onCharacterEntered(success);
            }

            @Override
            protected void onFocusLost() {
                updateValue();
                super.onFocusLost();
            }

            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                container.setX(container.getX() + (movement > 0 ? 1 : -1));
                return true;
            }
        }.setFilter(VALID_CHARACTERS, true);
        txtY = new ElementTextFieldLimited(this, xSize - 108, 33, 100, 10, (short) 10) {
            void updateValue() {
                try {
                    container.setY(Integer.parseInt(getText()));
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                if ( success )
                    updateValue();

                super.onCharacterEntered(success);
            }

            @Override
            protected void onFocusLost() {
                updateValue();
                super.onFocusLost();
            }

            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                container.setY(container.getY() + (movement > 0 ? 1 : -1));
                return true;
            }
        }.setFilter(VALID_CHARACTERS, true);
        txtZ = new ElementTextFieldLimited(this, xSize - 108, 48, 100, 10, (short) 10) {
            void updateValue() {
                try {
                    container.setZ(Integer.parseInt(getText()));
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                if ( success )
                    updateValue();

                super.onCharacterEntered(success);
            }

            @Override
            protected void onFocusLost() {
                updateValue();
                super.onFocusLost();
            }

            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                container.setZ(container.getZ() + (movement > 0 ? 1 : -1));
                return true;
            }
        }.setFilter(VALID_CHARACTERS, true);

        btnFacing = new ElementDynamicContainedButton(this, "Facing", xSize - 109, 63, 102, 16, "");
        btnClear = new ElementDynamicContainedButton(this, "Clear", 30, 63, xSize - 139, 16, StringHelper.localize("btn." + WirelessUtils.MODID + ".clear"));

        btnClear.setToolTipLines("btn." + WirelessUtils.MODID + ".clear.info");

        txtX.setText(Integer.toString(container.getX()));
        txtY.setText(Integer.toString(container.getY()));
        txtZ.setText(Integer.toString(container.getZ()));

        addElement(txtX);
        addElement(txtY);
        addElement(txtZ);

        addElement(btnFacing);
        addElement(btnClear);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".x"), 30, 19, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".y"), 30, 34, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".z"), 30, 49, 0x404040);

        Vec3d vector = container.getVector();
        if ( vector == null )
            return;

        final String range = new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".relative_positional_card.distance",
                new TextComponentString(StringHelper.formatNumber((int) Math.floor(vector.length()))).setStyle(TextHelpers.BLACK)
        ).getFormattedText();

        drawRightAlignedText(range, xSize - 6, ySize - 96 + 3, 0x404040);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        boolean locked = container.isLocked();

        txtX.setEnabled(!locked);
        txtY.setEnabled(!locked);
        txtZ.setEnabled(!locked);
        btnFacing.setEnabled(!locked);

        btnClear.setEnabled(!locked && StringHelper.isControlKeyDown());

        if ( !txtX.isFocused() )
            txtX.setText(Integer.toString(container.getX()));

        if ( !txtY.isFocused() )
            txtY.setText(Integer.toString(container.getY()));

        if ( !txtZ.isFocused() )
            txtZ.setText(Integer.toString(container.getZ()));

        btnFacing.setText(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".blockpos.side",
                container.getFacing().toString()
        ).getFormattedText());

        BlockPosDimension newTarget = container.getTarget();
        if ( target != newTarget ) {
            if ( renderArea != -1 )
                RenderManager.INSTANCE.removeArea(renderArea);

            target = newTarget;
            if ( target == null )
                renderArea = -1;
            else
                renderArea = RenderManager.INSTANCE.addArea(new BlockArea(
                                target,
                                NiceColors.HANDY_COLORS[0],
                                null,
                                container.getVector()
                        )
                );
        }
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "Facing":
                if ( !container.setFacing(EnumFacing.byIndex(container.getFacing().ordinal() + amount)) )
                    return;
                break;
            case "Clear":
                if ( mouseButton == 0 && StringHelper.isControlKeyDown() ) {
                    container.clear();
                    mc.player.closeScreen();
                    break;
                }
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        playClickSound(pitch);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if ( renderArea != -1 ) {
            RenderManager.INSTANCE.removeArea(renderArea);
            renderArea = -1;
        }

        container.sendUpdate();
        RenderManager.INSTANCE.enableHeldAreas();
    }
}

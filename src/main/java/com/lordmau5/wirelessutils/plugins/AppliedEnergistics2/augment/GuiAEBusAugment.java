package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.augment;

import cofh.core.gui.element.listbox.SliderHorizontal;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.utils.BusTransferMode;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class GuiAEBusAugment extends BaseGuiItem {

    private final static String I18N_KEY = "item." + WirelessUtils.MODID + ".ae_bus_augment";

    private final ContainerAEBusAugment container;

    private SliderHorizontal ctlTick;
    private boolean updating = false;

    private ElementDynamicContainedButton btnEnergyMode;
    private ElementDynamicContainedButton btnItemsMode;
    private ElementDynamicContainedButton btnFluidMode;


    public GuiAEBusAugment(ContainerAEBusAugment container) {
        super(container);
        this.container = container;
        name = container.getItemStack().getDisplayName();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        container.sendUpdateIfChanged();
    }

    @Override
    public void initGui() {
        super.initGui();

        ctlTick = new SliderHorizontal(this, xSize - 108, 19, 100, 10, Byte.MAX_VALUE) {
            @Override
            public void addTooltip(List<String> list) {
                super.addTooltip(list);

                list.add(new TextComponentTranslation(
                        I18N_KEY + ".tick_rate.slow",
                        TextHelpers.getComponent(container.getTickRate()).setStyle(TextHelpers.WHITE),
                        TextHelpers.getComponent(container.getMinTickRate()).setStyle(TextHelpers.WHITE)
                ).setStyle(TextHelpers.GRAY).getFormattedText());
            }

            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    container.setTickRate((byte) 0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                if ( !updating )
                    container.setTickRate((byte) value);
            }
        };

        btnEnergyMode = new ElementDynamicContainedButton(this, "Energy", xSize - 109, 32, 102, 14, "");
        btnItemsMode = new ElementDynamicContainedButton(this, "Items", xSize - 109, 48, 102, 14, "");
        btnFluidMode = new ElementDynamicContainedButton(this, "Fluid", xSize - 109, 64, 102, 14, "");

        updating = true;
        ctlTick.setLimits(container.getMinTickRate(), Byte.MAX_VALUE);
        ctlTick.setValue(container.getTickRate());
        updating = false;

        addElement(ctlTick);
        addElement(btnEnergyMode);
        addElement(btnItemsMode);
        addElement(btnFluidMode);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "Energy":
                container.setEnergyMode(BusTransferMode.byIndex(container.getEnergyMode().ordinal() + amount));
                break;
            case "Items":
                container.setItemsMode(BusTransferMode.byIndex(container.getItemsMode().ordinal() + amount));
                break;
            case "Fluid":
                container.setFluidMode(BusTransferMode.byIndex(container.getFluidMode().ordinal() + amount));
                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();
        updating = true;

        ctlTick.setLimits(container.getMinTickRate(), Byte.MAX_VALUE);
        ctlTick.setValue(container.getTickRate());

        btnEnergyMode.setText(container.getEnergyMode().getComponent().getFormattedText());
        btnItemsMode.setText(container.getItemsMode().getComponent().getFormattedText());
        btnFluidMode.setText(container.getFluidMode().getComponent().getFormattedText());

        final boolean editable = !container.isLocked();

        ctlTick.setEnabled(editable);

        btnEnergyMode.setEnabled(editable);
        btnItemsMode.setEnabled(editable);
        btnFluidMode.setEnabled(editable);

        updating = false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        drawRightAlignedText(StringHelper.localize(I18N_KEY + ".rate"), xSize - 111, 21, textColor);
        drawRightAlignedText(StringHelper.localize(I18N_KEY + ".energy_mode"), xSize - 111, 35, textColor);
        drawRightAlignedText(StringHelper.localize(I18N_KEY + ".items_mode"), xSize - 111, 51, textColor);
        drawRightAlignedText(StringHelper.localize(I18N_KEY + ".fluid_mode"), xSize - 111, 67, textColor);
    }
}

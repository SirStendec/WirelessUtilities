package com.lordmau5.wirelessutils.gui.client.item;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.container.items.ContainerFacingAugment;
import net.minecraft.util.EnumFacing;

public class GuiFacingAugment extends BaseGuiItem {

    private final ContainerFacingAugment container;

    private ElementDynamicContainedButton btnFacing;

    public GuiFacingAugment(ContainerFacingAugment container) {
        super(container);
        this.container = container;
        name = container.getItemStack().getDisplayName();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        container.sendUpdate();
    }

    @Override
    public void initGui() {
        super.initGui();

        btnFacing = new ElementDynamicContainedButton(this, "Facing", xSize - 109, 33, 102, 16, "");
        addElement(btnFacing);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString(StringHelper.localizeFormat("info." + WirelessUtils.MODID + ".blockpos.side", ""), 30, 37, 0x404040);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        btnFacing.setEnabled(!container.isLocked());
        btnFacing.setText(String.valueOf(container.getFacing()));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        switch (buttonName) {
            case "Facing":
                EnumFacing facing = container.getFacing();
                int index = facing == null ? -1 : facing.ordinal();
                index += amount;

                if ( container.allowNull() ) {
                    if ( index < -1 )
                        index = EnumFacing.VALUES.length - 1;
                    else if ( index >= EnumFacing.VALUES.length )
                        index = -1;

                    facing = index == -1 ? null : EnumFacing.byIndex(index);
                } else
                    facing = EnumFacing.byIndex(index);

                if ( !container.setFacing(facing) )
                    return;

                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        playClickSound(pitch);
    }
}

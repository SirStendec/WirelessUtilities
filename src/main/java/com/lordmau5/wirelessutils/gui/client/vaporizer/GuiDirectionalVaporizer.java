package com.lordmau5.wirelessutils.gui.client.vaporizer;

import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.ElementFluidTank;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.SharedState;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementOffsetControls;
import com.lordmau5.wirelessutils.gui.client.elements.ElementRangeControls;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerDirectionalVaporizer;
import com.lordmau5.wirelessutils.tile.vaporizer.TileDirectionalVaporizer;
import com.lordmau5.wirelessutils.utils.Textures;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiDirectionalVaporizer extends GuiBaseVaporizer {

    private final TileDirectionalVaporizer vaporizer;

    private ElementDynamicContainedButton btnMode;
    private ElementRangeControls rangeControls;
    private ElementOffsetControls offsetControls;

    public GuiDirectionalVaporizer(InventoryPlayer player, TileDirectionalVaporizer vaporizer) {
        super(new ContainerDirectionalVaporizer(player, vaporizer), vaporizer);
        this.vaporizer = vaporizer;

        generateInfo("tab." + WirelessUtils.MODID + ".directional_vaporizer");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 10, 46, vaporizer.getEnergyStorage()).setInfinite(vaporizer.isCreative()));

        if ( vaporizer.hasFluid() )
            addElement(new ElementFluidTank(this, 34, 52, vaporizer.getTank()).setAlwaysShow(true).setSmall().drawTank(true));

        addElement(new ElementAreaButton(this, vaporizer, 152, 89));

        btnMode = new ElementDynamicContainedButton(this, "Mode", 134, 89, 16, 16, Textures.SIZE);
        addElement(btnMode);

        rangeControls = new ElementRangeControls(this, vaporizer, 138, 38);
        addElement(rangeControls);

        offsetControls = new ElementOffsetControls(this, vaporizer, 138, 38);
        addElement(offsetControls);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        switch (buttonName) {
            case "Mode":
                SharedState.offsetMode = !SharedState.offsetMode;
                break;
            default:
                return;
        }

        playClickSound(1F);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        boolean offsetMode = SharedState.offsetMode;

        btnMode.setIcon(offsetMode ? Textures.OFFSET : Textures.SIZE);
        btnMode.setToolTip("btn." + WirelessUtils.MODID + ".mode." + (offsetMode ? "offset" : "range"));
        btnMode.setVisible(vaporizer.getRange() > 0);

        rangeControls.setVisible(!offsetMode);
        offsetControls.setVisible(offsetMode);
    }
}

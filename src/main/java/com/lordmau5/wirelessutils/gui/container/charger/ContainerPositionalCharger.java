package com.lordmau5.wirelessutils.gui.container.charger;

import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import com.lordmau5.wirelessutils.tile.charger.TileEntityPositionalCharger;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPositionalCharger extends BaseContainerPositional {

    public ContainerPositionalCharger(InventoryPlayer playerInventory, TileEntityPositionalCharger charger) {
        super(playerInventory, charger);
        addOwnSlots();
    }
}

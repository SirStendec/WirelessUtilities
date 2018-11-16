package com.lordmau5.wirelessutils.gui.container.charger;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDirectionalCharger extends BaseContainerTile {
    public ContainerDirectionalCharger(InventoryPlayer playerInventory, TileEntityDirectionalCharger charger) {
        super(playerInventory, charger, true, true);
    }
}

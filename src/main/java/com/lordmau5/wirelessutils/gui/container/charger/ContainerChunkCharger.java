package com.lordmau5.wirelessutils.gui.container.charger;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.tile.charger.TileEntityChunkCharger;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerChunkCharger extends BaseContainerTile {
    public ContainerChunkCharger(InventoryPlayer playerInventory, TileEntityChunkCharger charger) {
        super(playerInventory, charger, true, true);
    }
}

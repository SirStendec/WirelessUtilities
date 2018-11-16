package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.positional;

import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPositionalRSNetwork extends BaseContainerPositional {

    public ContainerPositionalRSNetwork(InventoryPlayer playerInventory, TilePositionalRSNetwork tile) {
        super(playerInventory, tile);
        addOwnSlots();
    }
}

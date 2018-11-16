package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional;

import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPositionalAENetwork extends BaseContainerPositional {

    public ContainerPositionalAENetwork(InventoryPlayer playerInventory, TilePositionalAENetwork tile) {
        super(playerInventory, tile);
        addOwnSlots();
    }
}

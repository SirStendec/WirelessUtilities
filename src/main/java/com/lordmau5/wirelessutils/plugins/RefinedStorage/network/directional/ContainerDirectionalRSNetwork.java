package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.directional;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDirectionalRSNetwork extends BaseContainerTile {
    public ContainerDirectionalRSNetwork(InventoryPlayer playerInventory, TileDirectionalRSNetwork te) {
        super(playerInventory, te, true, true);
    }
}

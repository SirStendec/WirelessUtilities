package com.lordmau5.wirelessutils.plugins.XNet.network.directional;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDirectionalXNetNetwork extends BaseContainerTile {
    public ContainerDirectionalXNetNetwork(InventoryPlayer playerInventory, TileDirectionalXNetNetwork te) {
        super(playerInventory, te, true, true);
    }
}

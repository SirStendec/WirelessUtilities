package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDirectionalAENetwork extends BaseContainerTile {
    public ContainerDirectionalAENetwork(InventoryPlayer playerInventory, TileDirectionalAENetwork te) {
        super(playerInventory, te, true, true);
    }
}

package com.lordmau5.wirelessutils.gui.container.condenser;

import com.lordmau5.wirelessutils.gui.container.BaseContainerTile;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityDirectionalCondenser;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDirectionalCondenser extends BaseContainerTile {
    public ContainerDirectionalCondenser(InventoryPlayer playerInventory, TileEntityDirectionalCondenser condenser) {
        super(playerInventory, condenser, true, true);
    }
}

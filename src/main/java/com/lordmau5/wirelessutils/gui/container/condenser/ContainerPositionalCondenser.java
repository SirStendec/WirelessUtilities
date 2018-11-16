package com.lordmau5.wirelessutils.gui.container.condenser;

import com.lordmau5.wirelessutils.gui.container.BaseContainerPositional;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityPositionalCondenser;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPositionalCondenser extends BaseContainerPositional {

    public ContainerPositionalCondenser(InventoryPlayer player, TileEntityPositionalCondenser condenser) {
        super(player, condenser);
        addOwnSlots();
    }

}

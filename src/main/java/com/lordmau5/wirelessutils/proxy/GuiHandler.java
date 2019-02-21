package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.GuiPlayerCard;
import com.lordmau5.wirelessutils.gui.container.ContainerPlayerCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHandler extends cofh.core.gui.GuiHandler {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_PLAYER_CARD ) {
            return new GuiPlayerCard(new ContainerPlayerCard(player.inventory));
        }

        return super.getClientGuiElement(id, player, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if ( id == WirelessUtils.GUI_PLAYER_CARD ) {
            return new ContainerPlayerCard(player.inventory);
        }

        return super.getServerGuiElement(id, player, world, x, y, z);
    }
}

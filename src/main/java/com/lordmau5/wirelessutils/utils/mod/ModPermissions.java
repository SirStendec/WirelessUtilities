package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum ModPermissions {

    COMMAND_FLUIDGEN,
    COMMAND_DEBUG,
    COMMAND_EDIT_ITEM,
    COMMAND_VOIDGEN;

    public final String node;
    public final String desc;
    public final DefaultPermissionLevel level;

    ModPermissions() {
        this(DefaultPermissionLevel.OP, null);
    }

    ModPermissions(@Nullable String description) {
        this(DefaultPermissionLevel.OP, description);
    }

    ModPermissions(@Nonnull DefaultPermissionLevel level, @Nullable String desc) {
        this.level = level;
        this.desc = desc == null ? "" : desc;
        node = WirelessUtils.MODID + "." + name().toLowerCase().replaceAll("_", ".");
    }

    public boolean hasPermission(ICommandSender sender) {
        EntityPlayerMP player;
        try {
            player = CommandBase.getCommandSenderAsPlayer(sender);
        } catch (PlayerNotFoundException ex) {
            player = null;
        }

        return hasPermission(player);
    }

    public boolean hasPermission(EntityPlayer player) {
        return PermissionAPI.hasPermission(player, node);
    }

    public static void register() {
        for (ModPermissions permission : values()) {
            PermissionAPI.registerNode(permission.node, permission.level, permission.desc);
        }
    }
}

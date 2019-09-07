package com.lordmau5.wirelessutils.commands;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.IAdminEditableItem;
import com.lordmau5.wirelessutils.utils.mod.ModPermissions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

public class EditItemCommand extends CommandBase {

    @Nonnull
    public String getName() {
        return "wu_edit";
    }

    @Nonnull
    public String getUsage(ICommandSender sender) {
        return "wu_edit";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return ModPermissions.COMMAND_EDIT_ITEM.hasPermission(sender);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if ( player == null )
            return;

        ItemStack stack = player.getHeldItemMainhand();
        Item item = stack.getItem();

        if ( stack.isEmpty() || !(item instanceof IAdminEditableItem) )
            throw new CommandException("commands." + WirelessUtils.MODID + ".edit_item.not_holding", 0);

        ((IAdminEditableItem) item).openAdminGui(player, EnumHand.MAIN_HAND);
    }
}

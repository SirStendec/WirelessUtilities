package com.lordmau5.wirelessutils.commands;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DebugCommand extends CommandBase {

    private final List<String> ALIASES = Lists.newArrayList("wu_debug");

    @Override
    public String getName() {
        return "wu_debug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "wu_debug";
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 1;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if ( player == null )
            return;

        ItemStack bedStack = new ItemStack(Items.BED, 1, new Random().nextInt(16));
        bedStack.setStackDisplayName("Debug");

        if ( !player.addItemStackToInventory(bedStack) )
            player.entityDropItem(bedStack, 0);

        sender.sendMessage(new TextComponentString("Done."));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }
}

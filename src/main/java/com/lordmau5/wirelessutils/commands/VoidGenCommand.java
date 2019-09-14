package com.lordmau5.wirelessutils.commands;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import com.lordmau5.wirelessutils.utils.mod.ModPermissions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

public class VoidGenCommand extends CommandBase {

    @Override
    public String getName() {
        return "wu_voidgen";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands." + WirelessUtils.MODID + ".voidgen.usage";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return ModPermissions.COMMAND_VOIDGEN.hasPermission(sender);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if ( player == null )
            return;

        if ( args.length < 1 )
            throw new WrongUsageException("commands." + WirelessUtils.MODID + ".voidgen.usage", 0);

        if ( args[0].equalsIgnoreCase("looking") ) {

        }

        ResourceLocation key = new ResourceLocation(args[0]);
        if ( !EntityList.isRegistered(key) )
            throw new CommandException("command." + WirelessUtils.MODID + ".voidgen.invalid_entity", args[0]);

        final boolean crystalized = args.length > 1 && isTrue(args[1]);
        ItemStack stack = new ItemStack(crystalized ? ModItems.itemCrystallizedVoidPearl : ModItems.itemVoidPearl);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("EntityID", key.toString());

        if ( args.length > 2 ) {
            String s = buildString(args, 2);
            try {
                tag.setTag("EntityData", JsonToNBT.getTagFromJson(s));
            } catch (NBTException ex) {
                throw new CommandException("commands." + WirelessUtils.MODID + ".voidgen.tag_error", ex.getMessage());
            }
        }

        stack.setTagCompound(tag);

        sender.sendMessage(new TextComponentTranslation(
                "commands." + WirelessUtils.MODID + ".voidgen.success",
                stack.getTextComponent()
        ));

        if ( !player.addItemStackToInventory(stack) )
            player.entityDropItem(stack, 0);
    }

    public static boolean isTrue(String input) {
        return input.equalsIgnoreCase("t") || input.equalsIgnoreCase("true") || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("1");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if ( args.length < 2 )
            return getListOfStringsMatchingLastWord(args, EntityList.getEntityNameList());

        if ( args.length < 3 )
            return getListOfStringsMatchingLastWord(args, "true", "false");

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}

package com.lordmau5.wirelessutils.commands;

import com.google.common.collect.Lists;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidGenCommand extends CommandBase {

    private final List<String> ALIASES = Lists.newArrayList("wu_fluidgen");

    @Override
    public String getName() {
        return "wu_fluidgen";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "wu_fluidgen <fluid> <mb/t> <energy> [color/-] [fluid-nbt-data]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 1;
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if ( player == null )
            return;

        if ( args.length == 0 ) {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        if ( args.length < 3 ) {
            sender.sendMessage(new TextComponentString("Invalid Arguments"));
            return;
        }

        Fluid fluid = FluidRegistry.getFluid(args[0]);
        if ( fluid == null ) {
            sender.sendMessage(new TextComponentString("No Such Fluid: " + args[0]));
            return;
        }

        int amount = parseInt(args[1], 1);
        int cost = parseInt(args[2], 0);

        FluidStack fluidStack = new FluidStack(fluid, amount);
        ItemStack stack = new ItemStack(ModItems.itemFluidGenAugment);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound ftag = new NBTTagCompound();

        fluidStack.writeToNBT(ftag);
        tag.setTag("Fluid", ftag);
        tag.setInteger("Energy", cost);

        if ( args.length > 3 && !args[3].equalsIgnoreCase("-") ) {
            int color;
            if ( args[3].startsWith("0x") )
                color = parseHex(args[3], 0, 0xFFFFFF);
            else
                color = parseInt(args[3], 0, 0xFFFFFF);

            tag.setInteger("Color", color);
        }

        if ( args.length > 4 ) {
            String s = buildString(args, 4);
            try {
                ftag.merge(JsonToNBT.getTagFromJson(s));
                tag.setTag("Fluid", ftag);
            } catch (NBTException ex) {
                throw new CommandException("commands.give.tagError", ex.getMessage());
            }
        }

        stack.setTagCompound(tag);

        if ( !player.addItemStackToInventory(stack) )
            player.entityDropItem(stack, 0);

        sender.sendMessage(new TextComponentString("Done."));
    }

    public static int parseHex(String input) throws NumberInvalidException {
        boolean negative = false;
        if ( input.startsWith("-") ) {
            negative = true;
            input = input.substring(1);
        }

        if ( input.startsWith("0x") )
            input = input.substring(2);


        try {
            return (negative ? -1 : 1) * Integer.parseInt(input, 16);
        } catch (NumberFormatException ex) {
            throw new NumberInvalidException("commands.generic.num.invalid", input);
        }
    }

    public static int parseHex(String input, int min) throws NumberInvalidException {
        return parseHex(input, min, Integer.MAX_VALUE);
    }

    public static int parseHex(String input, int min, int max) throws NumberInvalidException {
        int i = parseHex(input);

        if ( i < min )
            throw new NumberInvalidException("commands.generic.num.tooSmall", i, min);

        if ( i > max )
            throw new NumberInvalidException("commands.generic.num.tooBig", i, max);

        return i;
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if ( args.length < 2 )
            return getListOfStringsMatchingLastWord(args, FluidRegistry.getRegisteredFluids().keySet());

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}

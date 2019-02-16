package com.lordmau5.wirelessutils.commands;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FluidGenCommand extends CommandBase {

    @Override
    public String getName() {
        return "wu_fluidgen";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands." + WirelessUtils.MODID + ".fluidgen.usage";
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

        if ( args.length < 3 )
            throw new WrongUsageException("commands." + WirelessUtils.MODID + ".fluidgen.usage", 0);

        Fluid fluid = null;

        if ( args[0].equalsIgnoreCase("held") ) {
            ItemStack held = player.getHeldItemMainhand();
            IFluidHandlerItem handler = held.isEmpty() ? null : held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if ( handler == null )
                throw new CommandException("command." + WirelessUtils.MODID + ".fluidgen.not_holding", 0);

            IFluidTankProperties[] properties = handler.getTankProperties();
            if ( properties == null || properties.length == 0 )
                throw new CommandException("command." + WirelessUtils.MODID + ".fluidgen.not_holding", 0);

            for (IFluidTankProperties prop : properties) {
                FluidStack contents = prop.getContents();
                if ( contents != null ) {
                    fluid = contents.getFluid();
                    if ( fluid != null )
                        break;
                }
            }

            if ( fluid == null )
                throw new CommandException("command." + WirelessUtils.MODID + ".fluidgen.not_holding", 0);

        } else {
            fluid = FluidRegistry.getFluid(args[0]);
            if ( fluid == null )
                throw new CommandException("commands." + WirelessUtils.MODID + ".fluidgen.no_such_fluid", args[0]);
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
                throw new CommandException("commands." + WirelessUtils.MODID + ".fluidgen.tag_error", ex.getMessage());
            }
        }

        stack.setTagCompound(tag);

        sender.sendMessage(new TextComponentTranslation("commands." + WirelessUtils.MODID + ".fluidgen.success", stack.getTextComponent()));

        if ( !player.addItemStackToInventory(stack) )
            player.entityDropItem(stack, 0);
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
        if ( args.length < 2 ) {
            Set<String> values = new HashSet<>(FluidRegistry.getRegisteredFluids().keySet());
            values.add("held");

            return getListOfStringsMatchingLastWord(args, values);
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}

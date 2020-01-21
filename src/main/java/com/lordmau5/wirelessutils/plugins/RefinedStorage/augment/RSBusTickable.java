package com.lordmau5.wirelessutils.plugins.RefinedStorage.augment;

import cofh.core.util.helpers.InventoryHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.augment.ITickableAugment;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseNetwork;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBusAugmentable;
import com.lordmau5.wirelessutils.utils.BusTransferMode;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.util.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RSBusTickable implements ITickableAugment {

    private ItemStack stack;
    private final TileEntityBaseNetwork tile;
    private final IBusAugmentable augmentable;

    private byte tickRate;
    private int energyRate;
    private int itemsRate;
    private int fluidRate;

    private BusTransferMode energyMode = BusTransferMode.DISABLED;
    private BusTransferMode itemsMode = BusTransferMode.DISABLED;
    private BusTransferMode fluidMode = BusTransferMode.DISABLED;

    private int remainingEnergy = 0;
    private int remainingItems = 0;
    private int remainingFluid = 0;

    private byte ticks = 0;

    public RSBusTickable(@Nonnull ItemStack stack, @Nonnull TileEntityBaseNetwork tile) {
        this.stack = stack;
        this.tile = tile;
        this.augmentable = (IBusAugmentable) tile;

        update(stack);
        tile.setRSEnabled(true);
    }

    public void destroy() {
        tile.setRSEnabled(false);
    }

    @Override
    public boolean update(@Nonnull ItemStack stack) {
        this.stack = stack;
        updateLevel();
        return true;
    }

    private void updateLevel() {
        tickRate = RefinedStoragePlugin.itemRSBusAugment.getMinTickRate(stack);
        if ( tickRate < 0 )
            tickRate = Byte.MAX_VALUE;

        if ( ticks > tickRate )
            ticks = tickRate;

        energyRate = RefinedStoragePlugin.itemRSBusAugment.getEnergyRate(stack);
        itemsRate = RefinedStoragePlugin.itemRSBusAugment.getItemsRate(stack);
        fluidRate = RefinedStoragePlugin.itemRSBusAugment.getFluidRate(stack);

        energyMode = RefinedStoragePlugin.itemRSBusAugment.getEnergyMode(stack);
        itemsMode = RefinedStoragePlugin.itemRSBusAugment.getItemsMode(stack);
        fluidMode = RefinedStoragePlugin.itemRSBusAugment.getFluidMode(stack);
    }


    /* Save and Load */

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Ticks", ticks);

        if ( !stack.isEmpty() )
            tag.setTag("Item", stack.serializeNBT());

        return tag;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        ticks = tag.getByte("Ticks");

        if ( tag.hasKey("Item", Constants.NBT.TAG_COMPOUND) )
            stack = new ItemStack(tag.getCompoundTag("Item"));
        else
            stack = ItemStack.EMPTY;

        updateLevel();
    }


    /* ITickableAugment */

    @Nonnull
    public ItemStack getItemStack() {
        return stack;
    }

    public void preTick(boolean active) {
        if ( !active )
            return;

        ticks--;
        if ( ticks == -1 )
            ticks = tickRate;

        remainingEnergy = energyRate;
        remainingItems = itemsRate;
        remainingFluid = fluidRate;

        execute(IBusAugmentable.TickPhase.PRE);
    }

    public void tick(boolean active) {
        // Nothing
    }

    public void postTick() {
        execute(IBusAugmentable.TickPhase.POST);
    }

    /* Runner */

    @Nullable
    private INetwork getNetwork() {
        INetworkNode node = tile.getRSNode();
        if ( node == null )
            return null;
        return node.getNetwork();
    }

    private void execute(IBusAugmentable.TickPhase phase) {
        if ( ticks != 0 || ((energyMode == BusTransferMode.DISABLED || remainingEnergy == 0) && (itemsMode == BusTransferMode.DISABLED || remainingItems == 0) && (fluidMode == BusTransferMode.DISABLED || remainingFluid == 0)) )
            return;

        WirelessUtils.profiler.startSection("RSBus:Init");

        final INetwork network = getNetwork();
        if ( network == null ) {
            WirelessUtils.profiler.endSection();
            return;
        }


        // Energy
        WirelessUtils.profiler.endStartSection("RSBus:Energy");

        if ( energyMode._import && remainingEnergy > 0 && augmentable.getBusShouldExtractEnergy(phase) )
            remainingEnergy = importEnergy(remainingEnergy, network);

        if ( energyMode._export && remainingEnergy > 0 && augmentable.getBusShouldInsertEnergy(phase) )
            remainingEnergy = exportEnergy(remainingEnergy, network);


        // Items
        WirelessUtils.profiler.endStartSection("RSBus:Items");

        if ( itemsMode._import && remainingItems > 0 ) {
            final IItemHandler itemExtractHandler = augmentable.getBusItemOutputHandler(phase);
            if ( itemExtractHandler != null )
                remainingItems = importItems(remainingItems, itemExtractHandler, network);
        }

        if ( itemsMode._export && remainingItems > 0 ) {
            final ItemStack[] itemRequest = augmentable.getBusItemInputRequest(phase);
            if ( itemRequest != null ) {
                final IItemHandler itemInsertHandler = augmentable.getBusItemInputHandler(phase);
                if ( itemInsertHandler != null )
                    remainingItems = exportItems(remainingItems, itemRequest, augmentable.doesBusItemInputRequestMatchSlots(phase), itemInsertHandler, network);
            }
        }


        // Fluid
        WirelessUtils.profiler.endStartSection("RSBus:Fluid");

        if ( fluidMode._import && remainingFluid > 0 ) {
            final IFluidHandler fluidExtractHandler = augmentable.getBusFluidOutputHandler(phase);
            if ( fluidExtractHandler != null )
                remainingFluid = importFluid(remainingFluid, fluidExtractHandler, network);
        }

        if ( fluidMode._export && remainingFluid > 0 ) {
            final FluidStack[] fluidRequest = augmentable.getBusFluidInputRequest(phase);
            if ( fluidRequest != null ) {
                final IFluidHandler fluidInputHandler = augmentable.getBusFluidInputHandler(phase);
                if ( fluidInputHandler != null )
                    remainingFluid = exportFluid(remainingFluid, fluidRequest, fluidInputHandler, network);
            }
        }


        WirelessUtils.profiler.endSection();
    }


    /* Energy */

    private int importEnergy(int budget, @Nonnull INetwork network) {
        WirelessUtils.profiler.startSection("ImportEnergy");

        int available = Math.min(tile.getEnergyStored(), tile.getMaxExtract());
        if ( available < 1 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        if ( available > budget )
            available = budget;

        int value = network.getEnergy().insert(available, Action.SIMULATE);
        if ( value > 0 )
            value = tile.extractEnergy(value, false);

        if ( value == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        network.getEnergy().insert(value, Action.PERFORM);
        WirelessUtils.profiler.endSection();
        return budget - value;
    }

    private int exportEnergy(int budget, @Nonnull INetwork network) {
        WirelessUtils.profiler.startSection("ExportEnergy");

        int needed = Math.min(tile.getMaxReceive(), tile.getMaxEnergyStored() - tile.getEnergyStored());
        if ( needed < 1 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        if ( needed > budget )
            needed = budget;

        int available = network.getEnergy().extract(needed, Action.SIMULATE);
        if ( available == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        int received = tile.receiveEnergy(available, false);
        if ( received == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        network.getEnergy().extract(received, Action.PERFORM);
        WirelessUtils.profiler.endSection();
        return budget - received;
    }


    /* Items */

    private int importItems(int budget, @Nonnull IItemHandler handler, @Nonnull INetwork network) {
        WirelessUtils.profiler.startSection("ImportItems");

        final int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = handler.extractItem(i, budget, true);
            if ( stack.isEmpty() )
                continue;

            int extracted = stack.getCount();
            ItemStack remaining = network.insertItem(stack, extracted, Action.PERFORM);
            if ( remaining != null )
                extracted -= remaining.getCount();

            if ( extracted > 0 ) {
                handler.extractItem(i, extracted, false);

                budget -= extracted;
                if ( budget < 1 )
                    break;
            }
        }

        WirelessUtils.profiler.endSection();
        return budget;
    }

    private int exportItems(int budget, @Nonnull ItemStack[] request, boolean slotsMatch, @Nonnull IItemHandler handler, @Nonnull INetwork network) {
        WirelessUtils.profiler.startSection("ExportItems");

        int slots = handler.getSlots();
        if ( slotsMatch )
            slots = Math.min(slots, request.length);

        if ( slots == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        for (int i = 0; i < slots; i++) {
            ItemStack requested = request[i];
            if ( requested == null || requested.isEmpty() )
                continue;

            int count = requested.getCount();

            if ( slotsMatch ) {
                ItemStack existing = handler.getStackInSlot(i);
                count -= existing.getCount();
            }

            if ( count < 1 )
                continue;

            ItemStack available = network.extractItem(requested, count, Action.SIMULATE);
            if ( available == null || available.isEmpty() )
                continue;

            ItemStack remaining;
            int inserted = available.getCount();

            if ( slotsMatch )
                remaining = handler.insertItem(i, available, false);
            else
                remaining = InventoryHelper.insertStackIntoInventory(handler, available, false);

            inserted -= remaining.getCount();

            if ( inserted > 0 ) {
                network.extractItem(requested, inserted, Action.PERFORM);

                budget -= inserted;
                if ( budget < 1 )
                    break;
            }
        }

        WirelessUtils.profiler.endSection();
        return budget;
    }


    /* Fluid */

    private int importFluid(int budget, @Nonnull IFluidHandler handler, @Nonnull INetwork network) {
        WirelessUtils.profiler.startSection("ImportFluid");

        final IFluidTankProperties[] tanks = handler.getTankProperties();
        if ( tanks == null ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        for (IFluidTankProperties tank : tanks) {
            if ( tank == null || !tank.canDrain() )
                continue;

            FluidStack stack = tank.getContents();
            if ( stack == null || stack.amount == 0 )
                continue;

            FluidStack drained = handler.drain(stack, false);
            if ( drained == null || drained.amount == 0 )
                continue;

            if ( drained.amount > budget )
                drained.amount = budget;

            int extracted = drained.amount;
            FluidStack remaining = network.insertFluid(drained, drained.amount, Action.PERFORM);
            if ( remaining != null )
                extracted -= remaining.amount;

            if ( extracted > 0 ) {
                FluidStack drain = new FluidStack(drained, extracted);
                handler.drain(drain, true);

                budget -= extracted;
                if ( budget < 1 )
                    break;
            }
        }

        WirelessUtils.profiler.endSection();
        return budget;
    }

    private int exportFluid(int budget, @Nonnull FluidStack[] request, @Nonnull IFluidHandler handler, @Nonnull INetwork network) {
        WirelessUtils.profiler.startSection("ExportFluid");

        for (int i = 0; i < request.length; i++) {
            FluidStack stack = request[i];
            if ( stack == null || stack.amount == 0 )
                continue;

            int amount = stack.amount;
            if ( amount > budget )
                amount = budget;

            FluidStack available = network.extractFluid(stack, amount, Action.SIMULATE);
            if ( available == null || available.amount == 0 )
                continue;

            int inserted = handler.fill(available, true);

            if ( inserted > 0 ) {
                network.extractFluid(stack, inserted, Action.PERFORM);

                budget -= inserted;
                if ( budget < 1 )
                    break;
            }
        }

        WirelessUtils.profiler.endSection();
        return budget;
    }

}

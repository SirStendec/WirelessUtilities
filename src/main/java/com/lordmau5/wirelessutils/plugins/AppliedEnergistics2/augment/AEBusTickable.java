package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.augment;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import cofh.core.util.helpers.InventoryHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.augment.ITickableAugment;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseNetwork;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBusAugmentable;
import com.lordmau5.wirelessutils.utils.BusTransferMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class AEBusTickable implements ITickableAugment, IActionSource {

    private ItemStack stack;
    private final TileEntityBaseNetwork tile;
    private final IBusAugmentable augmentable;

    private byte tickRate;
    private int itemsRate;
    private int fluidRate;
    private int energyRate;

    private BusTransferMode itemsMode = BusTransferMode.DISABLED;
    private BusTransferMode fluidMode = BusTransferMode.DISABLED;
    private BusTransferMode energyMode = BusTransferMode.DISABLED;

    private int remainingItems = 0;
    private int remainingFluid = 0;
    private int remainingEnergy = 0;

    private byte ticks = 0;


    public AEBusTickable(@Nonnull ItemStack stack, @Nonnull TileEntityBaseNetwork tile) {
        this.stack = stack;
        this.tile = tile;
        this.augmentable = (IBusAugmentable) tile;

        update(stack);
        tile.setAE2Enabled(true);
    }

    public void destroy() {
        tile.setAE2Enabled(false);
    }

    @Override
    public boolean update(@Nonnull ItemStack stack) {
        this.stack = stack;
        updateLevel();
        return true;
    }

    private void updateLevel() {
        tickRate = AppliedEnergistics2Plugin.itemAEBusAugment.getMinTickRate(stack);
        if ( tickRate < 0 )
            tickRate = Byte.MAX_VALUE;

        if ( ticks > tickRate )
            ticks = tickRate;

        energyRate = AppliedEnergistics2Plugin.itemAEBusAugment.getEnergyRate(stack);
        itemsRate = AppliedEnergistics2Plugin.itemAEBusAugment.getItemsRate(stack);
        fluidRate = AppliedEnergistics2Plugin.itemAEBusAugment.getFluidRate(stack);

        energyMode = AppliedEnergistics2Plugin.itemAEBusAugment.getEnergyMode(stack);
        itemsMode = AppliedEnergistics2Plugin.itemAEBusAugment.getItemsMode(stack);
        fluidMode = AppliedEnergistics2Plugin.itemAEBusAugment.getFluidMode(stack);
    }


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


    /* Energy */

    private int importEnergy(int budget, @Nonnull IEnergyGrid energyGrid) {
        WirelessUtils.profiler.startSection("ImportEnergy");

        double available = Math.min(tile.getEnergyStored(), tile.getMaxExtract());
        if ( available < 1 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        if ( available > budget )
            available = budget;

        available = PowerUnits.RF.convertTo(PowerUnits.AE, available);

        // Per comment, AE2 wants us to 'please don't send more then 10,000 at a time'.
        if ( available > 10000 )
            available = 10000;

        double overflow = energyGrid.injectPower(available, Actionable.SIMULATE);
        double inserted = available - overflow;
        if ( inserted == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        inserted = PowerUnits.AE.convertTo(PowerUnits.RF, inserted);
        int extracted = tile.extractEnergy((int) Math.floor(inserted), false);
        if ( extracted == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        budget -= extracted;
        energyGrid.injectPower(PowerUnits.RF.convertTo(PowerUnits.AE, extracted), Actionable.MODULATE);

        WirelessUtils.profiler.endSection();
        return budget;
    }

    private int exportEnergy(int budget, @Nonnull IEnergyGrid energyGrid) {
        WirelessUtils.profiler.startSection("ExportEnergy");

        double needed = Math.min(tile.getMaxReceive(), tile.getMaxEnergyStored() - tile.getEnergyStored());
        if ( needed < 1 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        if ( needed > budget )
            needed = budget;

        needed = PowerUnits.RF.convertTo(PowerUnits.AE, needed);
        double extracted = energyGrid.extractAEPower(needed, Actionable.SIMULATE, PowerMultiplier.ONE);
        if ( extracted == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        extracted = PowerUnits.AE.convertTo(PowerUnits.RF, extracted);
        int received = tile.receiveEnergy((int) Math.floor(extracted), false);
        if ( received == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        budget -= received;
        energyGrid.extractAEPower(PowerUnits.RF.convertTo(PowerUnits.AE, received), Actionable.MODULATE, PowerMultiplier.CONFIG);

        WirelessUtils.profiler.endSection();
        return budget;
    }


    /* Fluid */

    private IFluidStorageChannel getFluidChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    private int importFluid(int budget, @Nonnull IFluidHandler handler, @Nonnull IStorageGrid storageGrid, @Nonnull IEnergyGrid energyGrid) {
        WirelessUtils.profiler.startSection("ImportFluid");

        final IFluidTankProperties[] tanks = handler.getTankProperties();
        if ( tanks == null ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        final IFluidStorageChannel channel = getFluidChannel();
        final IMEMonitor<IAEFluidStack> ME = storageGrid.getInventory(channel);

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

            IAEFluidStack aeStack = channel.createStack(drained);
            if ( aeStack == null )
                continue;

            IAEFluidStack remaining = AEApi.instance().storage().poweredInsert(energyGrid, ME, aeStack, this, Actionable.MODULATE);

            int extracted = drained.amount;
            if ( remaining != null )
                extracted -= remaining.getStackSize();

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

    private int exportFluid(int budget, @Nonnull FluidStack[] request, @Nonnull IFluidHandler handler, @Nonnull IStorageGrid storageGrid, @Nonnull IEnergyGrid energyGrid) {
        WirelessUtils.profiler.startSection("ExportFluid");

        final IFluidStorageChannel channel = getFluidChannel();
        final IMEMonitor<IAEFluidStack> ME = storageGrid.getInventory(channel);

        for (int i = 0; i < request.length; i++) {
            FluidStack stack = request[i];
            if ( stack == null || stack.amount == 0 )
                continue;

            IAEFluidStack aeStack = channel.createStack(request[i]);
            if ( aeStack == null )
                continue;

            if ( stack.amount > budget )
                aeStack.setStackSize(budget);

            IAEFluidStack available = AEApi.instance().storage().poweredExtraction(energyGrid, ME, aeStack, this, Actionable.SIMULATE);
            if ( available == null || available.getStackSize() == 0 )
                continue;

            FluidStack insertStack = available.getFluidStack();
            int inserted = handler.fill(insertStack, true);

            if ( inserted > 0 ) {
                aeStack.setStackSize(inserted);
                AEApi.instance().storage().poweredExtraction(energyGrid, ME, aeStack, this, Actionable.MODULATE);

                budget -= inserted;
                if ( budget < 1 )
                    break;
            }
        }

        WirelessUtils.profiler.endSection();
        return budget;
    }


    /* Items */

    private IItemStorageChannel getItemChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    private int importItems(int budget, @Nonnull IItemHandler handler, @Nonnull IStorageGrid storageGrid, @Nonnull IEnergyGrid energyGrid) {
        WirelessUtils.profiler.startSection("ImportItems");

        final IItemStorageChannel channel = getItemChannel();
        final IMEMonitor<IAEItemStack> ME = storageGrid.getInventory(channel);

        final int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = handler.extractItem(i, budget, true);
            if ( stack.isEmpty() )
                continue;

            IAEItemStack aeStack = channel.createStack(stack);
            if ( aeStack == null )
                continue;

            IAEItemStack remaining = AEApi.instance().storage().poweredInsert(energyGrid, ME, aeStack, this, Actionable.MODULATE);

            int extracted = stack.getCount();
            if ( remaining != null )
                extracted -= remaining.getStackSize();

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

    private int exportItems(int budget, @Nonnull ItemStack[] request, boolean slotsMatch, @Nonnull IItemHandler handler, @Nonnull IStorageGrid storageGrid, @Nonnull IEnergyGrid energyGrid) {
        WirelessUtils.profiler.startSection("ExportItems");

        int slots = handler.getSlots();
        if ( slotsMatch )
            slots = Math.min(slots, request.length);

        if ( slots == 0 ) {
            WirelessUtils.profiler.endSection();
            return budget;
        }

        final IItemStorageChannel channel = getItemChannel();
        final IMEMonitor<IAEItemStack> ME = storageGrid.getInventory(channel);

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

            IAEItemStack aeStack = channel.createStack(requested);
            if ( aeStack == null )
                continue;

            if ( count > budget )
                count = budget;

            aeStack.setStackSize(count);

            IAEItemStack available = AEApi.instance().storage().poweredExtraction(energyGrid, ME, aeStack, this, Actionable.SIMULATE);
            if ( available == null || available.getStackSize() == 0 )
                continue;

            ItemStack insertStack = available.createItemStack();
            ItemStack remaining;
            int inserted = insertStack.getCount();

            if ( slotsMatch )
                remaining = handler.insertItem(i, insertStack, false);
            else
                remaining = InventoryHelper.insertStackIntoInventory(handler, insertStack, false);

            inserted -= remaining.getCount();

            if ( inserted > 0 ) {
                aeStack.setStackSize(inserted);
                AEApi.instance().storage().poweredExtraction(energyGrid, ME, aeStack, this, Actionable.MODULATE);

                budget -= inserted;
                if ( budget < 1 )
                    break;
            }
        }

        WirelessUtils.profiler.endSection();
        return budget;
    }


    /* Runner */

    @Nullable
    private IGrid getGrid() {
        IGridNode node = tile.getGridNode(AEPartLocation.INTERNAL);
        if ( node == null || !node.isActive() )
            return null;

        return node.getGrid();
    }

    private void execute(IBusAugmentable.TickPhase phase) {
        if ( ticks != 0 || ((energyMode == BusTransferMode.DISABLED || remainingEnergy == 0) &&
                (itemsMode == BusTransferMode.DISABLED || remainingItems == 0) &&
                (fluidMode == BusTransferMode.DISABLED || remainingFluid == 0)) )
            return;

        WirelessUtils.profiler.startSection("AEBus:Init");

        final IGrid grid = getGrid();
        if ( grid == null ) {
            WirelessUtils.profiler.endSection();
            return;
        }

        final IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        final IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        if ( storageGrid == null || energyGrid == null ) {
            WirelessUtils.profiler.endSection();
            return;
        }

        // Energy
        WirelessUtils.profiler.endStartSection("AEBus:Energy");

        if ( energyMode._import && remainingEnergy > 0 && augmentable.getBusShouldExtractEnergy(phase) )
            remainingEnergy = importEnergy(remainingEnergy, energyGrid);

        if ( energyMode._export && remainingEnergy > 0 && augmentable.getBusShouldInsertEnergy(phase) )
            remainingEnergy = exportEnergy(remainingEnergy, energyGrid);

        // Items
        WirelessUtils.profiler.endStartSection("AEBus:Items");

        if ( itemsMode._import && remainingItems > 0 ) {
            final IItemHandler itemExtractHandler = augmentable.getBusItemOutputHandler(phase);
            if ( itemExtractHandler != null )
                remainingItems = importItems(remainingItems, itemExtractHandler, storageGrid, energyGrid);
        }

        if ( itemsMode._export && remainingItems > 0 ) {
            final ItemStack[] itemRequest = augmentable.getBusItemInputRequest(phase);
            if ( itemRequest != null ) {
                final IItemHandler itemInsertHandler = augmentable.getBusItemInputHandler(phase);
                if ( itemInsertHandler != null )
                    remainingItems = exportItems(remainingItems, itemRequest, augmentable.doesBusItemInputRequestMatchSlots(phase), itemInsertHandler, storageGrid, energyGrid);
            }
        }

        // Fluids
        WirelessUtils.profiler.endStartSection("AEBus:Fluid");

        if ( fluidMode._import && remainingFluid > 0 ) {
            final IFluidHandler fluidExtractHandler = augmentable.getBusFluidOutputHandler(phase);
            if ( fluidExtractHandler != null )
                remainingFluid = importFluid(remainingFluid, fluidExtractHandler, storageGrid, energyGrid);
        }

        if ( fluidMode._export && remainingFluid > 0 ) {
            final FluidStack[] fluidRequest = augmentable.getBusFluidInputRequest(phase);
            if ( fluidRequest != null ) {
                final IFluidHandler fluidInputHandler = augmentable.getBusFluidInputHandler(phase);
                if ( fluidInputHandler != null )
                    remainingFluid = exportFluid(remainingFluid, fluidRequest, fluidInputHandler, storageGrid, energyGrid);
            }
        }

        WirelessUtils.profiler.endSection();
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


    /* IActionSource */

    @Nonnull
    public Optional<EntityPlayer> player() {
        return Optional.empty();
    }

    @Nonnull
    public Optional<IActionHost> machine() {
        return Optional.of(tile);
    }

    @Nonnull
    public <T> Optional<T> context(@Nonnull Class<T> key) {
        return Optional.empty();
    }
}

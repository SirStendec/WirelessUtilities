package com.lordmau5.wirelessutils.tile.charger;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.tile.base.IRoundRobinMachine;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseEnergy;
import com.lordmau5.wirelessutils.tile.base.Worker;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICapacityAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInventoryAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ITransferAugmentable;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class TileEntityBaseCharger extends TileEntityBaseEnergy implements IInvertAugmentable, IRoundRobinMachine, ICapacityAugmentable, ITransferAugmentable, IInventoryAugmentable, ITickable, IWorkProvider<TileEntityBaseCharger.ChargerTarget> {

    protected List<BlockPosDimension> validTargets;
    protected Worker worker;

    private boolean inverted = false;

    protected long transferLimit = -1;
    protected int capacityAugment = 0;
    protected int transferAugment = 0;

    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;
    protected long roundRobin = -1;

    private int craftingEnergy = 0;
    private int craftingTicks = 0;
    private long remainingPerTick;
    private int activeTargetsPerTick;
    private int validTargetsPerTick;

    private boolean processItems = false;

    public TileEntityBaseCharger() {
        super();
        worker = new Worker(this);
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();

        System.out.println(" Transfer Limit: " + transferLimit);
        System.out.println("     Iter. Mode: " + iterationMode);
        System.out.println("    Round Robin: " + roundRobin);
        System.out.println("       Inverted: " + inverted);
        System.out.println("Capacity Factor: " + capacityAugment);
        System.out.println("Transfer Factor: " + transferAugment);
        System.out.println("       Capacity: " + getEnergyStorage().getFullMaxEnergyStored());
        System.out.println("  Stored Energy: " + getEnergyStorage().getFullEnergyStored());
        System.out.println("    Max Extract: " + getEnergyStorage().getFullMaxExtract());

        System.out.println("  Process Items: " + processItems);
        System.out.println(" Crafting Ticks: " + craftingTicks);
        System.out.println("Crafting Energy: " + craftingEnergy);

        System.out.println(" Remaining/Tick: " + remainingPerTick);
        System.out.println("   Maximum/Tick: " + getFullMaxEnergyPerTick());
        System.out.println("  Valid Targets: " + (validTargets == null ? "NULL" : validTargets.size()));

        if ( worker != null )
            worker.debugPrint();
    }

    /* Augments */

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void setInvertAugmented(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public void setCapacityFactor(int factor) {
        capacityAugment = factor;
        getEnergyStorage().setCapacity(calculateEnergyCapacity());
    }

    @Override
    public void setTransferFactor(int factor) {
        transferAugment = factor;
        getEnergyStorage().setMaxTransfer(calculateEnergyMaxTransfer());
    }

    @Override
    public void setProcessItems(boolean process) {
        processItems = process;
    }

    /* Area Rendering */

    @Override
    public void enableRenderAreas(boolean enabled) {
        // Make sure we've run calculateTargets at least once.
        if ( enabled )
            getTargets();

        super.enableRenderAreas(enabled);
    }

    /* Work Info */

    public String getWorkUnit() {
        return "RF/t";
    }

    public long getWorkLastTick() {
        return energyPerTick;
    }

    public long getWorkMaxRate() {
        return calculateEnergyMaxTransfer();
    }

    public int getActiveTargetCount() {
        return activeTargetsPerTick;
    }

    public int getValidTargetCount() {
        return validTargetsPerTick;
    }

    /* Round Robin */

    public long getRoundRobin() {
        return roundRobin;
    }

    public void setRoundRobin(long value) {
        long max = getWorkMaxRate();
        if ( value >= max )
            value = -1;

        roundRobin = value;
        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    /* Energy Transfer */

    @Override
    public int getEnergyStored() {
        if ( isCreative && inverted )
            return 0;

        return super.getEnergyStored();
    }

    @Override
    public long getFullEnergyStored() {
        if ( isCreative && inverted )
            return 0;

        return super.getFullEnergyStored();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if ( isCreative && inverted )
            return maxReceive;

        return super.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public boolean canExtract() {
        return inverted;
    }

    @Override
    public boolean canReceive() {
        return !inverted;
    }

    @Override
    public long calculateEnergyCapacity() {
        int factor = capacityAugment == 0 ? 1 : (int) Math.floor(Math.pow(2, capacityAugment));
        long result = level.maxChargerCapacity * factor;
        if ( result < 0 )
            return Long.MAX_VALUE;

        return result;
    }

    public long getMaxPossibleTransfer() {
        if ( isCreative )
            return Long.MAX_VALUE;

        int factor = transferAugment == 0 ? 1 : (int) Math.floor(Math.pow(2, transferAugment));
        long result = level.maxChargerTransfer * factor;
        if ( result < 0 )
            return Long.MAX_VALUE;

        return result;
    }

    @Override
    public long calculateEnergyMaxTransfer() {
        long transfer = getMaxPossibleTransfer();
        if ( transferLimit >= 0 && transferLimit < transfer )
            transfer = transferLimit;

        return transfer;
    }

    public long getTransferLimit() {
        return transferLimit;
    }

    public void setTransferLimit(long limit) {
        if ( limit == transferLimit )
            return;

        transferLimit = limit;
        getEnergyStorage().setMaxTransfer(calculateEnergyMaxTransfer());

        if ( !world.isRemote )
            markChunkDirty();
    }

    private long transmitEnergy(long maxTransfer, IEnergyStorage storage, long cost) {
        if ( storage == null )
            return 0;

        if ( iterationMode == IterationMode.ROUND_ROBIN && roundRobin != -1 && maxTransfer > roundRobin )
            maxTransfer = roundRobin;

        long transfer = maxTransfer - cost;
        if ( transfer <= 0 )
            return 0;

        if ( transfer > Integer.MAX_VALUE )
            transfer = Integer.MAX_VALUE;

        long transferred;

        if ( inverted ) {
            long maxReceive = getFullMaxEnergyStored() - getFullEnergyStored() + cost;
            if ( transfer > maxReceive )
                transfer = maxReceive;

            transferred = storage.extractEnergy((int) transfer, true);
            if ( transferred > cost )
                transferred = storage.extractEnergy((int) transfer, false);

        } else
            transferred = storage.receiveEnergy((int) transfer, false);

        if ( transferred == 0 )
            return 0;

        if ( inverted ) {
            receiveEnergy((int) (transferred - cost), false);
        } else {
            transferred += cost;
            getEnergyStorage().extractEnergy(transferred, false);
        }

        return transferred;
    }

    public abstract int getEnergyCost(double distance, boolean isInterdimensional);

    /* Charger Crafting */

    public int getInsertSlot(IItemHandler handler, ItemStack stack) {
        if ( handler == null || stack.isEmpty() )
            return -1;

        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack output = handler.insertItem(i, stack, true);
            if ( output.isEmpty() )
                return i;
        }

        return -1;
    }


    /* IWorkProvider */

    public IterationMode getIterationMode() {
        return iterationMode;
    }

    public void setIterationMode(IterationMode mode) {
        if ( mode == iterationMode )
            return;

        iterationMode = mode;
        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    public Iterable<BlockPosDimension> getTargets() {
        if ( validTargets == null )
            calculateTargets();

        validTargetsPerTick = 0;
        return validTargets;
    }

    public boolean shouldProcessBlocks() {
        return false;
    }

    public boolean shouldProcessTiles() {
        return true;
    }

    public boolean shouldProcessItems() {
        return processItems;
    }

    @Override
    public ChargerTarget createInfo(@Nonnull BlockPosDimension target) {
        BlockPosDimension worker = getPosition();

        boolean interdimensional = worker.getDimension() != target.getDimension();
        double distance = worker.getDistance(target.getX(), target.getY(), target.getZ()) - 1;

        return new ChargerTarget(target, getEnergyCost(distance, interdimensional));
    }

    public ChargerTarget canWork(@Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, TileEntity tile) {
        if ( tile == null || !tile.hasCapability(CapabilityEnergy.ENERGY, target.getFacing()) )
            return null;

        validTargetsPerTick++;
        return createInfo(target);
    }

    public boolean canWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, @Nonnull TileEntity tile) {
        if ( stack.isEmpty() )
            return false;

        if ( stack.hasCapability(CapabilityEnergy.ENERGY, null) || (!inverted && ChargerRecipeManager.recipeExists(stack)) ) {
            validTargetsPerTick++;
            return true;
        }

        return false;
    }

    @Nonnull
    public WorkResult performWork(@Nonnull ChargerTarget target, @Nonnull World world, @Nonnull IBlockState state, TileEntity tile) {
        if ( tile == null )
            return WorkResult.FAILURE_REMOVE;

        IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, target.pos.getFacing());
        if ( storage == null )
            return WorkResult.FAILURE_REMOVE;

        return chargeStorage(storage, target.cost);
    }

    @Nonnull
    @Override
    public WorkResult performWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull ChargerTarget target, @Nonnull World world, @Nonnull IBlockState state, @Nonnull TileEntity tile) {
        if ( stack.isEmpty() )
            return WorkResult.FAILURE_REMOVE;

        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if ( storage != null )
            return chargeStorage(storage, target.cost);

        if ( !target.canCharge || inverted )
            return WorkResult.FAILURE_REMOVE;

        ChargerRecipeManager.ChargerRecipe recipe = ChargerRecipeManager.getRecipe(stack);
        if ( recipe == null )
            return WorkResult.FAILURE_REMOVE;

        long added = 0;
        if ( craftingEnergy < recipe.cost && craftingEnergy + getEnergyStored() >= recipe.cost ) {
            added = Math.min(recipe.cost - craftingEnergy, remainingPerTick);
            added = getEnergyStorage().extractEnergy(added, false);
            craftingEnergy += added;
            remainingPerTick -= added;

            if ( craftingEnergy < recipe.cost || remainingPerTick <= 0 ) {
                if ( added > 0 ) {
                    activeTargetsPerTick++;
                    return WorkResult.SUCCESS_STOP;
                }

                return WorkResult.FAILURE_STOP;
            }
        }

        if ( craftingTicks < recipe.ticks || remainingPerTick < target.cost )
            return WorkResult.FAILURE_CONTINUE;

        if ( inventory.extractItem(slot, 1, true).isEmpty() )
            return WorkResult.FAILURE_REMOVE;

        IItemHandler destination = (target.useSingleChest && tile instanceof TileEntityChest) ?
                tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.pos.getFacing()) :
                inventory;
        if ( destination == null )
            return WorkResult.FAILURE_REMOVE;

        ItemStack outStack = recipe.output.copy();
        int destSlot = getInsertSlot(destination, outStack);
        if ( destSlot == -1 ) {
            target.canCharge = false;
            return WorkResult.FAILURE_REMOVE;
        }

        stack.shrink(1);
        destination.insertItem(destSlot, outStack, false);
        remainingPerTick -= getEnergyStorage().extractEnergy(target.cost, false);
        craftingEnergy -= recipe.cost;
        craftingTicks = 0;
        activeTargetsPerTick++;

        if ( stack.getCount() == 0 )
            return remainingPerTick <= 0 ? WorkResult.SUCCESS_STOP_REMOVE : WorkResult.SUCCESS_REMOVE;
        else
            return remainingPerTick <= 0 ? WorkResult.SUCCESS_STOP : WorkResult.SUCCESS_CONTINUE;
    }

    public WorkResult chargeStorage(@Nonnull IEnergyStorage storage, long cost) {
        long transferred = transmitEnergy(remainingPerTick, storage, cost);
        remainingPerTick -= transferred;

        boolean cont = remainingPerTick >= 0;
        if ( inverted )
            cont &= getFullEnergyStored() < getFullMaxEnergyStored();
        else
            cont &= getFullEnergyStored() > 0;

        if ( transferred == 0 )
            return cont ? WorkResult.FAILURE_CONTINUE : WorkResult.FAILURE_STOP;
        else {
            activeTargetsPerTick++;
            return cont ? WorkResult.SUCCESS_CONTINUE : WorkResult.SUCCESS_STOP;
        }
    }


    /* NBT Save and Load */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        craftingTicks = tag.getInteger("CraftingTicks");
        craftingEnergy = tag.getInteger("CraftingEnergy");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("CraftingTicks", craftingTicks);
        tag.setInteger("CraftingEnergy", craftingEnergy);
        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        iterationMode = IterationMode.fromInt(tag.getByte("IterationMode"));
        roundRobin = tag.hasKey("RoundRobin") ? tag.getLong("RoundRobin") : -1;
        transferLimit = tag.hasKey("TransferLimit") ? tag.getLong("TransferLimit") : -1;

        getEnergyStorage().setMaxTransfer(calculateEnergyMaxTransfer());
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        super.writeExtraToNBT(tag);
        tag.setByte("IterationMode", (byte) iterationMode.ordinal());
        if ( transferLimit >= 0 )
            tag.setLong("TransferLimit", transferLimit);
        if ( roundRobin >= 0 )
            tag.setLong("RoundRobin", roundRobin);

        return tag;
    }

    @Override
    public void update() {
        if ( world.isRemote )
            return;

        worker.tickDown();

        if ( !redstoneControlOrDisable() || (inverted ? (getFullEnergyStored() == getFullMaxEnergyStored() || getMaxReceive() == 0) : (getEnergyStored() == 0 || getMaxExtract() == 0)) ) {
            activeTargetsPerTick = 0;
            energyPerTick = 0;
            setActive(false);
            return;
        }

        craftingTicks += level.craftingTPT;
        if ( craftingTicks < 0 )
            craftingTicks = 0;

        long total = getFullMaxEnergyPerTick();
        remainingPerTick = total;
        activeTargetsPerTick = 0;
        setActive(worker.performWork());
        energyPerTick = total - remainingPerTick;
    }

    /* Packets */

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addLong(transferLimit);
        payload.addByte(iterationMode.ordinal());
        payload.addLong(roundRobin);
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        setTransferLimit(payload.getLong());
        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getLong());
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addLong(transferLimit);
        payload.addByte(iterationMode.ordinal());
        payload.addLong(roundRobin);
        payload.addInt(validTargetsPerTick);
        payload.addInt(activeTargetsPerTick);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        setTransferLimit(payload.getLong());
        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getLong());
        validTargetsPerTick = payload.getInt();
        activeTargetsPerTick = payload.getInt();
    }

    /* Target Info */

    public static class ChargerTarget extends TargetInfo {

        public final int cost;
        public boolean canCharge = true;

        public ChargerTarget(BlockPosDimension target, int cost) {
            super(target);
            this.cost = cost;
        }

        @Override
        public String toString() {
            return getStringBuilder()
                    .add("cost", cost)
                    .add("canCharge", canCharge)
                    .toString();
        }
    }
}

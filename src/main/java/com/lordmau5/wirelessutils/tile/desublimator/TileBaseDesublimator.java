package com.lordmau5.wirelessutils.tile.desublimator;

import cofh.core.inventory.ComparableItemStackValidatedNBT;
import cofh.core.network.PacketBase;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.*;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICapacityAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ITransferAugmentable;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public abstract class TileBaseDesublimator extends TileEntityBaseEnergy implements IInvertAugmentable, ITransferAugmentable, ICapacityAugmentable, IUnlockableSlots, IRoundRobinMachine, ITickable, IWorkProvider<TileBaseDesublimator.DesublimatorTarget> {

    protected List<BlockPosDimension> validTargets;
    protected Worker worker;
    protected CapabilityHandler capabilityHandler;

    private ComparableItemStackValidatedNBT[] locks;

    private int transferAugment;
    private int capacityAugment;
    private int roundRobin = -1;
    private int itemRate;
    private int itemRatePerTarget;

    private byte gatherTick;

    private int itemsPerTick;
    private int remainingPerTick;
    private int activeTargetsPerTick;
    private int validTargetsPerTick;
    private int maxEnergyPerTick;

    private boolean inverted = false;
    private boolean processBlocks = false;

    public TileBaseDesublimator() {
        super();
        worker = new Worker(this);
    }

    @Override
    protected void initializeItemStackHandler(int size) {
        super.initializeItemStackHandler(size);
        locks = new ComparableItemStackValidatedNBT[size];
        capabilityHandler = new CapabilityHandler(this);
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("Capacity Augment: " + capacityAugment);
        System.out.println("     Round Robin: " + roundRobin);
        System.out.println("         Items/t: " + itemRate);
        System.out.println("   Valid Targets: " + validTargetsPerTick);
        System.out.println("  Active Targets: " + activeTargetsPerTick);
        System.out.println("Locks: " + (locks == null ? "NULL" : locks.length));
        if ( locks != null ) {
            for (int i = 0; i < locks.length; i++)
                System.out.println("  " + i + ": " + locks[i]);
        }
    }

    /* Inventory */

    public int getBufferOffset() {
        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if ( locks[slot] != null ) {
            ComparableItemStackValidatedNBT compare = new ComparableItemStackValidatedNBT(stack);
            compare.stackSize = locks[slot].stackSize;
            if ( !locks[slot].isStackEqual(compare) )
                return false;
        }

        return isSlotUnlocked(slot);
    }

    @Override
    public int getStackLimit(int slot) {
        if ( locks[slot] != null )
            return locks[slot].stackSize;

        return super.getStackLimit(slot);
    }

    /* Slot Locks */

    public ComparableItemStackValidatedNBT getLock(int slot) {
        return locks[slot];
    }

    public void clearLocks() {
        Arrays.fill(locks, null);
        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    public void setLocks() {
        if ( itemStackHandler == null )
            return;

        int slots = itemStackHandler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack slotted = itemStackHandler.getStackInSlot(i);
            if ( slotted.getItem() != Items.AIR )
                locks[i] = new ComparableItemStackValidatedNBT(slotted);
        }

        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    public void setLock(int slot, @Nonnull ItemStack stack) {
        if ( stack == null )
            stack = ItemStack.EMPTY;

        if ( stack.getItem() == Items.AIR )
            locks[slot] = null;
        else
            locks[slot] = new ComparableItemStackValidatedNBT(stack);

        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    /* Unlockable Slots */

    public boolean isSlotUnlocked(int slotIndex) {
        int offset = getBufferOffset();
        if ( slotIndex < offset )
            return false;

        return (slotIndex - offset) < capacityAugment;
    }

    /* Augments */

    @Override
    public void updateLevel() {
        super.updateLevel();

        itemRate = calculateMaxItems();
        itemRatePerTarget = calculateMaxPerTarget();
    }

    public int calculateMaxItems() {
        return calculateMaxItems(transferAugment);
    }

    public int calculateMaxItems(int factor) {
        factor = factor == 0 ? 1 : (int) Math.floor(Math.pow(2, factor));
        int result = level.maxItemsPerTick * factor;
        if ( result < 0 )
            result = Integer.MAX_VALUE;

        return result;
    }

    public int calculateMaxPerTarget() {
        int items = itemRate;
        if ( roundRobin != -1 && roundRobin < items )
            return roundRobin;

        return items;
    }

    public int calculateMaxSlots(int factor) {
        return ModConfig.desublimators.minimumSlots + (factor * ModConfig.desublimators.slotsPerTier);
    }

    @Override
    public void setTransferFactor(int factor) {
        transferAugment = factor;
        itemRate = calculateMaxItems();
        itemRatePerTarget = calculateMaxPerTarget();
    }

    @Override
    public void setCapacityFactor(int factor) {
        capacityAugment = calculateMaxSlots(factor);
    }

    public void setWorldAugmented(boolean augmented) {
        processBlocks = augmented;
    }

    public void setInvertAugmented(boolean augmented) {
        inverted = augmented;
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public boolean isValidAugment(int slot, ItemStack augment) {
        if ( !ModConfig.desublimators.allowWorldAugment && augment.getItem() == ModItems.itemWorldAugment )
            return false;

        return super.isValidAugment(slot, augment);
    }

    @Override
    public boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement) {
        if ( !super.canRemoveAugment(player, slot, augment, replacement) )
            return false;

        Item item = augment.getItem();
        if ( item == ModItems.itemCapacityAugment ) {
            if ( itemStackHandler == null )
                return true;

            int factor = 0;
            if ( !replacement.isEmpty() && replacement.getItem().equals(item) )
                factor = ModItems.itemCapacityAugment.getCapacityFactor(replacement);

            int slots = calculateMaxSlots(factor);
            if ( slots >= capacityAugment )
                return true;

            int offset = getBufferOffset();
            int totalSlots = itemStackHandler.getSlots();
            if ( slots + offset >= totalSlots )
                return true;

            for (int i = slots + offset; i < totalSlots; i++) {
                if ( !itemStackHandler.getStackInSlot(i).isEmpty() )
                    return false;
            }
        }

        return true;
    }

    /* Work Info */

    public String getWorkUnit() {
        return StringHelper.localize("info." + WirelessUtils.MODID + ".item_rate");
    }

    public long getWorkLastTick() {
        return itemsPerTick;
    }

    public long getWorkMaxRate() {
        return itemRate;
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
        int max = (int) getWorkMaxRate();
        if ( value >= max )
            value = -1;

        roundRobin = (int) value;
        itemRatePerTarget = calculateMaxPerTarget();

        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    /* Area Rendering */

    @Override
    public void enableRenderAreas(boolean enabled) {
        // Make sure we've run calculateTargets at least once.
        if ( enabled )
            getTargets();

        super.enableRenderAreas(enabled);
    }

    /* IWorkProvider */

    public DesublimatorTarget createInfo(@Nonnull BlockPosDimension target) {
        return new DesublimatorTarget(target, getEnergyCost(target));
    }

    public int getEnergyCost(@Nonnull BlockPosDimension target) {
        BlockPosDimension worker = getPosition();

        boolean interdimensional = worker.getDimension() != target.getDimension();
        double distance = worker.getDistance(target.getX(), target.getY(), target.getZ()) - 1;

        return getEnergyCost(distance, interdimensional);
    }

    public abstract int getEnergyCost(double distance, boolean interdimensional);

    public Iterable<BlockPosDimension> getTargets() {
        if ( validTargets == null )
            calculateTargets();

        validTargetsPerTick = 0;
        maxEnergyPerTick = 0;
        return validTargets;
    }

    public boolean shouldProcessBlocks() {
        return processBlocks;
    }

    public boolean shouldProcessTiles() {
        return true;
    }

    public boolean shouldProcessItems() {
        return false;
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    @Override
    public DesublimatorTarget canWork(@Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, TileEntity tile) {
        if ( tile == null ) {
            if ( !processBlocks )
                return null;

        } else if ( !InventoryHelper.hasItemHandlerCap(tile, target.getFacing()) )
            return null;

        validTargetsPerTick++;
        DesublimatorTarget out = createInfo(target);
        maxEnergyPerTick += level.baseEnergyPerOperation + out.cost;
        return out;
    }

    @Nonnull
    @Override
    public WorkResult performWork(@Nonnull DesublimatorTarget target, @Nonnull World world, @Nonnull IBlockState state, TileEntity tile) {
        if ( getEnergyStored() < level.baseEnergyPerOperation || itemStackHandler == null || itemRate == 0 )
            return WorkResult.FAILURE_STOP;

        if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        if ( tile == null ) {
            //if ( !processBlocks )
            return WorkResult.FAILURE_REMOVE;

            /*if ( gatherTick != 0 )
                return WorkResult.FAILURE_CONTINUE;

            int slots = itemStackHandler.getSlots();
            for (int i = getBufferOffset(); i < slots; i++) {
                ItemStack stack = itemStackHandler.getStackInSlot(i);
                if ( stack.isEmpty() )
                    continue;

                ItemStack move = stack.copy();
                int count = stack.getCount();
                if ( count > itemRatePerTarget ) {
                    move.setCount(itemRatePerTarget);
                    count = itemRatePerTarget;
                }

                if ( count > remainingPerTick ) {
                    move.setCount(remainingPerTick);
                    count = remainingPerTick;
                }

                CoreUtils.dropItemStackIntoWorld(move, world, new Vec3d(target.pos));
                stack.shrink(count);
                extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                activeTargetsPerTick++;
                itemsPerTick += count;
                remainingPerTick -= count;

                if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                    return WorkResult.SUCCESS_STOP;

                return WorkResult.SUCCESS_CONTINUE;
            }

            return WorkResult.FAILURE_STOP;*/

            /*List<EntityItem> entityItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(target.pos));
            if ( entityItems == null || entityItems.isEmpty() )
                return WorkResult.FAILURE_CONTINUE;

            boolean gathered = false;
            for (EntityItem item : entityItems) {
                ItemStack stack = item.getItem().copy();
                int count = stack.getCount();
                if ( count > itemRatePerTarget ) {
                    stack.setCount(itemRatePerTarget);
                    count = itemRatePerTarget;
                }

                if ( count > remainingPerTick ) {
                    stack.setCount(remainingPerTick);
                    count = remainingPerTick;
                }

                ItemStack result = InventoryHelper.insertStackIntoInventory(capabilityHandler, stack, false);
                int inserted = count - result.getCount();
                if ( inserted > 0 ) {
                    gathered = true;
                    itemsPerTick += inserted;
                    remainingPerTick -= inserted;
                    if ( result.isEmpty() )
                        item.setDead();
                    else
                        item.setItem(result);

                    if ( remainingPerTick <= 0 )
                        break;
                }
            }

            if ( gathered ) {
                activeTargetsPerTick++;
                if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                    return WorkResult.SUCCESS_STOP;

                return WorkResult.SUCCESS_CONTINUE;
            }

            return WorkResult.FAILURE_CONTINUE;*/
        }

        IItemHandler handler = InventoryHelper.getItemHandlerCap(tile, target.pos.getFacing());
        if ( handler == null )
            return WorkResult.FAILURE_REMOVE;

        IItemHandler source = capabilityHandler;
        IItemHandler dest = handler;

        if ( inverted ) {
            source = handler;
            dest = capabilityHandler;
        }

        boolean had_items = false;
        int slots = source.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = source.getStackInSlot(i);
            if ( stack.isEmpty() )
                continue;

            int count = stack.getCount();
            if ( count > itemRatePerTarget )
                count = itemRatePerTarget;
            if ( count > remainingPerTick )
                count = remainingPerTick;

            ItemStack move = source.extractItem(i, count, true);
            if ( move.isEmpty() )
                continue;

            count = move.getCount();
            had_items = true;
            ItemStack result = InventoryHelper.insertStackIntoInventory(dest, move.copy(), false);
            int inserted = count - result.getCount();
            if ( inserted > 0 ) {
                if ( inverted || !isCreative )
                    source.extractItem(i, inserted, false);

                extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                activeTargetsPerTick++;
                itemsPerTick += inserted;
                remainingPerTick -= inserted;

                if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                    return WorkResult.SUCCESS_STOP;

                return WorkResult.SUCCESS_CONTINUE;
            }
        }

        if ( !inverted && !had_items )
            return WorkResult.FAILURE_STOP;

        return WorkResult.FAILURE_REMOVE;
    }

    @Override
    public boolean canWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, @Nonnull TileEntity tile) {
        return false;
    }

    @Nonnull
    @Override
    public WorkResult performWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull DesublimatorTarget target, @Nonnull World world, @Nonnull IBlockState state, @Nonnull TileEntity tile) {
        return WorkResult.FAILURE_REMOVE;
    }

    /* Energy */

    @Override
    public long calculateEnergyCapacity() {
        return level.maxEnergyCapacity;
    }

    @Override
    public long calculateEnergyMaxTransfer() {
        return level.maxEnergyCapacity;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        energyPerTick += extracted;
        return extracted;
    }

    @Override
    public int getInfoMaxEnergyPerTick() {
        return maxEnergyPerTick;
    }

    @Override
    public long getFullMaxEnergyPerTick() {
        return getInfoMaxEnergyPerTick();
    }


    /* ITickable */

    @Override
    public void update() {
        worker.tickDown();

        gatherTick--;
        if ( gatherTick < 0 )
            gatherTick = 10;

        itemsPerTick = 0;
        energyPerTick = 0;
        activeTargetsPerTick = 0;

        if ( !redstoneControlOrDisable() ) {
            setActive(false);
            return;
        }

        remainingPerTick = itemRate;
        setActive(worker.performWork());
    }

    /* Capabilities */

    public static class CapabilityHandler implements IItemHandler {
        public final TileBaseDesublimator desublimator;

        public CapabilityHandler(TileBaseDesublimator desublimator) {
            this.desublimator = desublimator;
        }

        @Override
        public int getSlots() {
            ItemStackHandler handler = desublimator.itemStackHandler;
            if ( handler == null )
                return 0;

            return handler.getSlots() - desublimator.getBufferOffset();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStackHandler handler = desublimator.itemStackHandler;
            if ( handler == null )
                return ItemStack.EMPTY;

            slot += desublimator.getBufferOffset();
            return handler.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            ItemStackHandler handler = desublimator.itemStackHandler;
            if ( handler == null )
                return stack;

            slot += desublimator.getBufferOffset();
            return handler.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStackHandler handler = desublimator.itemStackHandler;
            if ( handler == null )
                return ItemStack.EMPTY;

            slot += desublimator.getBufferOffset();
            return handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            ItemStackHandler handler = desublimator.itemStackHandler;
            if ( handler == null )
                return 0;

            slot += desublimator.getBufferOffset();
            return handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            ItemStackHandler handler = desublimator.itemStackHandler;
            if ( handler == null )
                return false;

            slot += desublimator.getBufferOffset();
            return handler.isItemValid(slot, stack);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing from) {
        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
            return capabilityHandler != null;

        return super.hasCapability(capability, from);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ) {
            if ( capabilityHandler == null )
                return null;

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(capabilityHandler);
        }

        return super.getCapability(capability, facing);
    }

    /* NBT Read and Write */

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        roundRobin = tag.hasKey("RoundRobin") ? tag.getInteger("RoundRobin") : -1;

        NBTTagList locks = tag.getTagList("Locks", 10);
        if ( locks != null && !locks.isEmpty() ) {
            int length = Math.min(this.locks.length, locks.tagCount());
            for (int i = 0; i < length; i++) {
                NBTTagCompound itemTag = locks.getCompoundTagAt(i);
                if ( itemTag != null && !itemTag.isEmpty() )
                    this.locks[i] = new ComparableItemStackValidatedNBT(new ItemStack(itemTag));
                else
                    this.locks[i] = null;
            }
        }
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);
        if ( roundRobin >= 0 )
            tag.setInteger("RoundRobin", roundRobin);

        if ( this.locks != null ) {
            NBTTagList locks = new NBTTagList();
            for (int i = 0; i < this.locks.length; i++) {
                ComparableItemStackValidatedNBT lock = this.locks[i];
                if ( lock == null )
                    locks.appendTag(new NBTTagCompound());
                else
                    locks.appendTag(lock.toItemStack().serializeNBT());
            }

            tag.setTag("Locks", locks);
        }

        return tag;
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();

        payload.addByte(locks.length);
        for (int i = 0; i < locks.length; i++)
            payload.addItemStack(locks[i] == null ? ItemStack.EMPTY : locks[i].toItemStack());

        payload.addInt(maxEnergyPerTick);
        payload.addShort(validTargetsPerTick);
        payload.addShort(activeTargetsPerTick);
        payload.addInt(roundRobin);
        payload.addInt(itemsPerTick);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);

        int length = Math.min(payload.getByte(), locks.length);
        for (int i = 0; i < length; i++)
            setLock(i, payload.getItemStack());

        maxEnergyPerTick = payload.getInt();
        validTargetsPerTick = payload.getShort();
        activeTargetsPerTick = payload.getShort();
        setRoundRobin(payload.getInt());
        itemsPerTick = payload.getInt();
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addByte(locks.length);
        for (int i = 0; i < locks.length; i++)
            payload.addItemStack(locks[i] == null ? ItemStack.EMPTY : locks[i].toItemStack());
        payload.addInt(roundRobin);
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        int length = Math.min(payload.getByte(), locks.length);
        for (int i = 0; i < length; i++)
            setLock(i, payload.getItemStack());
        setRoundRobin(payload.getInt());
    }

    /* Target Info */

    public static class DesublimatorTarget extends TargetInfo {
        public final int cost;

        public DesublimatorTarget(BlockPosDimension pos, int cost) {
            super(pos);
            this.cost = cost;
        }

        @Override
        public String toString() {
            return getStringBuilder()
                    .add("cost", cost)
                    .toString();
        }
    }

}

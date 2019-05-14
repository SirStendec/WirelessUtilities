package com.lordmau5.wirelessutils.tile.desublimator;

import cofh.core.inventory.ComparableItemStackValidatedNBT;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.*;
import com.lordmau5.wirelessutils.tile.base.augmentable.*;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.StackHelper;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import com.lordmau5.wirelessutils.utils.crops.BehaviorManager;
import com.lordmau5.wirelessutils.utils.crops.IHarvestBehavior;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class TileBaseDesublimator extends TileEntityBaseEnergy implements
        IWorldAugmentable, IBlockAugmentable, IChunkLoadAugmentable,
        ICropAugmentable, IInvertAugmentable, ITransferAugmentable, ICapacityAugmentable,
        IUnlockableSlots, IRoundRobinMachine, ITickable, ISidedTransfer, ISidedTransferAugmentable,
        IWorkProvider<TileBaseDesublimator.DesublimatorTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;
    protected CapabilityHandler capabilityHandler;

    private ComparableItemStackValidatedNBT[] locks;
    private boolean[] emptySlots;
    private boolean[] fullSlots;
    private boolean[] plantableSlots;
    private boolean[] fertilizerSlots;
    private int plantables = 0;
    private int fertilizers = 0;
    private int full = 0;
    private int empty = 0;

    private int transferAugment;
    private int capacityAugment;
    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;
    private int roundRobin = -1;
    private int itemRatePerTarget;

    private boolean fullGather;
    private byte gatherTick;

    private int itemsPerTick;
    private int remainingBudget;
    private int costPerItem;
    private int maximumBudget;
    private int budgetPerTick;

    private int activeTargetsPerTick;
    private int validTargetsPerTick;
    private int maxEnergyPerTick;

    protected boolean chunkLoading = false;
    private boolean inverted = false;
    private boolean processDrops = false;
    private boolean processBlocks = false;
    private boolean processCrops = false;
    private boolean silkyCrops = false;
    private int fortuneCrops = 0;

    private boolean silkyBlocks = false;
    private int fortuneBlocks = 0;
    private ItemStack pickaxe = ItemStack.EMPTY;

    private boolean sideTransferAugment = false;
    private Mode[] sideTransfer;

    public TileBaseDesublimator() {
        super();
        sideTransfer = new Mode[6];
        Arrays.fill(sideTransfer, Mode.PASSIVE);
        worker = new Worker<>(this);

        updateTextures();
    }

    @Override
    protected void initializeItemStackHandler(int size) {
        super.initializeItemStackHandler(size);
        locks = new ComparableItemStackValidatedNBT[size];
        fullSlots = new boolean[size];
        emptySlots = new boolean[size];
        plantableSlots = new boolean[size];
        fertilizerSlots = new boolean[size];
        Arrays.fill(plantableSlots, false);
        Arrays.fill(fertilizerSlots, false);

        plantables = 0;
        fertilizers = 0;

        capabilityHandler = new CapabilityHandler(this);
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("   Side Transfer: " + Arrays.toString(sideTransfer));
        System.out.println("Capacity Augment: " + capacityAugment);
        System.out.println("      Iter. Mode: " + iterationMode);
        System.out.println("     Round Robin: " + roundRobin);
        System.out.println("       Item Cost: " + costPerItem);
        System.out.println("          Budget: " + remainingBudget + " (max: " + maximumBudget + ")");
        System.out.println("        Budget/t: " + budgetPerTick);
        System.out.println("   Process Drops: " + processDrops);
        System.out.println("  Process Blocks: " + processBlocks);
        System.out.println("   Process Crops: " + processCrops + (silkyCrops ? " (SILKY)" : ""));
        System.out.println("   Valid Targets: " + validTargetsPerTick);
        System.out.println("  Active Targets: " + activeTargetsPerTick);
        System.out.println("      Full Slots: " + full);
        System.out.println("     Empty Slots: " + empty);
        System.out.println("     Fertilizers: " + fertilizers);
        System.out.println("      Plantables: " + plantables);
        System.out.println("Locks: " + (locks == null ? "NULL" : locks.length));
        if ( locks != null ) {
            for (int i = 0; i < locks.length; i++)
                System.out.println("  " + i + ": " + locks[i]);
        }
    }

    /* Comparator */

    @Override
    public int calculateComparatorInput() {
        if ( capabilityHandler == null )
            return 0;

        if ( isCreative() ) {
            if ( inverted )
                return 0;
        }

        int slots = itemStackHandler.getSlots();
        int items = 0;
        int capacity = 0;

        for (int i = getBufferOffset(); i < slots; i++) {
            if ( !isSlotUnlocked(i) )
                continue;

            ItemStack stack = itemStackHandler.getStackInSlot(i);
            ComparableItemStackValidatedNBT lock = locks[i];

            int slot_max = itemStackHandler.getSlotLimit(i);
            int max = stack.getMaxStackSize();

            if ( lock != null && lock.stackSize < slot_max )
                slot_max = lock.stackSize;

            if ( slot_max < max )
                capacity += slot_max;
            else
                capacity += max;

            items += stack.getCount();
        }

        if ( items == 0 )
            return 0;

        return 1 + MathHelper.round(items * 14 / (double) capacity);
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

    @Override
    public void onContentsChanged(int slot) {
        updateItemCache(slot);
        super.onContentsChanged(slot);
    }

    @Override
    public void readInventoryFromNBT(NBTTagCompound tag) {
        super.readInventoryFromNBT(tag);
        int slots = itemStackHandler.getSlots();
        for (int i = 0; i < slots; i++)
            updateItemCache(i);
    }

    public void updateItemCache(int slot) {
        if ( slot < getBufferOffset() )
            return;

        ItemStack stack = itemStackHandler.getStackInSlot(slot);
        Item item = stack.getItem();
        Block block = Block.getBlockFromItem(item);
        boolean plantable = (item instanceof IPlantable) || (block != Blocks.AIR && block instanceof IPlantable);
        boolean fertilizer = isValidFertilizer(stack);

        ComparableItemStackValidatedNBT lock = locks[slot];
        int slot_max = itemStackHandler.getSlotLimit(slot);
        int max = stack.getMaxStackSize();

        if ( lock != null && lock.stackSize < slot_max )
            slot_max = lock.stackSize;

        if ( slot_max < max )
            max = slot_max;

        boolean slotEmpty = stack.isEmpty();
        boolean slotFull = !slotEmpty && stack.getCount() == max;

        if ( emptySlots[slot] != slotEmpty ) {
            emptySlots[slot] = slotEmpty;
            empty += slotEmpty ? 1 : -1;
        }

        if ( fullSlots[slot] != slotFull ) {
            fullSlots[slot] = slotFull;
            full += slotFull ? 1 : -1;
        }

        if ( plantableSlots[slot] != plantable ) {
            plantableSlots[slot] = plantable;
            plantables += plantable ? 1 : -1;
        }

        if ( fertilizerSlots[slot] != fertilizer ) {
            fertilizerSlots[slot] = fertilizer;
            fertilizers += fertilizer ? 1 : -1;
        }
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

        budgetPerTick = calculateBudget();
        maximumBudget = calculateMaxBudget();
        costPerItem = level.costPerItem;

        itemRatePerTarget = calculateMaxPerTarget();
    }

    public int calculateBudget() {
        return calculateBudget(transferAugment);
    }

    public int calculateBudget(int factor) {
        factor = factor == 0 ? 1 : (int) Math.floor(Math.pow(2, factor));
        int result = level.budgetPerTick * factor;
        if ( result < 0 )
            result = Integer.MAX_VALUE;

        return result;
    }

    public int calculateMaxBudget() {
        return calculateMaxBudget(transferAugment);
    }

    public int calculateMaxBudget(int factor) {
        factor = factor == 0 ? 1 : (int) Math.floor(Math.pow(2, factor));
        int result = level.maxBudget * factor;
        if ( result < 0 )
            result = Integer.MAX_VALUE;

        return result;
    }

    public int calculateMaxPerTarget() {
        int budget = costPerItem == 0 ? 0 : maximumBudget / costPerItem;
        if ( iterationMode == IterationMode.ROUND_ROBIN && roundRobin != -1 && roundRobin < maximumBudget )
            return roundRobin;

        return budget;
    }

    public int calculateMaxSlots(int factor) {
        return ModConfig.desublimators.minimumSlots + (factor * ModConfig.desublimators.slotsPerTier);
    }

    @Override
    public void setTransferFactor(int factor) {
        transferAugment = factor;
        budgetPerTick = calculateBudget();
        maximumBudget = calculateMaxBudget();
        costPerItem = level.costPerItem;
        itemRatePerTarget = calculateMaxPerTarget();
    }

    @Override
    public void setCapacityFactor(int factor) {
        capacityAugment = calculateMaxSlots(factor);
    }

    public void setCropAugmented(boolean augmented, boolean silky, int fortune) {
        processCrops = augmented;
        silkyCrops = silky;
        fortuneCrops = fortune;
    }

    @Override
    public void setBlockAugmented(boolean augmented, boolean silky, int fortune) {
        processBlocks = augmented;
        silkyBlocks = silky;
        fortuneBlocks = fortune;
        if ( augmented ) {
            pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
            if ( silky || fortune > 0 ) {
                Map<Enchantment, Integer> enchantments = new HashMap<>();
                if ( silkyBlocks )
                    enchantments.put(Enchantments.SILK_TOUCH, 1);
                if ( fortuneBlocks > 0 )
                    enchantments.put(Enchantments.FORTUNE, fortuneBlocks);

                EnchantmentHelper.setEnchantments(enchantments, pickaxe);
            }

        } else
            pickaxe = ItemStack.EMPTY;

    }

    @Override
    public boolean isBlockAugmented() {
        return processBlocks;
    }

    @Override
    public boolean isCropAugmented() {
        return processCrops;
    }

    @Override
    public boolean isWorldAugmented() {
        return processDrops;
    }

    public void setWorldAugmented(boolean augmented) {
        processDrops = augmented;
    }

    @Override
    public void setChunkLoadAugmented(boolean augmented) {
        if ( chunkLoading == augmented )
            return;

        chunkLoading = augmented;
        if ( world != null && !world.isRemote )
            calculateTargets();
    }

    @Override
    public void energyChanged() {
        if ( world != null && !world.isRemote )
            calculateTargets();
    }

    public void setInvertAugmented(boolean augmented) {
        if ( inverted == augmented )
            return;

        inverted = augmented;
        updateTextures();
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void setSidedTransferAugmented(boolean augmented) {
        if ( sideTransferAugment == augmented )
            return;

        sideTransferAugment = augmented;
        updateTextures();
    }

    @Override
    public boolean isSidedTransferAugmented() {
        return sideTransferAugment;
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

    @Override
    public String formatWorkUnit(double value) {
        String unit;
        if ( value != 0 && value < 1 ) {
            value *= 20;
            if ( value < 1 ) {
                value = 1 / value;
                unit = StringHelper.localize("info." + WirelessUtils.MODID + ".item_rate.item");
            } else
                unit = StringHelper.localize("info." + WirelessUtils.MODID + ".item_rate.second");

        } else
            unit = StringHelper.localize("info." + WirelessUtils.MODID + ".item_rate.tick");

        if ( value == Math.floor(value) )
            return String.format("%.0f %s", value, unit);

        return String.format("%.2f %s", value, unit);
    }

    public String getWorkUnit() {
        return StringHelper.localize("info." + WirelessUtils.MODID + ".item_rate");
    }

    public double getWorkLastTick() {
        return itemsPerTick;
    }

    public double getWorkMaxRate() {
        if ( costPerItem == 0 )
            return 0;

        else if ( costPerItem == 1 )
            return budgetPerTick;

        long budgetPerSecond = budgetPerTick * 20;
        return (budgetPerSecond / (double) costPerItem) / 20;
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

    public IterationMode getIterationMode() {
        return iterationMode;
    }

    @Override
    public void setIterationMode(IterationMode mode) {
        if ( mode == iterationMode )
            return;

        iterationMode = mode;
        itemRatePerTarget = calculateMaxPerTarget();

        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    public DesublimatorTarget createInfo(@Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        return new DesublimatorTarget(target, tile, entity, target == null ? 0 : getEnergyCost(target, source));
    }

    public int getEnergyCost(@Nonnull BlockPosDimension target, @Nonnull ItemStack source) {
        int cost = -1;

        if ( !source.isEmpty() ) {
            Item item = source.getItem();
            if ( item instanceof ItemBasePositionalCard )
                cost = ((ItemBasePositionalCard) item).getCost(source);
        }

        if ( cost == -1 )
            cost = getEnergyCost(target);

        return cost;
    }

    public int getEnergyCost(@Nonnull BlockPosDimension target) {
        BlockPosDimension worker = getPosition();

        boolean interdimensional = worker.getDimension() != target.getDimension();
        double distance = worker.getDistance(target.getX(), target.getY(), target.getZ()) - 1;

        return getEnergyCost(distance, interdimensional);
    }

    public abstract int getEnergyCost(double distance, boolean interdimensional);

    public Iterable<Tuple<BlockPosDimension, ItemStack>> getTargets() {
        if ( validTargets == null )
            calculateTargets();

        validTargetsPerTick = 0;
        maxEnergyPerTick = augmentDrain;
        return validTargets;
    }

    public boolean shouldProcessBlocks() {
        return processDrops || processBlocks || processCrops;
    }

    public boolean shouldProcessTiles() {
        return !shouldProcessBlocks();
    }

    public boolean shouldProcessItems() {
        return false;
    }

    public boolean shouldProcessEntities() {
        return true;
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    @Override
    public boolean canWorkBlock(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState state, @Nullable TileEntity tile) {
        if ( isBlacklisted(state) )
            return false;

        if ( processCrops ) {
            Block block = state.getBlock();
            if ( inverted ) {
                if ( world.isAirBlock(target) )
                    return false;

                IHarvestBehavior behavior = BehaviorManager.getBehavior(block);
                if ( behavior == null )
                    return false;

                if ( !behavior.canHarvest(state, world, target, silkyCrops, fortuneCrops, this) )
                    return false;

            } else {
                if ( !world.isAirBlock(target) && !(block instanceof IPlantable || block instanceof IGrowable) )
                    return false;
            }

        } else if ( processBlocks ) {
            if ( inverted == world.isAirBlock(target) )
                return false;

        } else if ( processDrops ) {
            if ( !world.isAirBlock(target) )
                return false;
        } else
            return false;

        validTargetsPerTick++;
        maxEnergyPerTick += baseEnergy + getEnergyCost(target, source);
        return true;
    }

    @Override
    public boolean canWorkTile(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nonnull TileEntity tile) {
        IBlockState state = block == null ? world.getBlockState(target) : block;
        if ( isBlacklisted(state) )
            return false;

        if ( !InventoryHelper.hasItemHandlerCap(tile, target.getFacing()) )
            return false;

        validTargetsPerTick++;
        maxEnergyPerTick += baseEnergy + getEnergyCost(target, source);
        return true;
    }

    public static boolean isValidFertilizer(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() )
            return false;

        // Bonemeal
        if ( stack.getItem().equals(Items.DYE) && stack.getMetadata() == 15 )
            return true;

        ResourceLocation name = stack.getItem().getRegistryName();
        if ( name != null && name.toString().equalsIgnoreCase("thermalfoundation:fertilizer") )
            return true;

        // Theoretical Ore Dictionary Stuff
        int id = OreDictionary.getOreID("fertilizer");
        for (int i : OreDictionary.getOreIDs(stack)) {
            if ( i == id )
                return true;
        }

        return false;
    }

    public boolean canWorkEntity(@Nonnull ItemStack source, @Nonnull World world, @Nonnull Entity entity) {
        if ( entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ) {
            validTargetsPerTick++;
            // TODO: Energy?
            maxEnergyPerTick += baseEnergy;
            return true;
        }

        return false;
    }

    public boolean canWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        return false;
    }

    @Nonnull
    public WorkResult performWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull DesublimatorTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile, @Nullable Entity entity) {
        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkBlock(@Nonnull DesublimatorTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile) {
        if ( gatherTick != 0 )
            return WorkResult.FAILURE_CONTINUE;

        if ( getEnergyStored() < baseEnergy || itemStackHandler == null || remainingBudget < costPerItem )
            return WorkResult.FAILURE_STOP_IN_PLACE;

        if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        if ( state == null )
            state = world.getBlockState(target.pos);

        if ( isBlacklisted(state) )
            return WorkResult.FAILURE_REMOVE;

        Block block = state.getBlock();
        if ( processCrops ) {
            boolean isAir = world.isAirBlock(target.pos);
            if ( !inverted && isAir ) {
                if ( plantables == 0 )
                    return WorkResult.FAILURE_CONTINUE;

                int slots = capabilityHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = capabilityHandler.getStackInSlot(i);
                    Item item = stack.getItem();
                    Block itemBlock = Block.getBlockFromItem(item);

                    if ( stack.isEmpty() || !(item instanceof IPlantable || itemBlock instanceof IPlantable) )
                        continue;

                    if ( item instanceof ItemSeeds && world.getBlockState(target.pos.down()).getBlock() == Blocks.DIRT )
                        world.setBlockState(target.pos.down(), Blocks.FARMLAND.getDefaultState());

                    FakePlayer player = WUFakePlayer.getFakePlayer(world, target.pos.up());
                    if ( isCreative )
                        stack = stack.copy();

                    player.setHeldItem(EnumHand.MAIN_HAND, stack);
                    EnumActionResult result = stack.onItemUse(player, world, target.pos.down(), EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

                    if ( result == EnumActionResult.SUCCESS ) {
                        itemsPerTick++;
                        remainingBudget -= costPerItem;
                        activeTargetsPerTick++;
                        extractEnergy(baseEnergy + target.cost, false);
                        if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                            return WorkResult.SUCCESS_STOP_REMOVE;
                        return WorkResult.SUCCESS_REMOVE;
                    } else
                        return WorkResult.FAILURE_REMOVE;
                }

            } else if ( !isAir ) {
                if ( inverted ) {
                    IHarvestBehavior behavior = BehaviorManager.getBehavior(block);
                    if ( behavior == null )
                        return WorkResult.FAILURE_REMOVE;

                    if ( !behavior.canHarvest(state, world, target.pos, silkyCrops, fortuneCrops, this) )
                        return WorkResult.FAILURE_REMOVE;

                    IHarvestBehavior.HarvestResult result = behavior.harvest(state, world, target.pos, silkyCrops, fortuneCrops, this);
                    if ( result == IHarvestBehavior.HarvestResult.FAILED )
                        return WorkResult.FAILURE_REMOVE;
                    else {
                        activeTargetsPerTick++;
                        extractEnergy(baseEnergy + target.cost, false);
                        if ( result == IHarvestBehavior.HarvestResult.HUGE_SUCCESS )
                            return WorkResult.SUCCESS_STOP_REMOVE;
                        else if ( result == IHarvestBehavior.HarvestResult.PROGRESS )
                            return WorkResult.SUCCESS_STOP_IN_PLACE;
                        else if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy ) {
                            return WorkResult.SUCCESS_STOP_REMOVE;
                        } else
                            return WorkResult.SUCCESS_REMOVE;
                    }

                } else if ( block instanceof IGrowable ) {
                    if ( fertilizers == 0 )
                        return WorkResult.FAILURE_CONTINUE;

                    int offset = getBufferOffset();
                    int slots = capabilityHandler.getSlots();
                    for (int i = 0; i < slots; i++) {
                        if ( !fertilizerSlots[i + offset] )
                            continue;

                        ItemStack stack = capabilityHandler.getStackInSlot(i);
                        if ( !isValidFertilizer(stack) )
                            continue;

                        FakePlayer player = WUFakePlayer.getFakePlayer(world, target.pos.up());
                        boolean success;

                        if ( isCreative )
                            stack = stack.copy();

                        if ( stack.getItem() == Items.DYE ) {
                            success = ItemDye.applyBonemeal(stack, world, target.pos, player, EnumHand.MAIN_HAND);

                        } else {
                            player.setHeldItem(EnumHand.MAIN_HAND, stack);
                            EnumActionResult result = stack.onItemUse(player, world, target.pos, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
                            success = result == EnumActionResult.SUCCESS;
                        }

                        if ( success ) {
                            itemsPerTick++;
                            remainingBudget -= costPerItem;
                            activeTargetsPerTick++;
                            extractEnergy(baseEnergy + target.cost, false);
                            if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                                return WorkResult.SUCCESS_STOP;
                            return WorkResult.SUCCESS_CONTINUE;
                        } else
                            return WorkResult.FAILURE_REMOVE;
                    }
                }
            }

            return WorkResult.FAILURE_REMOVE;

        } else if ( processBlocks ) {
            if ( inverted ) {
                if ( world.isAirBlock(target.pos) )
                    return WorkResult.FAILURE_REMOVE;

                WUFakePlayer player = WUFakePlayer.getFakePlayer(world, target.pos);
                player.setHeldItem(EnumHand.MAIN_HAND, pickaxe.copy());

                BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, target.pos, state, player);
                MinecraftForge.EVENT_BUS.post(event);
                if ( event.isCanceled() )
                    return WorkResult.FAILURE_REMOVE;

                if ( state.getBlockHardness(world, target.pos) < 0 )
                    return WorkResult.FAILURE_REMOVE;

                if ( block.getHarvestLevel(state) > ModConfig.augments.block.harvestLevel )
                    return WorkResult.FAILURE_REMOVE;

                NonNullList<ItemStack> drops = NonNullList.create();

                boolean silky = false;
                if ( silkyBlocks && block.canSilkHarvest(world, target.pos, state, player) ) {
                    ItemStack stack = block.getPickBlock(
                            state,
                            new RayTraceResult(new Vec3d(target.pos.getX(), target.pos.getY(), target.pos.getZ()), target.pos.getFacing()),
                            world,
                            target.pos,
                            player
                    );

                    if ( !stack.isEmpty() ) {
                        drops.add(stack);
                        silky = true;
                    }
                }

                if ( !silky )
                    block.getDrops(drops, world, target.pos, state, fortuneBlocks);

                BlockEvent.HarvestDropsEvent harvestEvent = new BlockEvent.HarvestDropsEvent(world, target.pos, state, fortuneBlocks, 1F, drops, player, silky);
                MinecraftForge.EVENT_BUS.post(harvestEvent);
                if ( harvestEvent.isCanceled() )
                    return WorkResult.FAILURE_REMOVE;

                List<ItemStack> finalDrops = harvestEvent.getDrops();
                if ( finalDrops != null && !finalDrops.isEmpty() ) {
                    if ( !canInsertAll(finalDrops) )
                        return WorkResult.FAILURE_REMOVE;

                    insertAll(finalDrops);
                }

                // Play the sound + particle of a block being broken.
                world.playEvent(null, 2001, target.pos, Block.getStateId(state));
                world.setBlockToAir(target.pos);

                activeTargetsPerTick++;
                extractEnergy(baseEnergy + target.cost, false);
                if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                    return WorkResult.SUCCESS_STOP_REMOVE;
                return WorkResult.SUCCESS_REMOVE;

            } else {
                if ( !world.isAirBlock(target.pos) )
                    return WorkResult.FAILURE_REMOVE;

                int slots = capabilityHandler.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = capabilityHandler.getStackInSlot(i);
                    if ( stack.isEmpty() )
                        continue;

                    EnumFacing facing = target.pos.getFacing();
                    if ( facing == null )
                        facing = EnumFacing.DOWN;

                    EnumFacing opposite = facing.getOpposite();
                    FakePlayer player = WUFakePlayer.getFakePlayer(world, target.pos.offset(facing, 2));
                    if ( isCreative )
                        stack = stack.copy();

                    player.setHeldItem(EnumHand.MAIN_HAND, stack);
                    player.rotationYaw = opposite.getHorizontalAngle();

                    BlockPos targetPos = target.pos;

                    Item item = stack.getItem();
                    if ( item instanceof ItemBed || item instanceof ItemDoor )
                        opposite = EnumFacing.UP;

                    else if ( item instanceof ItemFlintAndSteel ) {
                        targetPos = targetPos.offset(facing);
                    }

                    EnumActionResult result = ForgeHooks.onPlaceItemIntoWorld(stack, player, world, targetPos, opposite, 0.5F, 0.5F, 0.5F, EnumHand.MAIN_HAND);

                    if ( result == EnumActionResult.SUCCESS ) {
                        itemsPerTick++;
                        remainingBudget -= costPerItem;
                        activeTargetsPerTick++;
                        extractEnergy(baseEnergy + target.cost, false);
                        if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                            return WorkResult.SUCCESS_STOP_REMOVE;
                        return WorkResult.SUCCESS_REMOVE;
                    }
                }

                return WorkResult.FAILURE_STOP;
            }

        } else if ( !processDrops )
            return WorkResult.FAILURE_REMOVE;

        if ( inverted ) {
            if ( fullGather )
                return WorkResult.FAILURE_CONTINUE;

            List<EntityItem> entityItems;
            if ( canFullGather() ) {
                entityItems = world.getEntitiesWithinAABB(EntityItem.class, getFullGatherAABB());
                fullGather = true;

            } else
                entityItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(target.pos));

            if ( entityItems == null || entityItems.isEmpty() )
                return WorkResult.FAILURE_CONTINUE;

            boolean gathered = false;
            int budgeted = remainingBudget / costPerItem;

            long storedEnergy = getFullEnergyStored();
            HashSet<BlockPos> visitedTargets = null;
            if ( fullGather )
                visitedTargets = new HashSet<>();

            for (EntityItem item : entityItems) {
                if ( item == null )
                    continue;

                NBTTagCompound tag = item.getEntityData();
                if ( tag != null && tag.getBoolean("PreventRemoteMovement") && !tag.getBoolean("AllowMachineRemoteMovement") )
                    continue;

                int cost = 0;
                if ( fullGather ) {
                    BlockPos itemPos = item.getPosition();
                    if ( !visitedTargets.contains(itemPos) ) {
                        cost = baseEnergy + getEnergyCost(pos.getDistance(itemPos.getX(), itemPos.getY(), itemPos.getZ()), item.world != world);
                        if ( cost > storedEnergy )
                            continue;

                        visitedTargets.add(itemPos);
                    }
                }

                ItemStack stack = item.getItem().copy();
                int count = stack.getCount();
                if ( count > itemRatePerTarget ) {
                    stack.setCount(itemRatePerTarget);
                    count = itemRatePerTarget;
                }

                if ( count > budgeted ) {
                    stack.setCount(budgeted);
                    count = budgeted;
                }

                ItemStack result = InventoryHelper.insertStackIntoInventory(capabilityHandler, stack, false);
                int inserted = count - result.getCount();
                if ( inserted > 0 ) {
                    if ( cost > 0 ) {
                        extractEnergy(cost, false);
                        activeTargetsPerTick++;
                    }

                    gathered = true;
                    itemsPerTick += inserted;
                    remainingBudget -= (inserted * costPerItem);
                    budgeted -= inserted;

                    if ( result.isEmpty() )
                        item.setDead();
                    else
                        item.setItem(result);

                    if ( remainingBudget < costPerItem )
                        break;
                }
            }

            if ( gathered ) {
                if ( !fullGather ) {
                    extractEnergy(baseEnergy + target.cost, false);
                    activeTargetsPerTick++;
                }

                if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                    return WorkResult.SUCCESS_STOP;

                return WorkResult.SUCCESS_CONTINUE;
            }

            return WorkResult.FAILURE_CONTINUE;
        }

        int slots = itemStackHandler.getSlots();
        int budgeted = remainingBudget / costPerItem;

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

            if ( count > budgeted ) {
                move.setCount(budgeted);
                count = budgeted;
            }

            CoreUtils.dropItemStackIntoWorld(move, world, new Vec3d(target.pos));
            stack.shrink(count);
            itemStackHandler.setStackInSlot(i, stack);
            extractEnergy(baseEnergy + target.cost, false);
            activeTargetsPerTick++;
            itemsPerTick += count;
            remainingBudget -= (count * costPerItem);

            if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                return WorkResult.SUCCESS_STOP;

            return WorkResult.SUCCESS_CONTINUE;
        }

        return WorkResult.FAILURE_STOP;
    }

    public boolean canFullGather() {
        return false;
    }

    @Nullable
    public AxisAlignedBB getFullGatherAABB() {
        return null;
    }

    @Nonnull
    public WorkResult performWorkEntity(@Nonnull DesublimatorTarget target, @Nonnull World world, @Nonnull Entity entity) {
        if ( getEnergyStored() < baseEnergy || itemStackHandler == null || remainingBudget < costPerItem )
            return WorkResult.FAILURE_STOP_IN_PLACE;

        if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if ( handler == null )
            return WorkResult.FAILURE_REMOVE;

        IItemHandler source = capabilityHandler;
        IItemHandler dest = handler;

        if ( inverted ) {
            source = handler;
            dest = capabilityHandler;
        }

        int result = transferToInventory(source, dest, itemRatePerTarget, target.cost, true, true);
        if ( result == 2 ) {
            if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                return WorkResult.SUCCESS_STOP;

            return WorkResult.SUCCESS_CONTINUE;
        } else if ( !inverted && result == 0 )
            return WorkResult.FAILURE_STOP;

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkTile(@Nonnull DesublimatorTarget target, @Nonnull World world, @Nullable IBlockState state, @Nonnull TileEntity tile) {
        if ( getEnergyStored() < baseEnergy || itemStackHandler == null || remainingBudget < costPerItem )
            return WorkResult.FAILURE_STOP_IN_PLACE;

        if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        IItemHandler handler = InventoryHelper.getItemHandlerCap(tile, target.pos.getFacing());
        if ( handler == null )
            return WorkResult.FAILURE_REMOVE;

        IItemHandler source = capabilityHandler;
        IItemHandler dest = handler;

        if ( inverted ) {
            source = handler;
            dest = capabilityHandler;
        }

        int result = transferToInventory(source, dest, itemRatePerTarget, target.cost, true, true);
        if ( result == 2 ) {
            if ( remainingBudget < costPerItem || getEnergyStored() < baseEnergy )
                return WorkResult.SUCCESS_STOP;

            return WorkResult.SUCCESS_CONTINUE;
        } else if ( !inverted && result == 0 )
            return WorkResult.FAILURE_STOP;

        return WorkResult.FAILURE_REMOVE;
    }

    public int transferToInventory(IItemHandler source, IItemHandler dest, int maxRate, int cost, boolean drainEnergy, boolean doWorkStats) {
        int slots = source.getSlots();
        boolean had_items = false;
        int budget = costPerItem == 0 ? 0 : remainingBudget / costPerItem;
        if ( budget == 0 )
            return 0;

        for (int i = 0; i < slots; i++) {
            ItemStack stack = source.getStackInSlot(i);
            if ( stack.isEmpty() )
                continue;

            int count = stack.getCount();
            if ( count > maxRate )
                count = maxRate;
            if ( doWorkStats && count > budget )
                count = budget;

            ItemStack move = source.extractItem(i, count, true);
            if ( move.isEmpty() )
                continue;

            count = move.getCount();
            had_items = true;

            ItemStack result = InventoryHelper.insertStackIntoInventory(dest, move.copy(), false);
            int inserted = count - result.getCount();
            if ( inserted > 0 ) {
                source.extractItem(i, inserted, false);

                if ( drainEnergy )
                    extractEnergy(baseEnergy + cost, false);

                if ( doWorkStats ) {
                    activeTargetsPerTick++;
                    itemsPerTick += inserted;
                    remainingBudget -= (inserted * costPerItem);
                }

                return 2;
            }
        }

        if ( had_items )
            return 1;

        return 0;
    }

    public CapabilityHandler getCapabilityHandler() {
        return capabilityHandler;
    }

    public boolean canInsertAll(List<ItemStack> items) {
        return StackHelper.canInsertAll(capabilityHandler, items);
    }

    public void insertAll(List<ItemStack> items) {
        int inserted = StackHelper.insertAll(capabilityHandler, items);
        itemsPerTick += inserted;
        remainingBudget -= (inserted * costPerItem);
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


    /* Sided Transfer */

    public Mode getSideTransferMode(TransferSide side) {
        if ( !canSideTransfer(side) )
            return Mode.DISABLED;
        else if ( !sideTransferAugment )
            return Mode.PASSIVE;

        return sideTransfer[side.index];
    }

    public void updateTextures() {
        for (TransferSide side : TransferSide.VALUES)
            updateTexture(side);
    }

    public void updateTexture(TransferSide side) {
        Mode mode = sideTransfer[side.index];
        setProperty("machine.config." + side.name().toLowerCase(), canSideTransfer(side) ? getTextureForMode(mode, !inverted) : null);
    }

    public void setSideTransferMode(TransferSide side, Mode mode) {
        int index = side.index;
        if ( sideTransfer[index] == mode )
            return;

        sideTransfer[index] = mode;
        updateTexture(side);

        if ( !world.isRemote ) {
            sendTilePacket(Side.CLIENT);
            markChunkDirty();
        }

        callBlockUpdate();
    }

    @Override
    public void transferSide(TransferSide side) {
        if ( world == null || pos == null || world.isRemote )
            return;

        EnumFacing facing = getFacingForSide(side);
        BlockPos target = pos.offset(facing);

        TileEntity tile = world.getTileEntity(target);
        if ( tile == null )
            return;

        EnumFacing opposite = facing.getOpposite();

        // Energy
        long maxReceive = getFullMaxEnergyStored() - getFullEnergyStored();
        if ( maxReceive > getMaxReceive() )
            maxReceive = getMaxReceive();

        if ( maxReceive > 0 && tile.hasCapability(CapabilityEnergy.ENERGY, opposite) ) {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, opposite);
            if ( storage != null && storage.canExtract() ) {
                int received = storage.extractEnergy((int) maxReceive, false);
                if ( received > 0 )
                    receiveEnergy(received, false);
            }
        }

        if ( !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opposite) || costPerItem == 0 )
            return;

        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opposite);
        if ( handler == null )
            return;

        IItemHandler source = handler;
        IItemHandler dest = capabilityHandler;

        if ( inverted ) {
            source = capabilityHandler;
            dest = handler;
        }

        transferToInventory(source, dest, maximumBudget / costPerItem, 0, false, false);
    }

    /* ITickable */

    @Override
    public void update() {
        super.update();

        worker.tickDown();
        fullGather = false;

        if ( remainingBudget < maximumBudget ) {
            if ( remainingBudget < 0 )
                remainingBudget = 0;
            remainingBudget += budgetPerTick;
        }

        gatherTick--;
        if ( gatherTick < 0 )
            gatherTick = 10;

        itemsPerTick = 0;
        energyPerTick = 0;
        activeTargetsPerTick = 0;

        boolean enabled = redstoneControlOrDisable();

        int totalSlots = itemStackHandler.getSlots() - getBufferOffset();
        boolean canRun = remainingBudget >= costPerItem && (inverted ? full < totalSlots : empty < totalSlots);

        if ( sideTransferAugment && enabled && (inverted ? empty < totalSlots : full < totalSlots) )
            executeSidedTransfer();

        if ( enabled && canRun && augmentDrain > 0 )
            extractEnergy(augmentDrain, false);

        if ( !enabled || !canRun || getEnergyStored() < baseEnergy ) {
            setActive(false);
            updateTrackers();
            return;
        }

        setActive(worker.performWork());
        updateTrackers();
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

            return desublimator.capacityAugment;
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
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if ( getSideTransferMode(getSideForFacing(facing)) == Mode.DISABLED )
            return false;

        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
            return capabilityHandler != null;

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if ( getSideTransferMode(getSideForFacing(facing)) == Mode.DISABLED )
            return null;

        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ) {
            if ( capabilityHandler == null )
                return null;

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(capabilityHandler);
        }

        return super.getCapability(capability, facing);
    }

    /* NBT Read and Write */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        remainingBudget = tag.getInteger("Budget");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound out = super.writeToNBT(tag);
        out.setInteger("Budget", remainingBudget);
        return out;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        iterationMode = IterationMode.fromInt(tag.getByte("IterationMode"));
        roundRobin = tag.hasKey("RoundRobin") ? tag.getInteger("RoundRobin") : -1;
        itemRatePerTarget = calculateMaxPerTarget();

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = Mode.byIndex(tag.getByte("TransferSide" + i));

        updateTextures();

        NBTTagList locks = tag.getTagList("Locks", Constants.NBT.TAG_COMPOUND);
        if ( locks != null && !locks.isEmpty() ) {
            int length = Math.min(this.locks.length, locks.tagCount());
            for (int i = 0; i < length; i++) {
                NBTTagCompound itemTag = locks.getCompoundTagAt(i);
                if ( itemTag != null && !itemTag.isEmpty() ) {
                    ItemStack lockStack = new ItemStack(itemTag);
                    this.locks[i] = lockStack.isEmpty() ? null : new ComparableItemStackValidatedNBT(lockStack);
                } else
                    this.locks[i] = null;
            }
        }
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);
        tag.setByte("IterationMode", (byte) iterationMode.ordinal());
        if ( roundRobin >= 0 )
            tag.setInteger("RoundRobin", roundRobin);

        for (int i = 0; i < sideTransfer.length; i++)
            tag.setByte("TransferSide" + i, (byte) sideTransfer[i].index);

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
        payload.addByte(iterationMode.ordinal());
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
        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getInt());
        itemsPerTick = payload.getInt();
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addByte(locks.length);
        for (int i = 0; i < locks.length; i++)
            payload.addItemStack(locks[i] == null ? ItemStack.EMPTY : locks[i].toItemStack());
        payload.addByte(iterationMode.ordinal());
        payload.addInt(roundRobin);
        for (int i = 0; i < sideTransfer.length; i++)
            payload.addByte(sideTransfer[i].index);
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        int length = Math.min(payload.getByte(), locks.length);
        for (int i = 0; i < length; i++)
            setLock(i, payload.getItemStack());
        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getInt());

        for (int i = 0; i < sideTransfer.length; i++)
            setSideTransferMode(i, Mode.byIndex(payload.getByte()));
    }

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        for (TransferSide side : TransferSide.VALUES)
            payload.addByte(getSideTransferMode(side).index);
        payload.addBool(isInverted());
        payload.addBool(isSidedTransferAugmented());
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        for (TransferSide side : TransferSide.VALUES)
            setSideTransferMode(side, Mode.byIndex(payload.getByte()));
        setInvertAugmented(payload.getBool());
        setSidedTransferAugmented(payload.getBool());
        callBlockUpdate();
    }

    /* Target Info */

    public static boolean isBlacklisted(IBlockState state) {
        if ( state == null )
            return false;

        Block block = state.getBlock();
        int metadata = block.getMetaFromState(state);

        String name = block.getRegistryName().toString().toLowerCase();
        String name_meta = name + "@" + metadata;

        for (String key : ModConfig.desublimators.blockBlacklist)
            if ( key.equals(name) || key.equals(name_meta) )
                return true;

        return false;
    }

    public static class DesublimatorTarget extends TargetInfo {
        public final int cost;

        public DesublimatorTarget(BlockPosDimension pos, TileEntity tile, Entity entity, int cost) {
            super(pos, tile, entity);
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

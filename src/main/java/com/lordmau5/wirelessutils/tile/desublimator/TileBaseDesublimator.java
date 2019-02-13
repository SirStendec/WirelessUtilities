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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TileBaseDesublimator extends TileEntityBaseEnergy implements
        IWorldAugmentable, IBlockAugmentable, IChunkLoadAugmentable,
        ICropAugmentable, IInvertAugmentable, ITransferAugmentable, ICapacityAugmentable,
        IUnlockableSlots, IRoundRobinMachine, ITickable,
        IWorkProvider<TileBaseDesublimator.DesublimatorTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;
    protected CapabilityHandler capabilityHandler;

    private ComparableItemStackValidatedNBT[] locks;
    private boolean[] plantableSlots;
    private boolean[] fertilizerSlots;
    private int plantables = 0;
    private int fertilizers = 0;

    private int transferAugment;
    private int capacityAugment;
    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;
    private int roundRobin = -1;
    private int itemRate;
    private int itemRatePerTarget;

    private byte gatherTick;

    private int itemsPerTick;
    private int remainingPerTick;
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

    public TileBaseDesublimator() {
        super();
        worker = new Worker<>(this);
    }

    @Override
    protected void initializeItemStackHandler(int size) {
        super.initializeItemStackHandler(size);
        locks = new ComparableItemStackValidatedNBT[size];
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
        System.out.println("Capacity Augment: " + capacityAugment);
        System.out.println("      Iter. Mode: " + iterationMode);
        System.out.println("     Round Robin: " + roundRobin);
        System.out.println("         Items/t: " + itemRate);
        System.out.println("   Process Drops: " + processDrops);
        System.out.println("  Process Blocks: " + processBlocks);
        System.out.println("   Process Crops: " + processCrops + (silkyCrops ? " (SILKY)" : ""));
        System.out.println("   Valid Targets: " + validTargetsPerTick);
        System.out.println("  Active Targets: " + activeTargetsPerTick);
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
        if ( iterationMode == IterationMode.ROUND_ROBIN && roundRobin != -1 && roundRobin < items )
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

    public DesublimatorTarget createInfo(@Nonnull BlockPosDimension target, @Nonnull ItemStack source) {
        int cost = -1;

        if ( !source.isEmpty() ) {
            Item item = source.getItem();
            if ( item instanceof ItemBasePositionalCard )
                cost = ((ItemBasePositionalCard) item).getCost(source);
        }

        if ( cost == -1 )
            cost = getEnergyCost(target);

        return new DesublimatorTarget(target, cost);
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
        maxEnergyPerTick = 0;
        return validTargets;
    }

    public boolean shouldProcessBlocks() {
        return processDrops || processBlocks || processCrops;
    }

    public boolean shouldProcessTiles() {
        return !processBlocks;
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
    public DesublimatorTarget canWork(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState state, TileEntity tile) {
        if ( tile == null ) {
            if ( processCrops ) {
                Block block = state.getBlock();
                if ( inverted ) {
                    if ( world.isAirBlock(target) || BehaviorManager.getBehavior(block) == null )
                        return null;
                } else {
                    if ( !world.isAirBlock(target) && !(block instanceof IPlantable || block instanceof IGrowable) )
                        return null;
                }

            } else if ( processBlocks ) {
                if ( inverted == world.isAirBlock(target) )
                    return null;

            } else if ( processDrops ) {
                if ( !world.isAirBlock(target) )
                    return null;
            } else
                return null;

        } else if ( !InventoryHelper.hasItemHandlerCap(tile, target.getFacing()) )
            return null;

        validTargetsPerTick++;
        DesublimatorTarget out = createInfo(target, source);
        maxEnergyPerTick += level.baseEnergyPerOperation + out.cost;
        return out;
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

    @Nonnull
    @Override
    public WorkResult performWork(@Nonnull DesublimatorTarget target, @Nonnull World world, @Nonnull IBlockState state, TileEntity tile) {
        if ( getEnergyStored() < level.baseEnergyPerOperation || itemStackHandler == null || itemRate == 0 )
            return WorkResult.FAILURE_STOP;

        if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        if ( tile == null || processBlocks ) {
            if ( gatherTick != 0 )
                return WorkResult.FAILURE_CONTINUE;

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
                            remainingPerTick--;
                            activeTargetsPerTick++;
                            extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                            if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
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

                        if ( behavior.harvest(state, world, target.pos, silkyCrops, fortuneCrops, this) ) {
                            activeTargetsPerTick++;
                            extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                            if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                                return WorkResult.SUCCESS_STOP_REMOVE;
                            return WorkResult.SUCCESS_REMOVE;
                        } else
                            return WorkResult.FAILURE_REMOVE;

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
                                remainingPerTick--;
                                activeTargetsPerTick++;
                                extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                                if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                                    return WorkResult.SUCCESS_STOP;
                                return WorkResult.SUCCESS_CONTINUE;
                            } else
                                return WorkResult.FAILURE_REMOVE;
                        }
                    }
                }

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
                    extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                    if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
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

                        Item item = stack.getItem();
                        if ( item instanceof ItemBed || item instanceof ItemDoor )
                            opposite = EnumFacing.UP;

                        EnumActionResult result = ForgeHooks.onPlaceItemIntoWorld(stack, player, world, target.pos, opposite, 0.5F, 0.5F, 0.5F, EnumHand.MAIN_HAND);

                        if ( result == EnumActionResult.SUCCESS ) {
                            itemsPerTick++;
                            remainingPerTick--;
                            activeTargetsPerTick++;
                            extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                            if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                                return WorkResult.SUCCESS_STOP_REMOVE;
                            return WorkResult.SUCCESS_REMOVE;
                        }
                    }

                    return WorkResult.FAILURE_STOP;
                }

            } else if ( !processDrops )
                return WorkResult.FAILURE_REMOVE;

            if ( inverted ) {
                List<EntityItem> entityItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(target.pos));
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
                    extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                    activeTargetsPerTick++;
                    if ( remainingPerTick <= 0 || getEnergyStored() < level.baseEnergyPerOperation )
                        return WorkResult.SUCCESS_STOP;

                    return WorkResult.SUCCESS_CONTINUE;
                }

                return WorkResult.FAILURE_CONTINUE;
            }

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

            return WorkResult.FAILURE_STOP;
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
    public boolean canWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState block, @Nonnull TileEntity tile) {
        return false;
    }

    @Nonnull
    @Override
    public WorkResult performWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull DesublimatorTarget target, @Nonnull World world, @Nonnull IBlockState state, @Nonnull TileEntity tile) {
        return WorkResult.FAILURE_REMOVE;
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
        remainingPerTick -= inserted;
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
            updateTrackers();
            return;
        }

        remainingPerTick = itemRate;
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
        iterationMode = IterationMode.fromInt(tag.getByte("IterationMode"));
        roundRobin = tag.hasKey("RoundRobin") ? tag.getInteger("RoundRobin") : -1;
        itemRatePerTarget = calculateMaxPerTarget();

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
        tag.setByte("IterationMode", (byte) iterationMode.ordinal());
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

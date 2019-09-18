package com.lordmau5.wirelessutils.tile.condenser;

import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.packet.PacketParticleLine;
import com.lordmau5.wirelessutils.tile.base.IConfigurableWorldTickRate;
import com.lordmau5.wirelessutils.tile.base.IRoundRobinMachine;
import com.lordmau5.wirelessutils.tile.base.ISidedTransfer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseEnergy;
import com.lordmau5.wirelessutils.tile.base.Worker;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICapacityAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IChunkLoadAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFluidGenAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInventoryAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ITransferAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IWorldAugmentable;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.FluidTank;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.crafting.IWUCraftingMachine;
import com.lordmau5.wirelessutils.utils.crafting.IWURecipe;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public abstract class TileEntityBaseCondenser extends TileEntityBaseEnergy implements
        IConfigurableWorldTickRate, IWUCraftingMachine,
        IChunkLoadAugmentable, IRoundRobinMachine, IWorldAugmentable, ITransferAugmentable,
        ICapacityAugmentable, IInventoryAugmentable, IInvertAugmentable, ITickable,
        IFluidGenAugmentable, ISidedTransfer, ISidedTransferAugmentable,
        IWorkProvider<TileEntityBaseCondenser.CondenserTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;

    protected final FluidTank tank;
    protected final IFluidHandler fluidHandler;
    protected final IFluidHandler internalHandler;

    private boolean wantsTargets = false;

    protected boolean locked;
    protected FluidStack lockStack;

    private int capacityAugment;
    private int transferAugment;
    private boolean inverted;
    protected boolean chunkLoading;

    private int fluidRate;
    private int fluidMaxRate;
    private boolean burstTick = false;
    private int burstAmount = -1;
    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;
    private int roundRobin = -1;

    private boolean didCraftingTicks = false;
    private CondenserRecipeManager.CondenserRecipe lastRecipe = null;
    private int craftingSlot = -1;
    private FluidStack craftingFluid = null;
    private int craftingTicks = 0;
    private byte gatherTick = 0;
    private int gatherTickRate = -1;

    private int remainingPerTick;
    private int fluidPerTick;
    private int activeTargetsPerTick;
    private int validTargetsPerTick;
    private int maxEnergyPerTick;
    private boolean processBlocks = false;
    private boolean processItems = false;
    private boolean processEntities = false;

    private boolean fluidGen = false;
    private FluidStack fluidGenStack = null;
    private int fluidGenCost = 0;

    private boolean sideTransferAugment = false;
    private final Mode[] sideTransfer;
    private final boolean[] sideIsCached;
    private final TileEntity[] sideCache;


    public TileEntityBaseCondenser() {
        super();
        sideTransfer = new Mode[6];
        sideIsCached = new boolean[6];
        sideCache = new TileEntity[6];
        Arrays.fill(sideTransfer, Mode.PASSIVE);
        Arrays.fill(sideIsCached, false);
        Arrays.fill(sideCache, null);

        worker = new Worker<>(this);

        tank = new FluidTank(calculateFluidCapacity());
        fluidMaxRate = calculateMaxFluidRate();
        fluidRate = calculateFluidRate();

        updateTextures();

        internalHandler = new IFluidHandler() {
            @Override
            public IFluidTankProperties[] getTankProperties() {
                return new IFluidTankProperties[]{
                        new FluidTankProperties(tank.getFluid(), tank.getCapacity(), inverted, !inverted)
                };
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if ( !inverted )
                    return 0;

                if ( resource == null || resource.getFluid() == null )
                    return 0;

                if ( locked && lockStack != null && !lockStack.isFluidEqual(resource) )
                    return 0;

                int amount = resource.amount;

                if ( burstAmount != -1 ) {
                    if ( amount > burstAmount )
                        amount = burstAmount;
                } else {
                    if ( amount > remainingPerTick )
                        amount = remainingPerTick;

                    if ( amount > fluidRate )
                        amount = fluidRate;
                }

                if ( amount == 0 )
                    return 0;

                resource = resource.copy();
                resource.amount = amount;

                int filled = tank.fill(resource, doFill);
                if ( filled > 0 && doFill ) {
                    if ( burstAmount != -1 ) {
                        burstAmount = -1;
                        burstTick = false;
                    }

                    markChunkDirty();
                    if ( locked && lockStack == null )
                        setLocked(resource);

                    if ( filled == tank.getFluidAmount() )
                        updateFluidGen();

                    remainingPerTick -= filled;
                }

                return filled;
            }

            @Nullable
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                if ( inverted )
                    return null;

                if ( resource == null || !resource.isFluidEqual(tank.getFluid()) )
                    return null;

                return drain(resource.amount, doDrain);
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                if ( inverted )
                    return null;

                if ( burstAmount != -1 ) {
                    if ( maxDrain > burstAmount )
                        maxDrain = burstAmount;

                } else {
                    if ( maxDrain > remainingPerTick )
                        maxDrain = remainingPerTick;

                    if ( maxDrain > fluidRate )
                        maxDrain = fluidRate;
                }

                if ( maxDrain == 0 )
                    return null;

                FluidStack output = tank.drain(maxDrain, doDrain);
                if ( output != null && doDrain ) {
                    if ( burstAmount != -1 ) {
                        burstAmount = -1;
                        burstTick = false;
                    }

                    markChunkDirty();
                    FluidStack tankFluid = tank.getFluid();
                    if ( lockStack != null && !tank.isLocked() && (tankFluid == null || tankFluid.amount == 0) )
                        tank.setLock(lockStack.getFluid());

                    if ( tankFluid == null || tankFluid.amount == 0 )
                        updateFluidGen();

                    remainingPerTick -= output.amount;
                }

                return output;
            }
        };

        fluidHandler = new IFluidHandler() {
            @Override
            public IFluidTankProperties[] getTankProperties() {
                return new IFluidTankProperties[]{
                        new FluidTankProperties(tank.getFluid(), tank.getCapacity(), !inverted, inverted)
                };
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if ( inverted )
                    return 0;

                if ( locked ) {
                    if ( lockStack != null && !lockStack.isFluidEqual(resource) )
                        return 0;

                    else if ( lockStack == null && resource != null && resource.getFluid() != null )
                        setLocked(resource);
                }

                int out = tank.fill(resource, doFill);
                if ( out > 0 && doFill ) {
                    markChunkDirty();

                    if ( out == tank.getFluidAmount() )
                        updateFluidGen();
                }

                return out;
            }

            @Nullable
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                if ( !inverted )
                    return null;

                if ( resource == null )
                    return null;

                return drain(resource.amount, doDrain);
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                if ( !inverted )
                    return null;

                FluidStack out = tank.drain(maxDrain, doDrain);
                if ( out != null && doDrain ) {
                    markChunkDirty();

                    if ( tank.getFluidAmount() == 0 )
                        updateFluidGen();
                }

                return out;
            }
        };
    }

    /* Comparator */

    @Override
    public int calculateComparatorInput() {
        if ( isCreative() ) {
            if ( inverted )
                return 0;
            return 15;
        }

        if ( tank == null )
            return 0;

        int fluid = tank.getFluidAmount();
        if ( fluid == 0 )
            return 0;

        return 1 + MathHelper.round(fluid * 14 / (double) tank.getCapacity());
    }

    /* Lighting */

    private int lightValue;

    @Override
    public int getLightValue() {
        FluidStack stack = getTankFluid();
        if ( stack == null || stack.amount == 0 )
            return 0;

        Fluid fluid = stack.getFluid();
        if ( fluid == null )
            return 0;

        return MathHelper.round(fluid.getLuminosity(stack) / 2.0D);
    }

    @Override
    public void runTrackers() {
        super.runTrackers();

        int lightValue = getLightValue();
        if ( lightValue != this.lightValue ) {
            this.lightValue = lightValue;
            updateLighting();
            sendTilePacket(Side.CLIENT);
        }
    }

    /* Debugging */

    public String debugPrintStack(FluidStack stack) {
        if ( stack == null )
            return "NULL";

        return stack.amount + "x" + stack.getFluid().getName();
    }

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("         Locked: " + locked + " [" + debugPrintStack(lockStack) + "]");
        System.out.println("       Inverted: " + inverted);
        System.out.println("     Iter. Mode: " + iterationMode);
        System.out.println("    Round Robin: " + roundRobin);
        System.out.println("    Gather Tick: " + gatherTick);
        System.out.println("World Tick Rate: " + gatherTickRate);
        System.out.println("ActualTick Rate: " + getActualWorldTickRate());
        System.out.println(" Max Fluid Rate: " + fluidMaxRate);
        System.out.println(" Fluid per Tick: " + fluidPerTick);
        System.out.println("  Valid Targets: " + validTargetsPerTick);
        System.out.println(" Active Targets: " + activeTargetsPerTick);
        System.out.println("   Burst Amount: " + burstAmount);
        System.out.println(" Crafting Ticks: " + craftingTicks);
        System.out.println(" Crafting Fluid: " + debugPrintStack(craftingFluid));
        System.out.println("Crafting Recipe: " + lastRecipe);
        System.out.println("  Crafting Slot: " + craftingSlot);
        System.out.println("       Capacity: " + tank.getCapacity());
        System.out.println("       Contents: " + debugPrintStack(tank.getFluid()));
        System.out.println("    Light Level: " + lightValue);
        System.out.println("      Fluid Gen: " + fluidGen);
        System.out.println("Fluid Gen Fluid: " + fluidGenStack);
        System.out.println(" Fluid Gen Cost: " + fluidGenCost);
        System.out.println("  Side Transfer: " + Arrays.toString(sideTransfer));
    }

    /* Tank Stuff */

    @Override
    public void updateLevel() {
        super.updateLevel();
        tank.setInfinite(isCreative && !inverted);
        tank.setCapacity(calculateFluidCapacity());
        fluidMaxRate = calculateMaxFluidRate();
        fluidRate = calculateFluidRate();
    }

    @Override
    public FluidTankCore getTank() {
        return tank;
    }

    @Override
    public FluidStack getTankFluid() {
        return tank.getFluid();
    }

    public boolean isLocked() {
        return locked;
    }

    public FluidStack getLockStack() {
        return lockStack;
    }

    public void setLocked(FluidStack stack) {
        if ( stack == null ) {
            setLocked(false);
            return;
        }

        if ( locked && lockStack != null && lockStack.isFluidEqual(stack) )
            return;

        this.locked = false;
        lockStack = new FluidStack(stack, 0);
        setLocked(true);
    }

    public void setLocked(boolean locked) {
        if ( locked == this.locked )
            return;

        this.locked = locked;
        tank.setLocked(locked);

        if ( locked ) {
            FluidStack tankFluid = tank.getFluid();
            if ( lockStack != null && isCreative && !inverted ) {
                tank.setFluid(new FluidStack(lockStack, tank.getCapacity()));
                tank.setLocked(true);
            } else if ( lockStack == null && tankFluid != null ) {
                lockStack = new FluidStack(tankFluid, 0);
                tank.setLocked(true);
            } else if ( lockStack != null && tankFluid == null ) {
                tank.setLock(lockStack.getFluid());
            } else
                tank.setLocked(false);
        } else {
            lockStack = null;
            tank.setLocked(false);
        }

        updateFluidGen();

        if ( world != null && !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    /* Capacity */

    public int calculateFluidCapacity() {
        return calculateFluidCapacity(capacityAugment);
    }

    public int calculateFluidCapacity(int factor) {
        factor = factor == 0 ? 1 : (int) Math.floor(Math.pow(2, factor));
        int result = level.maxCondenserCapacity * factor;
        if ( result < 0 )
            return Integer.MAX_VALUE;

        return result;
    }

    @Override
    public void setCapacityFactor(int factor) {
        capacityAugment = factor;
        tank.setCapacity(calculateFluidCapacity());
    }

    /* Work Info */

    public String getWorkUnit() {
        return "Mb/t";
    }

    public String formatWorkUnit(double value) {
        long val = (long) value;
        if ( val < 1000000 || GuiScreen.isShiftKeyDown() )
            return String.format("%s %s", StringHelper.formatNumber(val), getWorkUnit());

        return TextHelpers.getScaledNumber(val / 1000, "B/t", true);
    }

    public double getWorkLastTick() {
        return fluidPerTick;
    }

    public boolean hasSustainedRate() {
        return false;
    }

    public double getWorkMaxRate() {
        return fluidMaxRate;
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
        if ( value > Integer.MAX_VALUE )
            value = Integer.MAX_VALUE;

        int max = (int) getWorkMaxRate();
        if ( value >= max )
            value = -1;

        roundRobin = (int) value;
        fluidRate = calculateFluidRate();

        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    /* Rate */

    public int calculateFluidRate() {
        return calculateFluidRate(transferAugment);
    }

    public int calculateFluidRate(int factor) {
        int rate = calculateMaxFluidRate(factor);
        if ( iterationMode != IterationMode.ROUND_ROBIN || roundRobin == -1 || roundRobin > rate )
            return rate;

        return roundRobin;
    }

    public int calculateMaxFluidRate() {
        return calculateMaxFluidRate(transferAugment);
    }

    public int calculateMaxFluidRate(int factor) {
        factor = factor == 0 ? 1 : (int) Math.floor(Math.pow(2, factor));
        int result = level.maxCondenserTransfer * factor;
        if ( result < 0 )
            return Integer.MAX_VALUE;

        return result;
    }

    @Override
    public void setTransferFactor(int factor) {
        transferAugment = factor;
        fluidMaxRate = calculateMaxFluidRate();
        fluidRate = calculateFluidRate();
    }

    /* Augments */

    @Override
    public boolean isValidAugment(int slot, ItemStack augment) {
        if ( !ModConfig.condensers.allowWorldAugment && augment.getItem() == ModItems.itemWorldAugment )
            return false;

        return super.isValidAugment(slot, augment);
    }

    @Override
    public boolean isWorldAugmented() {
        return processBlocks;
    }

    @Override
    public void setWorldAugmented(boolean augmented) {
        processBlocks = augmented;
    }

    @Override
    public void setProcessItems(boolean process) {
        processItems = process;
    }

    @Override
    public boolean isInverted() {
        return inverted;
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
    public void setFluidGenAugmented(boolean enabled, FluidStack fluidStack, int energy) {
        int oldCost = fluidGenCost;

        if ( !enabled ) {
            fluidGenStack = null;
            fluidGenCost = 0;

        } else {
            fluidGenStack = fluidStack;
            fluidGenCost = energy;
        }

        updateFluidGen();
    }

    public void updateFluidGen() {
        if ( fluidGenStack == null || tank == null ) {
            fluidGen = false;
            return;
        }

        if ( locked && lockStack != null && !lockStack.isFluidEqual(fluidGenStack) ) {
            fluidGen = false;
            return;
        }

        FluidStack fluid = tank.getFluid();
        if ( fluid != null && !fluid.isFluidEqual(fluidGenStack) ) {
            fluidGen = false;
            return;
        }

        fluidGen = true;
    }

    @Override
    public void energyChanged() {
        if ( world != null && !world.isRemote )
            calculateTargets();
    }

    @Override
    public void setInvertAugmented(boolean inverted) {
        if ( this.inverted == inverted )
            return;

        this.inverted = inverted;
        updateTextures();

        if ( !isCreative )
            return;

        tank.setInfinite(!inverted);
        if ( inverted )
            tank.setFluid(null);
        else if ( lockStack != null ) {
            locked = false;
            setLocked(true);
        }
    }

    @Override
    public void setSidedTransferAugmented(boolean augmented) {
        if ( sideTransferAugment == augmented )
            return;

        sideTransferAugment = augmented;
        updateTextures();
        callNeighborStateChange();
    }

    @Override
    public boolean isSidedTransferAugmented() {
        return sideTransferAugment;
    }

    @Override
    public boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement) {
        if ( !super.canRemoveAugment(player, slot, augment, replacement) )
            return false;

        Item item = augment.getItem();
        if ( item == ModItems.itemCapacityAugment ) {
            int factor = 0;
            if ( !replacement.isEmpty() && replacement.getItem().equals(item) )
                factor = ModItems.itemCapacityAugment.getCapacityFactor(replacement);

            int capacity = calculateFluidCapacity(factor);
            if ( capacity < tank.getFluidAmount() )
                return false;
        }

        return true;
    }

    /* Area Rendering */

    @Override
    public void enableRenderAreas(boolean enabled) {
        // Make sure we've run calculateTargets at least once.
        if ( enabled )
            getTargets();

        super.enableRenderAreas(enabled);
    }

    /* Condenser Crafting */

    @Nullable
    public IWURecipe getCurrentRecipe() {
        return lastRecipe;
    }

    public float getCraftingProgress() {
        if ( lastRecipe == null )
            return 0;

        return Math.min(
                (float) craftingTicks / lastRecipe.ticks,
                craftingFluid == null ? 0 : ((float) craftingFluid.amount / lastRecipe.fluid.amount)
        );
    }

    public boolean canCraft() {
        return processItems && !CondenserRecipeManager.getAllRecipes().isEmpty();
    }

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

    @Override
    public void setIterationMode(IterationMode mode) {
        if ( mode == iterationMode )
            return;

        iterationMode = mode;
        fluidRate = calculateFluidRate();

        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    public CondenserTarget createInfo(@Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile, @Nullable Entity entity) {
        // TODO: Entity target cost.
        int cost = target == null ? 0 : (int) (getEnergyCost(target, source) * augmentMultiplier);
        return new CondenserTarget(target, source, tile, entity, cost);
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
        if ( validTargets == null ) {
            tickActive();
            calculateTargets();
        }

        if ( world == null || !world.isRemote ) {
            validTargetsPerTick = 0;
            maxEnergyPerTick = 0;
            lastRecipe = null;
            craftingSlot = -1;
        }

        return validTargets;
    }

    @Override
    public void onInactive() {
        super.onInactive();
        worker.clearTargetCache();
        validTargets = null;
        lastRecipe = null;
        craftingSlot = -1;
    }

    public boolean shouldProcessBlocks() {
        return processBlocks;
    }

    public boolean shouldProcessTiles() {
        return true;
    }

    public boolean shouldProcessItems() {
        return processItems;
    }

    public boolean shouldProcessEntities() {
        return true;
        //return processEntities;
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    public boolean canWorkBlock(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState block, @Nullable TileEntity tile) {
        if ( fluidRate == 0 )
            return false;

        Material material = block.getMaterial();
        if ( inverted ) {
            if ( !material.isLiquid() )
                return false;

            IFluidHandler handler = getHandlerForBlock(world, target, block);
            if ( handler == null )
                return false;

            IFluidTankProperties[] properties = handler.getTankProperties();
            if ( properties == null || properties.length != 1 || !properties[0].canDrain() )
                return false;

            FluidStack stack = tank.getFluid();
            if ( stack != null && stack.amount > tank.getCapacity() - Fluid.BUCKET_VOLUME )
                return false;

            FluidStack other = properties[0].getContents();
            if ( other == null || other.amount < Fluid.BUCKET_VOLUME || (stack != null && !stack.isFluidEqual(other)) )
                return false;

        } else {
            if ( !world.isAirBlock(target) && material.isSolid() && !block.getBlock().isReplaceable(world, pos) )
                return false;

            FluidStack stack = tank.getFluid();
            if ( stack == null || stack.amount < Fluid.BUCKET_VOLUME || stack.getFluid() == null || !stack.getFluid().canBePlacedInWorld() )
                return false;

            if ( material.isLiquid() ) {
                IFluidHandler handler = getHandlerForBlock(world, target, block);
                if ( handler != null ) {
                    IFluidTankProperties[] properties = handler.getTankProperties();
                    if ( properties == null || properties.length != 1 || !properties[0].canFill() )
                        return false;

                    FluidStack other = properties[0].getContents();
                    if ( other != null && (other.amount >= properties[0].getCapacity() || !other.isFluidEqual(stack)) )
                        return false;
                }
            }
        }

        validTargetsPerTick++;
        int cost = baseEnergy + (int) (getEnergyCost(target, source) * augmentMultiplier);
        if ( cost > maxEnergyPerTick )
            maxEnergyPerTick = cost;

        return true;
    }

    public boolean canWorkTile(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World
            world, @Nullable IBlockState block, @Nonnull TileEntity tile) {
        if ( !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.getFacing()) )
            return false;

        validTargetsPerTick++;
        int cost = baseEnergy + (int) (getEnergyCost(target, source) * augmentMultiplier);
        if ( cost > maxEnergyPerTick )
            maxEnergyPerTick = cost;

        return true;
    }

    public boolean canWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory,
                               @Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world,
                               @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        if ( stack.isEmpty() )
            return false;

        int cost = target == null ? 0 : (int) (getEnergyCost(target) * augmentMultiplier);

        if ( stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if ( handler != null ) {
                FluidStack filled = getTankFluid();
                IFluidTankProperties[] props = handler.getTankProperties();
                if ( props != null )
                    for (IFluidTankProperties prop : props) {
                        boolean canUse;
                        FluidStack fs = prop.getContents();
                        if ( filled != null && fs != null && !filled.isFluidEqual(fs) )
                            canUse = false;
                        else if ( inverted )
                            canUse = (filled != null ? prop.canDrainFluidType(filled) : prop.canDrain());
                        else
                            canUse = (filled != null ? prop.canFillFluidType(filled) : prop.canFill());

                        if ( canUse ) {
                            validTargetsPerTick++;
                            int tCost = baseEnergy + cost;
                            if ( tCost > maxEnergyPerTick )
                                maxEnergyPerTick = tCost;
                            return true;
                        }
                    }
            }
        }

        if ( !inverted && FluidHelper.isFillableEmptyContainer(stack) ) {
            validTargetsPerTick++;
            int tCost = baseEnergy + cost;
            if ( tCost > maxEnergyPerTick )
                maxEnergyPerTick = tCost;
            return true;
        }

        if ( inverted )
            return false;

        CondenserRecipeManager.CondenserRecipe recipe = CondenserRecipeManager.getRecipe(getTankFluid(), stack);
        if ( recipe != null ) {
            validTargetsPerTick++;
            int tCost = baseEnergy + cost + (int) (recipe.cost * augmentMultiplier);
            if ( tCost > maxEnergyPerTick )
                maxEnergyPerTick = tCost;
            return true;
        }

        return false;
    }

    @Override
    public boolean canWorkEntity(@Nonnull ItemStack source, @Nonnull World world, @Nonnull Entity entity) {
        if ( entity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) ) {
            validTargetsPerTick++;
            // TODO: Entity energy calculations
            if ( baseEnergy > maxEnergyPerTick )
                maxEnergyPerTick = baseEnergy;
            return true;
        }

        return false;
    }

    public static IFluidHandler getHandlerForBlock(@Nonnull World world, @Nonnull BlockPos
            pos, @Nonnull IBlockState state) {
        Block block = state.getBlock();
        if ( block instanceof IFluidBlock )
            return new FluidBlockWrapper((IFluidBlock) block, world, pos);
        else if ( block instanceof BlockLiquid )
            return new BlockLiquidWrapper((BlockLiquid) block, world, pos);

        return null;
    }

    @Nonnull
    public WorkResult performWorkBlock(@Nonnull CondenserTarget target, @Nonnull World world, @Nullable IBlockState
            state, @Nullable TileEntity tile) {
        if ( gatherTick != 0 )
            return WorkResult.FAILURE_CONTINUE;

        if ( getEnergyStored() < baseEnergy )
            return WorkResult.FAILURE_STOP_IN_PLACE;
        else if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack stack = tank.getFluid();
        if ( inverted ) {
            if ( stack != null && stack.amount >= tank.getCapacity() )
                return WorkResult.FAILURE_STOP_IN_PLACE;
        } else {
            if ( stack == null || stack.amount <= 0 )
                return WorkResult.FAILURE_STOP_IN_PLACE;
        }

        if ( state == null )
            state = world.getBlockState(target.pos);

        IFluidHandler handler = getHandlerForBlock(world, target.pos, state);
        if ( handler != null ) {
            IFluidTankProperties[] properties = handler.getTankProperties();
            if ( properties == null || properties.length != 1 )
                return WorkResult.FAILURE_REMOVE;

            FluidStack contained = properties[0].getContents();

            if ( inverted ) {
                if ( contained == null || contained.amount < Fluid.BUCKET_VOLUME )
                    return WorkResult.FAILURE_REMOVE;

                if ( stack != null && !contained.isFluidEqual(stack) )
                    return WorkResult.FAILURE_REMOVE;

                if ( burstAmount < Fluid.BUCKET_VOLUME ) {
                    boolean justBurst = false;
                    if ( !burstTick ) {
                        burstAmount = burstAmount == -1 ? remainingPerTick : burstAmount + remainingPerTick;
                        burstTick = true;
                        justBurst = true;
                    }
                    if ( burstAmount > Fluid.BUCKET_VOLUME )
                        burstAmount = Fluid.BUCKET_VOLUME;
                    else if ( burstAmount < Fluid.BUCKET_VOLUME ) {
                        // Make us stick on gather tick until we have enough
                        // assuming this was the burst tick.
                        if ( justBurst ) {
                            activeTargetsPerTick++;
                            gatherTick++;
                        }
                        return WorkResult.FAILURE_STOP_IN_PLACE;
                    }
                }

                WorkResult result = fillContainer(handler, target.cost);
                switch (result) {
                    case FAILURE_CONTINUE:
                        return WorkResult.FAILURE_REMOVE;
                    case SUCCESS_CONTINUE:
                        return WorkResult.SUCCESS_REMOVE;
                    case SUCCESS_STOP:
                        return WorkResult.SUCCESS_STOP_REMOVE;
                    case FAILURE_STOP:
                        return WorkResult.FAILURE_STOP_REMOVE;
                    default:
                        return result;
                }
            }

            if ( contained != null && contained.amount == properties[0].getCapacity() )
                return WorkResult.FAILURE_REMOVE;
        }

        if ( inverted )
            return WorkResult.FAILURE_REMOVE;

        if ( burstAmount < Fluid.BUCKET_VOLUME ) {
            boolean justBurst = false;
            if ( !burstTick ) {
                burstAmount = burstAmount == -1 ? remainingPerTick : burstAmount + remainingPerTick;
                burstTick = true;
                justBurst = true;
            }
            if ( burstAmount > Fluid.BUCKET_VOLUME )
                burstAmount = Fluid.BUCKET_VOLUME;
            else if ( burstAmount < Fluid.BUCKET_VOLUME ) {
                // Make us stick on gather tick until we have enough.
                if ( justBurst ) {
                    activeTargetsPerTick++;
                    gatherTick++;
                }
                return WorkResult.FAILURE_STOP_IN_PLACE;
            }
        }

        if ( FluidUtil.tryPlaceFluid(null, world, target.pos, internalHandler, tank.getFluid()) ) {
            activeTargetsPerTick++;
            extractEnergy(baseEnergy + target.cost, false);

            if ( remainingPerTick > 0 )
                return WorkResult.SUCCESS_REMOVE;

            return WorkResult.SUCCESS_STOP_REMOVE;
        }

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkTile(@Nonnull CondenserTarget target, @Nonnull World world, @Nullable IBlockState
            state, @Nonnull TileEntity tile) {
        if ( getEnergyStored() < baseEnergy )
            return WorkResult.FAILURE_STOP_IN_PLACE;
        else if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack stack = tank.getFluid();
        if ( inverted ) {
            if ( stack != null && stack.amount >= tank.getCapacity() )
                return WorkResult.FAILURE_STOP_IN_PLACE;
        } else {
            if ( stack == null || stack.amount <= 0 )
                return WorkResult.FAILURE_STOP_IN_PLACE;
        }

        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.pos.getFacing());
        if ( handler != null )
            return fillContainer(handler, target.cost);

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    @Override
    public WorkResult performWorkEntity(@Nonnull CondenserTarget target, @Nonnull World world, @Nonnull Entity
            entity) {
        if ( getEnergyStored() < baseEnergy )
            return WorkResult.FAILURE_STOP_IN_PLACE;
        else if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack stack = tank.getFluid();
        if ( inverted ) {
            if ( stack != null && stack.amount >= tank.getCapacity() )
                return WorkResult.FAILURE_STOP_IN_PLACE;
        } else {
            if ( stack == null || stack.amount <= 0 )
                return WorkResult.FAILURE_STOP_IN_PLACE;
        }

        IFluidHandler handler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if ( handler != null )
            // TODO: Entity distance energy calculations
            return fillContainer(handler, 0);

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory,
                                      @Nonnull CondenserTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile,
                                      @Nullable Entity entity) {
        if ( stack.isEmpty() ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return WorkResult.FAILURE_REMOVE;
        }

        if ( getEnergyStored() < baseEnergy )
            return WorkResult.FAILURE_STOP_IN_PLACE;
        else if ( getEnergyStored() < (baseEnergy + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack fluid = tank.getFluid();
        if ( !inverted && (fluid == null || fluid.amount <= 0) )
            return WorkResult.FAILURE_STOP;

        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if ( handler != null ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            if ( burstAmount < Fluid.BUCKET_VOLUME ) {
                if ( !burstTick ) {
                    burstAmount = burstAmount == -1 ? remainingPerTick : burstAmount + remainingPerTick;
                    burstTick = true;
                }
                if ( burstAmount > Fluid.BUCKET_VOLUME )
                    burstAmount = Fluid.BUCKET_VOLUME;
            }

            int amount = burstAmount < remainingPerTick ? remainingPerTick : burstAmount;

            FluidActionResult result;
            if ( inverted )
                result = FluidUtil.tryEmptyContainerAndStow(stack, internalHandler, inventory, amount, null, true);
            else
                result = FluidUtil.tryFillContainerAndStow(stack, internalHandler, inventory, amount, null, true);

            if ( result.isSuccess() ) {
                ItemStack outStack = result.getResult();
                if ( !outStack.equals(stack) ) {
                    inventory.extractItem(slot, stack.getCount(), false);
                    ItemStack remaining = inventory.insertItem(slot, outStack, false);
                    if ( !remaining.isEmpty() ) {
                        remaining = InventoryHelper.insertStackIntoInventory(inventory, remaining, false);
                        if ( !remaining.isEmpty() )
                            CoreUtils.dropItemStackIntoWorldWithVelocity(remaining, world, target.pos);
                    }
                }

                activeTargetsPerTick++;
                extractEnergy(baseEnergy + target.cost, false);
                if ( remainingPerTick > 0 )
                    return WorkResult.SUCCESS_CONTINUE;
                return WorkResult.SUCCESS_STOP;
            } else
                return WorkResult.FAILURE_CONTINUE;
        }

        if ( lastRecipe != null && slot != craftingSlot )
            return WorkResult.FAILURE_CONTINUE;

        if ( !target.canCraft || inverted ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return WorkResult.FAILURE_REMOVE;
        }

        CondenserRecipeManager.CondenserRecipe recipe = CondenserRecipeManager.getRecipe(fluid, stack);
        lastRecipe = recipe;
        if ( recipe == null ) {
            craftingSlot = -1;
            return WorkResult.FAILURE_REMOVE;
        }

        craftingSlot = slot;

        if ( craftingFluid == null || !craftingFluid.isFluidEqual(fluid) )
            craftingFluid = new FluidStack(fluid.getFluid(), 0);

        int added = 0;
        if ( craftingFluid.amount < recipe.fluid.amount ) {
            added = Math.min(fluidRate, Math.min(recipe.fluid.amount - craftingFluid.amount, remainingPerTick));
            craftingFluid.amount += added;
            remainingPerTick -= added;
        }

        if ( craftingTicks < recipe.ticks && !didCraftingTicks ) {
            didCraftingTicks = true;
            craftingTicks += level.craftingTPT;
        }

        int totalCost = (int) (recipe.cost * augmentMultiplier) + baseEnergy + target.cost;
        if ( craftingTicks < recipe.ticks || craftingFluid.amount < recipe.fluid.amount || fluid.amount < craftingFluid.amount || getEnergyStored() < totalCost ) {
            if ( totalCost > getMaxEnergyStored() ) {
                if ( slot == craftingSlot ) {
                    lastRecipe = null;
                    craftingSlot = -1;
                }

                return WorkResult.FAILURE_REMOVE;
            }

            if ( added > 0 ) {
                activeTargetsPerTick++;
                return WorkResult.SUCCESS_STOP_IN_PLACE;
            }
            return WorkResult.FAILURE_STOP_IN_PLACE;
        }

        if ( inventory.extractItem(slot, 1, true).isEmpty() ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return WorkResult.FAILURE_REMOVE;
        }

        IItemHandler destination = (target.useSingleChest && tile instanceof TileEntityChest) ?
                tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.pos.getFacing()) :
                inventory;
        if ( destination == null ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return WorkResult.FAILURE_REMOVE;
        }

        ItemStack outStack = recipe.output.copy();
        int destSlot = getInsertSlot(destination, outStack);
        if ( destSlot == -1 ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            target.canCraft = false;
            return WorkResult.FAILURE_REMOVE;
        }

        inventory.extractItem(slot, 1, false);
        destination.insertItem(destSlot, outStack, false);
        extractEnergy(totalCost, false);
        tank.drain(craftingFluid.amount, true);
        craftingFluid.amount -= recipe.fluid.amount;
        activeTargetsPerTick++;
        craftingTicks = 0;
        lastRecipe = null;
        craftingSlot = -1;

        if ( stack.getCount() == 0 )
            return remainingPerTick <= 0 ? WorkResult.SUCCESS_STOP_REMOVE : WorkResult.SUCCESS_REMOVE;
        else
            return remainingPerTick <= 0 ? WorkResult.SUCCESS_STOP : WorkResult.SUCCESS_CONTINUE;
    }

    public WorkResult fillContainer(@Nonnull IFluidHandler handler, int cost) {
        IFluidHandler source = internalHandler;
        IFluidHandler destination = handler;

        if ( inverted ) {
            source = handler;
            destination = internalHandler;
        }

        FluidStack result = FluidUtil.tryFluidTransfer(destination, source, burstAmount == -1 ? remainingPerTick : burstAmount, true);
        if ( result != null ) {
            activeTargetsPerTick++;
            extractEnergy(baseEnergy + cost, false);
            if ( remainingPerTick > 0 )
                return WorkResult.SUCCESS_CONTINUE;
            return WorkResult.SUCCESS_STOP;
        }

        if ( remainingPerTick > 0 )
            return WorkResult.FAILURE_CONTINUE;

        return WorkResult.FAILURE_STOP;
    }

    /* Effects */

    @Override
    public void performEffect(@Nonnull CondenserTarget target, @Nonnull World world, boolean isEntity) {
        if ( world.isRemote || world != this.world || pos == null || !ModConfig.rendering.enableWorkParticles )
            return;

        int color = ColorHandler.getFluidColor(getTankFluid());
        float colorR = (color >> 16 & 255) / 255.0F;
        float colorG = (color >> 8 & 255) / 255.0F;
        float colorB = (color & 255) / 255.0F;

        if ( colorR == 0 )
            colorR = 0.000001F;

        PacketParticleLine packet;
        if ( isEntity && target.entity != null )
            packet = PacketParticleLine.betweenPoints(
                    EnumParticleTypes.REDSTONE, true,
                    pos, getEnumFacing(), target.entity,
                    3, colorR, colorG, colorB
            );
        else if ( target.pos != null )
            packet = PacketParticleLine.betweenPoints(
                    EnumParticleTypes.REDSTONE, true,
                    pos, getEnumFacing(), target.pos, target.pos.getFacing(),
                    3, colorR, colorG, colorB
            );
        else
            return;

        if ( packet != null )
            packet.sendToNearbyWorkers(this);
    }

    /* ITickable */

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
        if ( !simulate )
            energyPerTick += extracted;
        return extracted;
    }

    @Override
    public int getInfoMaxEnergyPerTick() {
        int rate = validTargetsPerTick;
        if ( rate > ModConfig.performance.stepsPerTick )
            rate = ModConfig.performance.stepsPerTick;

        if ( rate < 1 )
            return 0;

        return augmentDrain + (fluidGen ? fluidGenCost : 0) + (rate * maxEnergyPerTick);
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
        callNeighborStateChange(getFacingForSide(side));
    }

    @Override
    public void onNeighborBlockChange() {
        super.onNeighborBlockChange();
        Arrays.fill(sideCache, null);
        Arrays.fill(sideIsCached, false);
    }

    public void transferSide(TransferSide side) {
        if ( world == null || pos == null || world.isRemote )
            return;

        EnumFacing facing = getFacingForSide(side);
        TileEntity tile;

        if ( sideIsCached[side.index] ) {
            tile = sideCache[side.index];
            if ( tile != null && tile.isInvalid() ) {
                tile = world.getTileEntity(pos.offset(facing));
                sideCache[side.index] = tile;
            }

        } else {
            tile = world.getTileEntity(pos.offset(facing));
            sideCache[side.index] = tile;
            sideIsCached[side.index] = true;
        }

        if ( tile == null || tile.isInvalid() )
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

        // Fluid
        if ( !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite) )
            return;

        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
        if ( handler == null )
            return;

        if ( inverted )
            FluidUtil.tryFluidTransfer(handler, fluidHandler, fluidRate, true);
        else
            FluidUtil.tryFluidTransfer(fluidHandler, handler, fluidRate, true);
    }

    /* IConfigurableWorldTickRate */

    public boolean hasWorldTick() {
        return shouldProcessBlocks();
    }

    public int getWorldTickRate() {
        return gatherTickRate;
    }

    public int getMinWorldTickRate() {
        return level.gatherTicks;
    }

    public void setWorldTickRate(int value) {
        int max = getMaxWorldTickRate();
        if ( value > max )
            value = max;

        int min = getMinWorldTickRate();
        if ( value <= min )
            value = -1;

        if ( gatherTickRate == value )
            return;

        gatherTickRate = value;
        if ( world != null && !world.isRemote )
            markChunkDirty();
    }

    public int getActualWorldTickRate() {
        if ( gatherTickRate == -1 )
            return level.gatherTicks;

        int min = getMinWorldTickRate();
        if ( gatherTickRate == -1 || gatherTickRate < min )
            return min;

        int max = getMaxWorldTickRate();
        if ( gatherTickRate > max )
            return max;

        return gatherTickRate;
    }

    public void update() {
        super.update();

        worker.tickDown();

        gatherTick--;
        if ( gatherTick < 0 )
            gatherTick = (byte) getActualWorldTickRate();

        burstTick = false;
        activeTargetsPerTick = 0;
        energyPerTick = 0;
        fluidPerTick = 0;

        boolean enabled = redstoneControlOrDisable();
        if ( sideTransferAugment && enabled )
            executeSidedTransfer();

        if ( enabled && augmentDrain > 0 ) {
            if ( augmentDrain > getEnergyStored() )
                enabled = false;
            else
                extractEnergy(augmentDrain, false);
        }

        if ( fluidGen && getEnergyStored() >= fluidGenCost ) {
            int filled = fluidHandler.fill(fluidGenStack, true);
            if ( filled > 0 )
                extractEnergy(fluidGenCost, false);
        }

        if ( !enabled || getEnergyStored() < baseEnergy ) {
            if ( wantsTargets ) {
                wantsTargets = false;
                worker.updateTargetCache();
            }

            tickInactive();
            setActive(false);
            updateTrackers();
            saveEnergyHistory(energyPerTick);
            return;
        }

        tickActive();
        didCraftingTicks = false;

        int total = inverted ? tank.getCapacity() : tank.getFluidAmount();
        if ( total > fluidMaxRate )
            total = fluidMaxRate;

        remainingPerTick = total;
        setActive(worker.performWork());
        fluidPerTick = total - remainingPerTick;

        // While crafting, we always want to advance the ticks, even if we
        // didn't hit a crafting target this loop.
        if ( lastRecipe != null && !didCraftingTicks )
            craftingTicks += level.craftingTPT;

        if ( isCreative && inverted ) // Void Fluids
            tank.setFluid(null);

        updateTrackers();
        saveEnergyHistory(energyPerTick);
    }

    /* NBT Read and Write */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        burstAmount = tag.hasKey("Burst") ? tag.getInteger("Burst") : -1;
        craftingTicks = tag.getInteger("CraftingTicks");
        gatherTick = tag.getByte("GatherTick");
        NBTTagCompound crafting = tag.hasKey("CraftingFluid") ? tag.getCompoundTag("CraftingFluid") : null;
        if ( crafting != null )
            craftingFluid = FluidStack.loadFluidStackFromNBT(crafting);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setInteger("Burst", burstAmount);
        tag.setInteger("CraftingTicks", craftingTicks);
        tag.setByte("GatherTick", gatherTick);
        if ( craftingFluid != null && craftingFluid.amount > 0 ) {
            NBTTagCompound fluid = new NBTTagCompound();
            craftingFluid.writeToNBT(fluid);
            tag.setTag("CraftingFluid", fluid);
        }
        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        tank.readFromNBT(tag);
        iterationMode = IterationMode.fromInt(tag.getByte("IterationMode"));
        gatherTickRate = tag.hasKey("WorldTickRate") ? tag.getInteger("WorldTickRate") : -1;
        roundRobin = tag.hasKey("RoundRobin") ? tag.getInteger("RoundRobin") : -1;
        fluidRate = calculateFluidRate();

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = Mode.byIndex(tag.getByte("TransferSide" + i));

        updateTextures();

        boolean locked = tag.getBoolean("Locked");
        if ( locked ) {
            NBTTagCompound lock = tag.hasKey("LockStack") ? tag.getCompoundTag("LockStack") : null;
            if ( lock != null ) {
                FluidStack stack = FluidStack.loadFluidStackFromNBT(lock);
                if ( stack == null )
                    setLocked(true);
                else
                    setLocked(stack);
            } else
                setLocked(true);
        } else
            setLocked(false);
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);
        tank.writeToNBT(tag);
        tag.setByte("IterationMode", (byte) iterationMode.ordinal());
        if ( gatherTickRate != -1 )
            tag.setInteger("WorldTickRate", gatherTickRate);

        if ( roundRobin >= 0 )
            tag.setInteger("RoundRobin", roundRobin);

        for (int i = 0; i < sideTransfer.length; i++)
            tag.setByte("TransferSide" + i, (byte) sideTransfer[i].index);

        tag.setBoolean("Locked", locked);
        if ( lockStack != null ) {
            NBTTagCompound lock = new NBTTagCompound();
            lockStack.writeToNBT(lock);
            tag.setTag("LockStack", lock);
        }
        return tag;
    }

    /* Packets */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addFluidStack(tank.getFluid());
        for (TransferSide side : TransferSide.VALUES)
            payload.addByte(getSideTransferMode(side).index);
        payload.addBool(isInverted());
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        tank.setFluid(payload.getFluidStack());
        for (TransferSide side : TransferSide.VALUES)
            setSideTransferMode(side, Mode.byIndex(payload.getByte()));
        setInvertAugmented(payload.getBool());
        callBlockUpdate();
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();

        wantsTargets = true;

        payload.addFluidStack(tank.getFluid());
        payload.addInt(maxEnergyPerTick);
        payload.addInt(validTargetsPerTick);
        payload.addInt(activeTargetsPerTick);
        payload.addByte(iterationMode.ordinal());
        payload.addInt(roundRobin);
        payload.addInt(gatherTickRate);
        payload.addInt(fluidPerTick);
        payload.addBool(locked);
        payload.addFluidStack(lockStack);

        payload.addInt(craftingTicks);
        payload.addFluidStack(craftingFluid);
        payload.addItemStack(lastRecipe == null ? ItemStack.EMPTY : lastRecipe.input);

        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        tank.setFluid(payload.getFluidStack());
        maxEnergyPerTick = payload.getInt();
        validTargetsPerTick = payload.getInt();
        activeTargetsPerTick = payload.getInt();
        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getInt());
        setWorldTickRate(payload.getInt());
        fluidPerTick = payload.getInt();

        boolean locked = payload.getBool();
        FluidStack lockStack = payload.getFluidStack();
        if ( !locked || lockStack == null )
            setLocked(locked);
        else
            setLocked(lockStack);

        craftingTicks = payload.getInt();
        craftingFluid = payload.getFluidStack();
        ItemStack recipe = payload.getItemStack();
        if ( recipe.isEmpty() )
            lastRecipe = null;
        else
            lastRecipe = CondenserRecipeManager.getRecipe(craftingFluid == null ? getTankFluid() : craftingFluid, recipe);
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addBool(locked);
        payload.addFluidStack(lockStack);
        payload.addByte(iterationMode.ordinal());
        payload.addInt(roundRobin);
        payload.addInt(gatherTickRate);
        for (int i = 0; i < sideTransfer.length; i++)
            payload.addByte(sideTransfer[i].index);
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        boolean locked = payload.getBool();
        FluidStack lockStack = payload.getFluidStack();
        if ( !locked || lockStack == null )
            setLocked(locked);
        else
            setLocked(lockStack);

        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getInt());
        setWorldTickRate(payload.getInt());

        for (int i = 0; i < sideTransfer.length; i++)
            setSideTransferMode(i, Mode.byIndex(payload.getByte()));
    }

    /* Capabilities */

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if ( getSideTransferMode(facing) == Mode.DISABLED )
            return false;

        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
            return true;

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if ( getSideTransferMode(facing) == Mode.DISABLED )
            return null;

        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);

        return super.getCapability(capability, facing);
    }

    /* Target Info */

    public static class CondenserTarget extends TargetInfo {
        public int cost;
        public boolean canCraft = true;

        public CondenserTarget(BlockPosDimension target, ItemStack source, TileEntity tile, Entity entity, int cost) {
            super(target, source, tile, entity);
            this.cost = cost;
        }

        @Override
        public String toString() {
            return getStringBuilder()
                    .add("cost", cost)
                    .add("canCraft", canCraft)
                    .toString();
        }
    }
}

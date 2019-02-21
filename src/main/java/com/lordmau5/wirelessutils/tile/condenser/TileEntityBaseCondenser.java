package com.lordmau5.wirelessutils.tile.condenser;

import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.*;
import com.lordmau5.wirelessutils.tile.base.augmentable.*;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.FluidTank;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
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
import net.minecraft.util.ITickable;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.*;
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
        IChunkLoadAugmentable, IRoundRobinMachine, IWorldAugmentable, ITransferAugmentable,
        ICapacityAugmentable, IInventoryAugmentable, IInvertAugmentable, ITickable,
        IFluidGenAugmentable, ISidedTransfer, ISidedTransferAugmentable,
        IWorkProvider<TileEntityBaseCondenser.CondenserTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;

    protected final FluidTank tank;
    protected final IFluidHandler fluidHandler;
    protected final IFluidHandler internalHandler;

    protected boolean locked;
    protected FluidStack lockStack;

    private int capacityAugment;
    private int transferAugment;
    private boolean inverted;
    protected boolean chunkLoading;

    private int fluidRate;
    private int fluidMaxRate;
    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;
    private int roundRobin = -1;

    private FluidStack craftingFluid = null;
    private int craftingTicks = 0;
    private byte gatherTick = 0;

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
    private boolean[] sideTransfer;


    public TileEntityBaseCondenser() {
        super();
        sideTransfer = new boolean[6];
        Arrays.fill(sideTransfer, false);
        worker = new Worker<>(this);

        tank = new FluidTank(calculateFluidCapacity());
        fluidMaxRate = calculateMaxFluidRate();
        fluidRate = calculateFluidRate();

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
                if ( amount > remainingPerTick )
                    amount = remainingPerTick;

                if ( amount > fluidRate )
                    amount = fluidRate;

                if ( amount == 0 )
                    return 0;

                resource = resource.copy();
                resource.amount = amount;

                int filled = tank.fill(resource, doFill);
                if ( filled > 0 && doFill ) {
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

                if ( maxDrain > remainingPerTick )
                    maxDrain = remainingPerTick;

                if ( maxDrain > fluidRate )
                    maxDrain = fluidRate;

                if ( maxDrain == 0 )
                    return null;

                FluidStack output = tank.drain(maxDrain, doDrain);
                if ( output != null && doDrain ) {
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
        System.out.println(" Max Fluid Rate: " + fluidMaxRate);
        System.out.println(" Fluid per Tick: " + fluidPerTick);
        System.out.println("  Valid Targets: " + validTargetsPerTick);
        System.out.println(" Active Targets: " + activeTargetsPerTick);
        System.out.println(" Crafting Ticks: " + craftingTicks);
        System.out.println(" Crafting Fluid: " + debugPrintStack(craftingFluid));
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

    public String formatWorkUnit(long value) {
        if ( value < 1000000 || GuiScreen.isShiftKeyDown() )
            return String.format("%s %s", StringHelper.formatNumber(value), getWorkUnit());

        return TextHelpers.getScaledNumber(value / 1000, "B/t", true);
    }

    public long getWorkLastTick() {
        return fluidPerTick;
    }

    public long getWorkMaxRate() {
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

        if ( fluidGen )
            maxEnergyPerTick -= oldCost;

        updateFluidGen();

        if ( fluidGen )
            maxEnergyPerTick += fluidGenCost;
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
    public void setInvertAugmented(boolean inverted) {
        if ( this.inverted == inverted )
            return;

        this.inverted = inverted;
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
        sideTransferAugment = augmented;
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
        return new CondenserTarget(target, tile, entity, target == null ? 0 : getEnergyCost(target, source));
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
        maxEnergyPerTick = 0;
        if ( fluidGen )
            maxEnergyPerTick = fluidGenCost;

        return validTargets;
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
        if ( fluidRate < Fluid.BUCKET_VOLUME )
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
        }

        validTargetsPerTick++;
        maxEnergyPerTick += level.baseEnergyPerOperation + getEnergyCost(target, source);
        return true;
    }

    public boolean canWorkTile(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nonnull TileEntity tile) {
        if ( !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.getFacing()) )
            return false;

        validTargetsPerTick++;
        maxEnergyPerTick += level.baseEnergyPerOperation + getEnergyCost(target, source);
        return true;
    }

    public boolean canWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        if ( stack.isEmpty() )
            return false;

        int cost = target == null ? 0 : getEnergyCost(target);

        if ( stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ) {
            validTargetsPerTick++;
            maxEnergyPerTick += level.baseEnergyPerOperation + cost;
            return true;
        }

        if ( !inverted && FluidHelper.isFillableEmptyContainer(stack) ) {
            validTargetsPerTick++;
            maxEnergyPerTick += level.baseEnergyPerOperation + cost;
            return true;
        }

        if ( inverted )
            return false;

        CondenserRecipeManager.CondenserRecipe recipe = CondenserRecipeManager.getRecipe(getTankFluid(), stack);
        if ( recipe != null ) {
            validTargetsPerTick++;
            maxEnergyPerTick += level.baseEnergyPerOperation + cost + recipe.cost;
            return true;
        }

        return false;
    }

    @Override
    public boolean canWorkEntity(@Nonnull ItemStack source, @Nonnull World world, @Nonnull Entity entity) {
        if ( entity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) ) {
            validTargetsPerTick++;
            // TODO: Entity energy calculations
            maxEnergyPerTick += level.baseEnergyPerOperation;
            return true;
        }

        return false;
    }

    public static IFluidHandler getHandlerForBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        Block block = state.getBlock();
        if ( block instanceof IFluidBlock )
            return new FluidBlockWrapper((IFluidBlock) block, world, pos);
        else if ( block instanceof BlockLiquid )
            return new BlockLiquidWrapper((BlockLiquid) block, world, pos);

        return null;
    }

    @Nonnull
    public WorkResult performWorkBlock(@Nonnull CondenserTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile) {
        if ( gatherTick != 0 )
            return WorkResult.FAILURE_CONTINUE;

        if ( getEnergyStored() < level.baseEnergyPerOperation )
            return WorkResult.FAILURE_STOP;
        else if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack stack = tank.getFluid();
        if ( inverted ) {
            if ( stack != null && stack.amount >= tank.getCapacity() )
                return WorkResult.FAILURE_STOP;
        } else {
            if ( stack == null || stack.amount <= 0 )
                return WorkResult.FAILURE_STOP;
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

                return fillContainer(handler, target.cost);
            }

            if ( contained != null && contained.amount == properties[0].getCapacity() )
                return WorkResult.FAILURE_REMOVE;
        }

        if ( inverted )
            return WorkResult.FAILURE_REMOVE;

        if ( FluidUtil.tryPlaceFluid(null, world, target.pos, internalHandler, tank.getFluid()) ) {
            activeTargetsPerTick++;
            extractEnergy(level.baseEnergyPerOperation + target.cost, false);

            if ( remainingPerTick > 0 )
                return WorkResult.SUCCESS_REMOVE;

            return WorkResult.SUCCESS_STOP_REMOVE;
        }

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkTile(@Nonnull CondenserTarget target, @Nonnull World world, @Nullable IBlockState state, @Nonnull TileEntity tile) {
        if ( getEnergyStored() < level.baseEnergyPerOperation )
            return WorkResult.FAILURE_STOP;
        else if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack stack = tank.getFluid();
        if ( inverted ) {
            if ( stack != null && stack.amount >= tank.getCapacity() )
                return WorkResult.FAILURE_STOP;
        } else {
            if ( stack == null || stack.amount <= 0 )
                return WorkResult.FAILURE_STOP;
        }

        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.pos.getFacing());
        if ( handler != null )
            return fillContainer(handler, target.cost);

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    @Override
    public WorkResult performWorkEntity(@Nonnull CondenserTarget target, @Nonnull World world, @Nonnull Entity entity) {
        if ( getEnergyStored() < level.baseEnergyPerOperation )
            return WorkResult.FAILURE_STOP;
        else if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack stack = tank.getFluid();
        if ( inverted ) {
            if ( stack != null && stack.amount >= tank.getCapacity() )
                return WorkResult.FAILURE_STOP;
        } else {
            if ( stack == null || stack.amount <= 0 )
                return WorkResult.FAILURE_STOP;
        }

        IFluidHandler handler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if ( handler != null )
            // TODO: Entity distance energy calculations
            return fillContainer(handler, 0);

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull CondenserTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile, @Nullable Entity entity) {
        if ( stack.isEmpty() )
            return WorkResult.FAILURE_REMOVE;

        if ( getEnergyStored() < level.baseEnergyPerOperation )
            return WorkResult.FAILURE_STOP;
        else if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack fluid = tank.getFluid();
        if ( !inverted && (fluid == null || fluid.amount <= 0) )
            return WorkResult.FAILURE_STOP;

        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if ( handler != null ) {
            FluidActionResult result;
            if ( inverted )
                result = FluidUtil.tryEmptyContainerAndStow(stack, internalHandler, inventory, remainingPerTick, null, true);
            else
                result = FluidUtil.tryFillContainerAndStow(stack, internalHandler, inventory, remainingPerTick, null, true);

            if ( result.isSuccess() ) {
                ItemStack outStack = result.getResult();
                stack.shrink(stack.getCount());
                ItemStack remaining = inventory.insertItem(slot, outStack, false);
                if ( !remaining.isEmpty() ) {
                    remaining = InventoryHelper.insertStackIntoInventory(inventory, remaining, false);
                    if ( !remaining.isEmpty() )
                        CoreUtils.dropItemStackIntoWorldWithVelocity(remaining, world, target.pos);
                }

                activeTargetsPerTick++;
                extractEnergy(level.baseEnergyPerOperation + target.cost, false);
                if ( remainingPerTick > 0 )
                    return WorkResult.SUCCESS_CONTINUE;
                return WorkResult.SUCCESS_STOP;
            } else
                return WorkResult.FAILURE_CONTINUE;
        }

        if ( !target.canCraft || inverted )
            return WorkResult.FAILURE_REMOVE;

        CondenserRecipeManager.CondenserRecipe recipe = CondenserRecipeManager.getRecipe(fluid, stack);
        if ( recipe == null )
            return WorkResult.FAILURE_REMOVE;

        if ( craftingFluid == null || !craftingFluid.isFluidEqual(fluid) )
            craftingFluid = new FluidStack(fluid.getFluid(), 0);

        int added = 0;
        if ( craftingFluid.amount < recipe.fluid.amount ) {
            added = Math.min(fluidRate, Math.min(recipe.fluid.amount - craftingFluid.amount, remainingPerTick));
            craftingFluid.amount += added;
            remainingPerTick -= added;
        }

        int totalCost = recipe.cost + level.baseEnergyPerOperation + target.cost;
        if ( craftingTicks < recipe.ticks || craftingFluid.amount < recipe.fluid.amount || fluid.amount < craftingFluid.amount || getEnergyStored() < totalCost ) {
            if ( added > 0 ) {
                activeTargetsPerTick++;
                return WorkResult.SUCCESS_STOP;
            }
            return WorkResult.FAILURE_STOP;
        }

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
            target.canCraft = false;
            return WorkResult.FAILURE_REMOVE;
        }

        stack.shrink(1);
        destination.insertItem(destSlot, outStack, false);
        extractEnergy(totalCost, false);
        tank.drain(craftingFluid.amount, true);
        craftingFluid.amount -= recipe.fluid.amount;
        activeTargetsPerTick++;
        craftingTicks = 0;

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

        FluidStack result = FluidUtil.tryFluidTransfer(destination, source, remainingPerTick, true);
        if ( result != null ) {
            activeTargetsPerTick++;
            extractEnergy(level.baseEnergyPerOperation + cost, false);
            if ( remainingPerTick > 0 )
                return WorkResult.SUCCESS_CONTINUE;
            return WorkResult.SUCCESS_STOP;
        }

        if ( remainingPerTick > 0 )
            return WorkResult.FAILURE_CONTINUE;

        return WorkResult.FAILURE_STOP;
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

    public boolean isSideTransferEnabled(TransferSide side) {
        return sideTransfer[side.ordinal()];
    }

    public void setSideTransferEnabled(TransferSide side, boolean enabled) {
        int index = side.ordinal();
        if ( sideTransfer[index] == enabled )
            return;

        sideTransfer[index] = enabled;
        if ( !world.isRemote ) {
            sendTilePacket(Side.CLIENT);
            markChunkDirty();
        }

        callBlockUpdate();
    }

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

    public void update() {
        worker.tickDown();

        gatherTick--;
        if ( gatherTick < 0 )
            gatherTick = 10;

        activeTargetsPerTick = 0;
        energyPerTick = 0;
        fluidPerTick = 0;

        if ( fluidGen && getEnergyStored() >= fluidGenCost ) {
            int filled = fluidHandler.fill(fluidGenStack, true);
            if ( filled > 0 )
                extractEnergy(fluidGenCost, false);
        }

        if ( sideTransferAugment && redstoneControlOrDisable() )
            updateSidedTransfer();

        if ( !redstoneControlOrDisable() || getEnergyStored() < level.baseEnergyPerOperation ) {
            setActive(false);
            updateTrackers();
            return;
        }

        craftingTicks += level.craftingTPT;

        int total = inverted ? tank.getCapacity() : tank.getFluidAmount();
        if ( total > fluidMaxRate )
            total = fluidMaxRate;

        remainingPerTick = total;
        setActive(worker.performWork());
        fluidPerTick = total - remainingPerTick;

        if ( isCreative && inverted ) // Void Fluids
            tank.setFluid(null);

        updateTrackers();
    }

    /* NBT Read and Write */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        craftingTicks = tag.getInteger("CraftingTicks");
        NBTTagCompound crafting = tag.hasKey("CraftingFluid") ? tag.getCompoundTag("CraftingFluid") : null;
        if ( crafting != null )
            craftingFluid = FluidStack.loadFluidStackFromNBT(crafting);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setInteger("CraftingTicks", craftingTicks);
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
        roundRobin = tag.hasKey("RoundRobin") ? tag.getInteger("RoundRobin") : -1;
        fluidRate = calculateFluidRate();

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = tag.getBoolean("TransferSide" + i);

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
        if ( roundRobin >= 0 )
            tag.setInteger("RoundRobin", roundRobin);

        for (int i = 0; i < sideTransfer.length; i++)
            tag.setBoolean("TransferSide" + i, sideTransfer[i]);

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
        for (int i = 0; i < sideTransfer.length; i++)
            payload.addBool(sideTransfer[i]);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        tank.setFluid(payload.getFluidStack());
        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = payload.getBool();
        callBlockUpdate();
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addFluidStack(tank.getFluid());
        payload.addInt(maxEnergyPerTick);
        payload.addInt(validTargetsPerTick);
        payload.addInt(activeTargetsPerTick);
        payload.addByte(iterationMode.ordinal());
        payload.addInt(roundRobin);
        payload.addInt(fluidPerTick);
        payload.addBool(locked);
        payload.addFluidStack(lockStack);
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
        fluidPerTick = payload.getInt();

        boolean locked = payload.getBool();
        FluidStack lockStack = payload.getFluidStack();
        if ( !locked || lockStack == null )
            setLocked(locked);
        else
            setLocked(lockStack);
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addBool(locked);
        payload.addFluidStack(lockStack);
        payload.addByte(iterationMode.ordinal());
        payload.addInt(roundRobin);
        for (int i = 0; i < sideTransfer.length; i++)
            payload.addBool(sideTransfer[i]);
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

        TransferSide[] values = TransferSide.values();
        for (int i = 0; i < sideTransfer.length; i++)
            setSideTransferEnabled(values[i], payload.getBool());
    }

    /* Capabilities */

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing from) {
        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
            return true;

        return super.hasCapability(capability, from);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);

        return super.getCapability(capability, facing);
    }

    /* Target Info */

    public static class CondenserTarget extends TargetInfo {
        public final int cost;
        public boolean canCraft = true;

        public CondenserTarget(BlockPosDimension target, TileEntity tile, Entity entity, int cost) {
            super(target, tile, entity);
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

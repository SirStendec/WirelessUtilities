package com.lordmau5.wirelessutils.tile.condenser;

import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.tile.base.IRoundRobinMachine;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseEnergy;
import com.lordmau5.wirelessutils.tile.base.Worker;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class TileEntityBaseCondenser extends TileEntityBaseEnergy implements IRoundRobinMachine, IWorldAugmentable, ITransferAugmentable, ICapacityAugmentable, IInventoryAugmentable, IInvertAugmentable, ITickable, IWorkProvider<TileEntityBaseCondenser.CondenserTarget> {

    protected List<BlockPosDimension> validTargets;
    protected Worker worker;

    protected FluidTank tank;
    protected IFluidHandler fluidHandler;
    protected IFluidHandler internalHandler;

    protected boolean locked;
    protected FluidStack lockStack;

    private int capacityAugment;
    private int transferAugment;
    private boolean inverted;

    private int fluidRate;
    private int fluidMaxRate;
    private int roundRobin = -1;

    private FluidStack craftingFluid = null;
    private int craftingTicks = 0;

    private int remainingPerTick;
    private int fluidPerTick;
    private int activeTargetsPerTick;
    private int validTargetsPerTick;
    private int maxEnergyPerTick;
    private boolean processBlocks = false;
    private boolean processItems = false;


    public TileEntityBaseCondenser() {
        super();
        worker = new Worker(this);

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
                if ( out > 0 && doFill )
                    markChunkDirty();

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
                if ( out != null && doDrain )
                    markChunkDirty();

                return out;
            }
        };
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
        System.out.println("    Round Robin: " + roundRobin);
        System.out.println(" Max Fluid Rate: " + fluidMaxRate);
        System.out.println(" Fluid per Tick: " + fluidPerTick);
        System.out.println("  Valid Targets: " + validTargetsPerTick);
        System.out.println(" Active Targets: " + activeTargetsPerTick);
        System.out.println(" Crafting Ticks: " + craftingTicks);
        System.out.println(" Crafting Fluid: " + debugPrintStack(craftingFluid));
        System.out.println("       Capacity: " + tank.getCapacity());
        System.out.println("       Contents: " + debugPrintStack(tank.getFluid()));
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
        if ( roundRobin == -1 || roundRobin > rate )
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

    public CondenserTarget createInfo(@Nonnull BlockPosDimension target) {
        BlockPosDimension worker = getPosition();

        boolean interdimensional = worker.getDimension() != target.getDimension();
        double distance = worker.getDistance(target.getX(), target.getY(), target.getZ()) - 1;

        return new CondenserTarget(target, getEnergyCost(distance, interdimensional));
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
        return processItems;
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    @Override
    public CondenserTarget canWork(@Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, TileEntity tile) {
        if ( tile == null ) {
            if ( fluidRate < Fluid.BUCKET_VOLUME )
                return null;

            Material material = block.getMaterial();
            if ( inverted ) {
                if ( !material.isLiquid() )
                    return null;

                IFluidHandler handler = getHandlerForBlock(world, target, block);
                if ( handler == null )
                    return null;

                IFluidTankProperties[] properties = handler.getTankProperties();
                if ( properties == null || properties.length != 1 || !properties[0].canDrain() )
                    return null;

                FluidStack stack = tank.getFluid();
                if ( stack != null && stack.amount > tank.getCapacity() - Fluid.BUCKET_VOLUME )
                    return null;

                FluidStack other = properties[0].getContents();
                if ( other == null || other.amount < Fluid.BUCKET_VOLUME || (stack != null && !stack.isFluidEqual(other)) )
                    return null;

            } else {
                if ( !world.isAirBlock(target) && material.isSolid() && !block.getBlock().isReplaceable(world, pos) )
                    return null;

                FluidStack stack = tank.getFluid();
                if ( stack == null || stack.amount < Fluid.BUCKET_VOLUME || stack.getFluid() == null || !stack.getFluid().canBePlacedInWorld() )
                    return null;
            }

        } else if ( !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.getFacing()) )
            return null;

        validTargetsPerTick++;
        CondenserTarget out = createInfo(target);
        maxEnergyPerTick += level.baseEnergyPerOperation + out.cost;
        return out;
    }

    @Override
    public boolean canWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, @Nonnull TileEntity tile) {
        if ( stack.isEmpty() )
            return false;

        if ( stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ) {
            validTargetsPerTick++;
            maxEnergyPerTick += level.baseEnergyPerOperation + getEnergyCost(target);
            return true;
        }

        if ( inverted )
            return false;

        CondenserRecipeManager.CondenserRecipe recipe = CondenserRecipeManager.getRecipe(getTankFluid(), stack);
        if ( recipe != null ) {
            validTargetsPerTick++;
            maxEnergyPerTick += level.baseEnergyPerOperation + getEnergyCost(target) + recipe.cost;
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
    public WorkResult performWork(@Nonnull CondenserTarget target, @Nonnull World world, @Nonnull IBlockState state, TileEntity tile) {
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

        if ( tile == null ) {
            if ( !processBlocks )
                return WorkResult.FAILURE_REMOVE;

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

        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, target.pos.getFacing());
        if ( handler != null )
            return fillContainer(handler, target.cost);

        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull CondenserTarget target, @Nonnull World world, @Nonnull IBlockState state, @Nonnull TileEntity tile) {
        if ( stack.isEmpty() )
            return WorkResult.FAILURE_REMOVE;

        if ( getEnergyStored() < level.baseEnergyPerOperation )
            return WorkResult.FAILURE_STOP;
        else if ( getEnergyStored() < (level.baseEnergyPerOperation + target.cost) )
            return WorkResult.FAILURE_CONTINUE;

        FluidStack fluid = tank.getFluid();
        if ( fluid == null || fluid.amount <= 0 )
            return WorkResult.FAILURE_STOP;

        IFluidHandler handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if ( handler != null )
            return fillContainer(handler, target.cost);

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

    public void update() {
        worker.tickDown();

        if ( !redstoneControlOrDisable() ) {
            setActive(false);
            activeTargetsPerTick = 0;
            fluidPerTick = 0;
            energyPerTick = 0;
            return;
        }

        craftingTicks += level.craftingTPT;

        int total = inverted ? tank.getCapacity() : tank.getFluidAmount();
        if ( total > fluidMaxRate )
            total = fluidMaxRate;

        energyPerTick = 0;
        remainingPerTick = total;
        activeTargetsPerTick = 0;
        setActive(worker.performWork());
        fluidPerTick = total - remainingPerTick;

        if ( isCreative && inverted ) // Void Fluids
            tank.setFluid(null);
    }

    /* NBT Read and Write */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        craftingTicks = tag.getInteger("CraftingTicks");
        NBTTagCompound crafting = tag.getCompoundTag("CraftingFluid");
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
        roundRobin = tag.hasKey("RoundRobin") ? tag.getInteger("RoundRobin") : -1;
        fluidRate = calculateFluidRate();

        boolean locked = tag.getBoolean("Locked");
        if ( locked ) {
            NBTTagCompound lock = tag.getCompoundTag("LockStack");
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
        if ( roundRobin >= 0 )
            tag.setInteger("RoundRobin", roundRobin);

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
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        tank.setFluid(payload.getFluidStack());
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addFluidStack(tank.getFluid());
        payload.addInt(maxEnergyPerTick);
        payload.addInt(validTargetsPerTick);
        payload.addInt(activeTargetsPerTick);
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
        payload.addInt(roundRobin);
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

        setRoundRobin(payload.getInt());
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

        public CondenserTarget(BlockPosDimension target, int cost) {
            super(target);
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

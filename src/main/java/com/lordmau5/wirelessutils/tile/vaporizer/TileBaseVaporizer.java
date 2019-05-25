package com.lordmau5.wirelessutils.tile.vaporizer;

import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.InventoryHelper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.item.module.ItemModule;
import com.lordmau5.wirelessutils.tile.base.IConfigurableWorldTickRate;
import com.lordmau5.wirelessutils.tile.base.ISidedTransfer;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseEnergy;
import com.lordmau5.wirelessutils.tile.base.Worker;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.utils.FluidTank;
import com.lordmau5.wirelessutils.utils.ItemHandlerProxy;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class TileBaseVaporizer extends TileEntityBaseEnergy implements
        IWorkInfoProvider, ISidedTransfer, ISidedTransferAugmentable,
        IConfigurableWorldTickRate, IInvertAugmentable, IUnlockableSlots,
        IWorkProvider<TileBaseVaporizer.VaporizerTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;
    private Map<Integer, WUVaporizerPlayer> fakePlayerMap = new Int2ObjectOpenHashMap<>();

    protected IFluidHandler fluidHandler;
    protected FluidTank tank;

    private int excessFuel = 0;

    private boolean[] emptySlots;
    private boolean[] fullSlots;
    private int fullInput = 0;
    private int emptyInput = 0;
    private int fullOutput = 0;
    private int emptyOutput = 0;

    protected ItemHandlerProxy inputProxy;
    protected ItemHandlerProxy outputProxy;
    protected ItemHandlerProxy passiveProxy;

    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;

    private IVaporizerBehavior behavior = null;
    private boolean inverted = false;

    private byte gatherTick;
    private int gatherTickRate = -1;

    private int activeTargetsPerTick;
    private int validTargetsPerTick;
    private int maxEnergyPerTick;

    private boolean sideTransferAugment = false;
    private ISidedTransfer.Mode[] sideTransfer;
    private boolean[] sideIsCached;
    private TileEntity[] sideCache;

    private boolean temporarilyAllowInsertion = false;
    private boolean didFullEntities = false;

    private Item previousModule = null;

    public TileBaseVaporizer() {
        super();
        sideTransfer = new ISidedTransfer.Mode[6];
        sideIsCached = new boolean[6];
        sideCache = new TileEntity[6];
        Arrays.fill(sideTransfer, Mode.PASSIVE);
        Arrays.fill(sideIsCached, false);
        Arrays.fill(sideCache, null);

        worker = new Worker<>(this);
        tank = new FluidTank(calculateFluidCapacity());

        Fluid fluid = getExperienceFluid();
        if ( fluid != null ) {
            tank.setFluid(new FluidStack(fluid, 0));
            tank.setLocked();
        }

        fluidHandler = new IFluidHandler() {
            @Override
            public IFluidTankProperties[] getTankProperties() {
                boolean canFill = wantsFluid();
                return new IFluidTankProperties[]{
                        new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, true)
                };
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if ( !wantsFluid() )
                    return 0;

                return tank.fill(resource, doFill);
            }

            @Nullable
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                return tank.drain(resource, doDrain);
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                return tank.drain(maxDrain, doDrain);
            }
        };

        updateTextures();
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
        System.out.println("   Side Transfer: " + Arrays.toString(sideTransfer));
        System.out.println("   Valid Targets: " + validTargetsPerTick);
        System.out.println("  Active Targets: " + activeTargetsPerTick);

        System.out.println("   Tank Contents: " + debugPrintStack(tank.getFluid()));

        System.out.println(" World Tick Rate: " + gatherTickRate);
    }

    /* Fluid Stuff */

    public FluidTankCore getTank() {
        return tank;
    }

    public FluidStack getTankFluid() {
        return tank.getFluid();
    }

    public boolean wantsFluid() {
        return behavior != null && behavior.wantsFluid();
    }

    public boolean hasFluid() {
        if ( !ModConfig.vaporizers.useFluid )
            return false;

        return tank.getFluid() != null;
    }

    @Nullable
    public static Fluid getExperienceFluid() {
        Fluid result = null;
        if ( !ModConfig.vaporizers.customFluid.isEmpty() )
            result = FluidRegistry.getFluid(ModConfig.vaporizers.customFluid);

        if ( result == null )
            result = FluidRegistry.getFluid("essence");

        if ( result == null )
            result = FluidRegistry.getFluid("xpjuice");

        return result;
    }

    public int calculateFluidCapacity() {
        return level.maxCondenserCapacity;
    }

    /* Inventory */

    @Nonnull
    public ItemStack insertOutputStack(@Nonnull ItemStack stack) {
        temporarilyAllowInsertion = true;
        stack = ItemHandlerHelper.insertItemStacked(outputProxy, stack, false);
        temporarilyAllowInsertion = false;
        return stack;
    }

    public int getInputOffset() {
        return 0;
    }

    public int getOutputOffset() {
        return getInputOffset() + 8;
    }

    public int getModuleOffset() {
        return getOutputOffset() + 8;
    }

    public int getModifierOffset() {
        return getModuleOffset() + 1;
    }

    @Override
    protected void initializeItemStackHandler(int size) {
        super.initializeItemStackHandler(size);

        emptySlots = new boolean[size];
        fullSlots = new boolean[size];

        emptyOutput = 8;
        emptyInput = 8;

        Arrays.fill(emptySlots, true);
        Arrays.fill(fullSlots, false);

        passiveProxy = new ItemHandlerProxy(itemStackHandler, getInputOffset(), 16, true, true);
        inputProxy = new ItemHandlerProxy(itemStackHandler, getInputOffset(), 8, true, true);
        outputProxy = new ItemHandlerProxy(itemStackHandler, getOutputOffset(), 8, true, true);
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if ( !isSlotUnlocked(slot) )
            return false;

        if ( slot == getModuleOffset() )
            return isValidModule(stack);

        else if ( slot == getModifierOffset() )
            return behavior != null && behavior.isValidModifier(stack);

        else if ( slot >= getInputOffset() && slot < getOutputOffset() )
            return behavior != null && behavior.isValidInput(stack);

        return temporarilyAllowInsertion;
    }

    @Override
    public int getStackLimit(int slot) {
        if ( slot == getModuleOffset() || slot == getModifierOffset() )
            return 1;

        return super.getStackLimit(slot);
    }

    @Override
    public boolean isSlotUnlocked(int slot) {
        if ( slot == getModuleOffset() ) {
            if ( !itemStackHandler.getStackInSlot(getModifierOffset()).isEmpty() )
                return false;

            for (int s = getInputOffset(); s < getOutputOffset(); s++)
                if ( !itemStackHandler.getStackInSlot(s).isEmpty() )
                    return false;

            return true;

        } else if ( slot == getModifierOffset() )
            return behavior != null && behavior.isModifierUnlocked();

        else if ( slot >= getInputOffset() && slot < getOutputOffset() )
            return behavior != null && behavior.isInputUnlocked(slot - getInputOffset());

        return true;
    }

    @Override
    public void onContentsChanged(int slot) {
        updateItemCache(slot);

        if ( slot == getModuleOffset() )
            updateModule();

        if ( slot == getModifierOffset() )
            updateModifier();

        super.onContentsChanged(slot);
    }

    public boolean isValidModule(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() )
            return false;

        Item item = stack.getItem();
        return item instanceof ItemModule && ((ItemModule) item).canApplyTo(stack, this);
    }

    @Nullable
    public IVaporizerBehavior getBehavior() {
        return behavior;
    }

    public void updateModule() {
        ItemStack stack = itemStackHandler.getStackInSlot(getModuleOffset());
        if ( stack.isEmpty() || !isValidModule(stack) ) {
            behavior = null;
            previousModule = null;
            return;
        }

        ItemModule module = (ItemModule) stack.getItem();

        if ( module == previousModule && behavior != null ) {
            behavior.updateModule(stack);
            return;
        }

        behavior = module.getBehavior(stack, this);
        previousModule = module;
    }

    public ItemStack getModule() {
        return itemStackHandler.getStackInSlot(getModuleOffset());
    }

    public void setModule(@Nonnull ItemStack stack) {
        if ( isValidModule(stack) )
            itemStackHandler.setStackInSlot(getModuleOffset(), stack);
    }

    public ItemStack getModifier() {
        return itemStackHandler.getStackInSlot(getModifierOffset());
    }

    public void setModifier(@Nonnull ItemStack stack) {
        if ( behavior != null && behavior.isValidModifier(stack) )
            itemStackHandler.setStackInSlot(getModifierOffset(), stack);
    }

    public ItemStack getModifierGhost() {
        return behavior != null ? behavior.getModifierGhost() : ItemStack.EMPTY;
    }

    public void updateModifier() {
        if ( behavior == null )
            return;

        ItemStack stack = itemStackHandler.getStackInSlot(getModifierOffset());
        behavior.updateModifier(behavior.isValidModifier(stack) ? stack : ItemStack.EMPTY);
    }

    @Override
    public void readInventoryFromNBT(NBTTagCompound tag) {
        super.readInventoryFromNBT(tag);
        int slots = itemStackHandler.getSlots();
        for (int i = 0; i < slots; i++)
            updateItemCache(i);

        updateModule();
    }

    public void updateItemCache(int slot) {
        if ( slot < getInputOffset() )
            return;

        boolean isInput = slot >= getInputOffset() && slot < getOutputOffset();
        boolean isOutput = !isInput && slot >= getOutputOffset() && slot < getModuleOffset();

        ItemStack stack = itemStackHandler.getStackInSlot(slot);
        Item item = stack.getItem();

        boolean slotEmpty = stack.isEmpty();
        boolean slotFull = getStackLimit(slot, stack) == stack.getCount();

        if ( emptySlots[slot] != slotEmpty ) {
            emptySlots[slot] = slotEmpty;
            if ( isInput )
                emptyInput += slotEmpty ? 1 : -1;
            else if ( isOutput )
                emptyOutput += slotEmpty ? 1 : -1;
        }

        if ( fullSlots[slot] != slotFull ) {
            fullSlots[slot] = slotFull;
            if ( isInput )
                fullInput += slotFull ? 1 : -1;
            else if ( isOutput )
                fullOutput += slotFull ? 1 : -1;
        }
    }


    public ItemHandlerProxy getInput() {
        return inputProxy;
    }

    public ItemHandlerProxy getOutput() {
        return outputProxy;
    }

    public boolean hasInput() {
        return emptyInput < 8;
    }

    public boolean hasEmptyOutput() {
        return emptyOutput > 0;
    }

    /* Augments */

    @Override
    public void updateLevel() {
        super.updateLevel();
    }

    @Override
    public boolean isInverted() {
        return inverted && (behavior != null && behavior.canInvert());
    }

    @Override
    public void setInvertAugmented(boolean augmented) {
        if ( inverted == augmented )
            return;

        inverted = augmented;
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

    /* Work Info */

    public String getWorkUnit() {
        return "Butts/t";
    }

    @Override
    public double getWorkMaxRate() {
        return 0;
    }

    @Override
    public double getWorkLastTick() {
        return 0;
    }

    @Override
    public int getValidTargetCount() {
        return validTargetsPerTick;
    }

    @Override
    public int getActiveTargetCount() {
        return activeTargetsPerTick;
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

    public void addFuel(int amount) {
        // Always dirtying.
        markChunkDirty();

        // Store as fluid if possible.
        if ( hasFluid() ) {
            int room = Math.floorDiv(tank.getCapacity() - tank.getFluidAmount(), ModConfig.vaporizers.mbPerPoint);
            if ( room > 0 ) {
                if ( room < amount ) {
                    tank.fill(room * ModConfig.vaporizers.mbPerPoint, true);
                    amount -= room;

                } else {
                    tank.fill(amount * ModConfig.vaporizers.mbPerPoint, true);
                    return;
                }
            }
        }

        excessFuel += amount;
    }

    public boolean removeFuel(int amount) {
        // This is always dirtying.
        markChunkDirty();

        // Do we have excess fuel?
        if ( excessFuel > amount ) {
            excessFuel -= amount;
            markChunkDirty();
            return true;
        } else if ( excessFuel > 0 ) {
            amount -= excessFuel;
            excessFuel = 0;
        }

        // Use fluid before burning items.
        if ( hasFluid() ) {
            int points = Math.min(amount, Math.floorDiv(tank.getFluidAmount(), ModConfig.vaporizers.mbPerPoint));
            if ( points > 0 ) {
                tank.drain(points * ModConfig.vaporizers.mbPerPoint, true);
                amount -= points;
            }
        }

        // TODO: Burning items.

        return amount <= 0;
    }

    public BlockPosDimension getPosition() {
        if ( pos == null || world == null )
            return null;

        return new BlockPosDimension(pos, world.provider.getDimension());
    }

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

    public VaporizerTarget createInfo(@Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        return new VaporizerTarget(target, tile, entity, target == null ? 0 : getEnergyCost(target, source));
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
        return true;
    }

    public boolean shouldProcessTiles() {
        return false;
    }

    public boolean shouldProcessItems() {
        return false;
    }

    public boolean shouldProcessEntities() {
        return false;
    }

    public boolean canWorkBlock(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState block, @Nullable TileEntity tile) {
        return world.isAirBlock(target);
    }

    public boolean canWorkTile(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nonnull TileEntity tile) {
        return false;
    }

    public boolean canWorkEntity(@Nonnull ItemStack source, @Nonnull World world, @Nonnull Entity entity) {
        return false;
    }

    public boolean canWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        return false;
    }

    public boolean canGetFullEntities() {
        return false;
    }

    @Nullable
    public AxisAlignedBB getFullEntitiesAABB() {
        return null;
    }

    @Nonnull
    public WorkResult performWorkBlock(@Nonnull VaporizerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile) {
        WorkResult result = behavior.processBlock(target, world);
        boolean worked = result.success;
        boolean stop = !result.keepProcessing;

        if ( !result.remove ) {
            validTargetsPerTick++;
            if ( worked )
                activeTargetsPerTick++;
        }

        if ( !didFullEntities && !stop ) {
            Class<? extends Entity> klass = behavior.getEntityClass();
            Predicate<? super Entity> filter = behavior.getEntityFilter();

            if ( klass != null ) {
                AxisAlignedBB box = null;
                if ( canGetFullEntities() ) {
                    didFullEntities = true;
                    box = getFullEntitiesAABB();
                }

                if ( box == null )
                    box = new AxisAlignedBB(target.pos);

                List<Entity> entities;
                if ( filter == null )
                    entities = world.getEntitiesWithinAABB(klass, box);
                else
                    entities = world.getEntitiesWithinAABB(klass, box, filter);

                validTargetsPerTick += entities.size();

                for (Entity entity : entities) {
                    result = behavior.processEntity(entity, target);
                    worked |= result.success;
                    if ( result.success )
                        activeTargetsPerTick++;

                    if ( !result.keepProcessing ) {
                        stop = true;
                        break;
                    }
                }
            }
        }

        if ( worked )
            return stop ? WorkResult.SUCCESS_STOP : WorkResult.SUCCESS_CONTINUE;

        return stop ? WorkResult.FAILURE_STOP : WorkResult.FAILURE_CONTINUE;
    }

    @Nonnull
    public WorkResult performWorkTile(@Nonnull VaporizerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nonnull TileEntity tile) {
        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkEntity(@Nonnull VaporizerTarget target, @Nonnull World world, @Nonnull Entity entity) {
        return WorkResult.FAILURE_REMOVE;
    }

    @Nonnull
    public WorkResult performWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull VaporizerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile, @Nullable Entity entity) {
        return WorkResult.FAILURE_REMOVE;
    }

    public void performEffect(@Nonnull VaporizerTarget target, @Nonnull World world) {

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
        if ( !simulate )
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

    @Override
    public boolean isModeSpecific() {
        return true;
    }

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
        setProperty(
                "machine.config." + side.name().toLowerCase(),
                canSideTransfer(side) ?
                        getTextureForMode(getSideTransferMode(side))
                        : null
        );
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

    @Override
    public void transferSide(TransferSide side) {
        if ( world == null || pos == null || world.isRemote )
            return;

        Mode mode = getSideTransferMode(side);
        if ( mode == Mode.DISABLED || mode == Mode.PASSIVE )
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
        if ( hasFluid() ) {
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
            if ( mode == Mode.INPUT && wantsFluid() && handler != null )
                FluidUtil.tryFluidTransfer(fluidHandler, handler, tank.getCapacity(), true);
            else if ( mode == Mode.OUTPUT && handler != null )
                FluidUtil.tryFluidTransfer(handler, fluidHandler, tank.getCapacity(), true);
        }

        // Items
        IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opposite);
        if ( handler == null )
            return;

        // TODO: Tracking if we actually have room to insert or items to extract.
        if ( mode == Mode.INPUT && fullInput < 8 )
            transferOne(handler, inputProxy);
        else if ( mode == Mode.OUTPUT && emptyOutput < 8 )
            transferOne(outputProxy, handler);
    }

    public void transferOne(IItemHandler source, IItemHandler destination) {
        int slots = source.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = source.extractItem(i, 64, true);
            if ( stack.isEmpty() )
                continue;

            ItemStack remainder = InventoryHelper.insertStackIntoInventory(destination, stack, true);
            int count = stack.getCount() - remainder.getCount();
            if ( count == 0 )
                continue;

            remainder = InventoryHelper.insertStackIntoInventory(
                    destination,
                    source.extractItem(i, count, false),
                    false);

            if ( !remainder.isEmpty() ) {
                remainder = source.insertItem(i, remainder, false);
                if ( !remainder.isEmpty() )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(remainder, world, pos);
            }

            break;
        }
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

    /* ITickable */

    @Override
    public void update() {
        super.update();

        worker.tickDown();
        didFullEntities = false;

        gatherTick--;
        if ( gatherTick < 0 )
            gatherTick = (byte) getActualWorldTickRate();

        boolean enabled = behavior != null && redstoneControlOrDisable();

        if ( gatherTick == 0 ) {
            activeTargetsPerTick = 0;
            validTargetsPerTick = 0;
            energyPerTick = 0;
        }

        if ( enabled && sideTransferAugment )
            executeSidedTransfer();

        if ( enabled && augmentDrain > 0 ) {
            if ( augmentDrain > getEnergyStored() )
                enabled = false;
            else
                extractEnergy(augmentDrain, false);
        }

        boolean canRun = behavior != null && behavior.canRun();

        if ( !enabled || !canRun || getEnergyStored() < baseEnergy || gatherTick != 0 ) {
            tickInactive();
            setActive(false);
            updateTrackers();
            return;
        }

        tickActive();
        setActive(worker.performWork());
        updateTrackers();
    }

    /* Event Handling */

    public void onItemDrops(LivingDropsEvent event) {
        int mode = behavior == null ? 0 : behavior.getDropMode();
        if ( mode == 0 )
            return;

        List<EntityItem> drops = event.getDrops();
        if ( mode == 3 ) {
            drops.clear();
            return;
        }

        drops.removeIf(item -> {
            ItemStack stack = insertOutputStack(item.getItem());
            if ( mode == 2 || stack.isEmpty() )
                return true;
            item.setItem(stack);
            return false;
        });
    }

    public void onExperienceDrops(LivingExperienceDropEvent event) {
        int mode = behavior == null ? 0 : behavior.getExperienceMode();
        if ( mode == 0 )
            return;

        else if ( mode == 3 ) {
            event.setCanceled(true);
            return;
        }

        if ( !hasFluid() )
            return;

        int amount = ModConfig.vaporizers.mbPerPoint * event.getDroppedExperience();
        int used = tank.fill(amount, true);
        if ( used > 0 )
            markChunkDirty();

        int remaining = mode == 2 ? 0 : Math.floorDiv(amount - used, ModConfig.vaporizers.mbPerPoint);
        if ( remaining == 0 )
            event.setCanceled(true);
        else
            event.setDroppedExperience(remaining);
    }

    /* NBT Read and Write */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        gatherTick = tag.getByte("GatherTick");
        excessFuel = tag.getInteger("ExcessFuel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setByte("GatherTick", gatherTick);
        tag.setInteger("ExcessFuel", excessFuel);
        return tag;
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);

        if ( gatherTickRate != -1 )
            tag.setInteger("WorldTickRate", gatherTickRate);

        FluidStack fluid = tank.getFluid();
        if ( fluid != null && fluid.amount > 0 )
            tag.setInteger("Fluid", fluid.amount);

        tag.setByte("IterationMode", (byte) iterationMode.ordinal());

        for (int i = 0; i < sideTransfer.length; i++)
            tag.setByte("TransferSide" + i, (byte) sideTransfer[i].index);

        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        iterationMode = IterationMode.fromInt(tag.getByte("IterationMode"));
        gatherTickRate = tag.hasKey("WorldTickRate") ? tag.getInteger("WorldTickRate") : -1;

        FluidStack fluid = tank.getFluid();
        if ( fluid != null )
            fluid.amount = tag.getInteger("Fluid");

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = Mode.byIndex(tag.getByte("TransferSide" + i));

        updateTextures();
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();

        payload.addFluidStack(tank.getFluid());

        payload.addByte(iterationMode.ordinal());
        payload.addShort(validTargetsPerTick);
        payload.addShort(activeTargetsPerTick);
        payload.addInt(maxEnergyPerTick);
        payload.addInt(gatherTickRate);

        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);

        tank.setFluid(payload.getFluidStack());

        setIterationMode(IterationMode.fromInt(payload.getByte()));
        validTargetsPerTick = payload.getShort();
        activeTargetsPerTick = payload.getShort();
        maxEnergyPerTick = payload.getInt();
        setWorldTickRate(payload.getInt());
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();

        payload.addByte(iterationMode.ordinal());
        payload.addInt(gatherTickRate);

        for (int i = 0; i < sideTransfer.length; i++)
            payload.addByte(sideTransfer[i].index);

        if ( behavior != null )
            behavior.updateModePacket(payload);

        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);

        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setWorldTickRate(payload.getInt());

        for (int i = 0; i < sideTransfer.length; i++)
            setSideTransferMode(i, Mode.byIndex(payload.getByte()));

        if ( behavior != null )
            behavior.handleModePacket(payload);
    }

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();

        for (TransferSide side : TransferSide.VALUES)
            payload.addByte(getSideTransferMode(side).index);

        payload.addBool(isSidedTransferAugmented());

        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);

        for (TransferSide side : TransferSide.VALUES)
            setSideTransferMode(side, Mode.byIndex(payload.getByte()));

        setSidedTransferAugmented(payload.getBool());
    }

    /* Capabilities */

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if ( getSideTransferMode(facing) == Mode.DISABLED )
            return false;

        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
            return true;

        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
            return true;

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        Mode mode = getSideTransferMode(facing);
        if ( mode == Mode.DISABLED )
            return null;

        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);

        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ) {
            if ( mode == Mode.INPUT )
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inputProxy);
            else if ( mode == Mode.OUTPUT )
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(outputProxy);

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(passiveProxy);
        }

        return super.getCapability(capability, facing);
    }

    /* Target Info */

    public static class VaporizerTarget extends TargetInfo {
        public final int cost;

        public VaporizerTarget(BlockPosDimension pos, TileEntity tile, Entity entity, int cost) {
            super(pos, tile, entity);
            this.cost = cost;
        }

        @Override
        public MoreObjects.ToStringHelper getStringBuilder() {
            return super.getStringBuilder().add("cost", cost);
        }
    }

    /* Behaviors */

    public interface IVaporizerBehavior {

        void updateModule(@Nonnull ItemStack stack);

        boolean isModifierUnlocked();

        boolean isValidModifier(@Nonnull ItemStack stack);

        @Nonnull
        ItemStack getModifierGhost();

        void updateModifier(@Nonnull ItemStack stack);

        boolean isInputUnlocked(int slot);

        boolean isValidInput(@Nonnull ItemStack stack);

        ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui);

        default void updateModePacket(@Nonnull PacketBase packet) {

        }

        default void handleModePacket(@Nonnull PacketBase packet) {

        }

        boolean wantsFluid();

        boolean canRun();

        boolean canInvert();

        int getExperienceMode();

        int getDropMode();

        @Nullable
        Class<? extends Entity> getEntityClass();

        @Nullable
        Predicate<? super Entity> getEntityFilter();

        @Nonnull
        WorkResult processBlock(@Nonnull VaporizerTarget target, @Nonnull World world);

        @Nonnull
        WorkResult processEntity(@Nonnull Entity entity, @Nonnull VaporizerTarget target);
    }

    /* Fake Player */

    public void removeFakePlayer(@Nonnull World world) {
        fakePlayerMap.remove(world.provider.getDimension());
    }

    @Nullable
    public WUVaporizerPlayer getFakePlayer(@Nonnull World world) {
        int dimension = world.provider.getDimension();
        if ( fakePlayerMap.containsKey(dimension) )
            return fakePlayerMap.get(dimension);

        if ( world instanceof WorldServer ) {
            WUVaporizerPlayer player = new WUVaporizerPlayer((WorldServer) world, this);
            player.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), 90, 90);
            fakePlayerMap.put(dimension, player);
            return player;
        }

        return null;
    }

    public static class WUVaporizerPlayer extends WUFakePlayer {
        private final TileBaseVaporizer vaporizer;

        private WUVaporizerPlayer(WorldServer world, TileBaseVaporizer vaporizer) {
            super(world);
            this.vaporizer = vaporizer;
        }

        public TileBaseVaporizer getVaporizer() {
            return vaporizer;
        }
    }
}

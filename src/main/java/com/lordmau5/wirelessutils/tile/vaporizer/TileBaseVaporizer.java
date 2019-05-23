package com.lordmau5.wirelessutils.tile.vaporizer;

import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import com.google.common.base.MoreObjects;
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
import net.minecraft.util.math.BlockPos;
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

    private boolean temporarilyAllowInsertion = false;
    private boolean didFullEntities = false;

    public TileBaseVaporizer() {
        super();
        sideTransfer = new ISidedTransfer.Mode[6];
        Arrays.fill(sideTransfer, ISidedTransfer.Mode.PASSIVE);
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
        return behavior != null && behavior.wantsFluid(this);
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
            return behavior != null && behavior.isValidModifier(stack, this);

        else if ( slot >= getInputOffset() && slot < getOutputOffset() )
            return behavior != null && behavior.isValidInput(stack, this);

        return temporarilyAllowInsertion;
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
            return behavior != null && behavior.isModifierUnlocked(this);

        else if ( slot >= getInputOffset() && slot < getOutputOffset() )
            return behavior != null && behavior.isInputUnlocked(slot - getInputOffset(), this);

        return true;
    }

    @Override
    public void onContentsChanged(int slot) {
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

    public void updateModule() {
        ItemStack stack = itemStackHandler.getStackInSlot(getModuleOffset());
        if ( stack.isEmpty() || !isValidModule(stack) ) {
            behavior = null;
            return;
        }

        ItemModule module = (ItemModule) stack.getItem();
        behavior = module.getBehavior(stack, this);
    }

    public ItemStack getModifier() {
        return itemStackHandler.getStackInSlot(getModifierOffset());
    }

    public void updateModifier() {
        if ( behavior == null )
            return;

        ItemStack stack = itemStackHandler.getStackInSlot(getModifierOffset());
        behavior.updateModifier(behavior.isValidModifier(stack, this) ? stack : ItemStack.EMPTY, this);
    }

    @Override
    public void readInventoryFromNBT(NBTTagCompound tag) {
        super.readInventoryFromNBT(tag);
        updateModule();
    }

    public ItemHandlerProxy getInput() {
        return inputProxy;
    }

    public ItemHandlerProxy getOutput() {
        return outputProxy;
    }

    /* Augments */

    @Override
    public void updateLevel() {
        super.updateLevel();
    }

    @Override
    public boolean isInverted() {
        return inverted && (behavior != null && behavior.canInvert(this));
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
        if ( didFullEntities )
            return WorkResult.FAILURE_CONTINUE;

        AxisAlignedBB box = null;
        if ( canGetFullEntities() ) {
            didFullEntities = true;
            box = getFullEntitiesAABB();
        }

        if ( box == null )
            box = new AxisAlignedBB(target.pos);

        List<Entity> entities = world.getEntitiesWithinAABB(behavior.getEntityClass(), box);

        boolean worked = false;

        for (Entity entity : entities)
            worked = behavior.process(entity, this) || worked;

        if ( worked )
            behavior.postProcess(target, box, world, this);

        if ( worked )
            return didFullEntities ? WorkResult.SUCCESS_STOP : WorkResult.SUCCESS_CONTINUE;

        return didFullEntities ? WorkResult.FAILURE_STOP : WorkResult.FAILURE_CONTINUE;
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
    }

    @Override
    public void transferSide(TransferSide side) {
        if ( world == null || pos == null || world.isRemote )
            return;

        Mode mode = getSideTransferMode(side);
        if ( mode == Mode.DISABLED || mode == Mode.PASSIVE )
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
        if ( mode == Mode.INPUT && wantsFluid() && hasFluid() ) {
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
            if ( handler != null )
                FluidUtil.tryFluidTransfer(fluidHandler, handler, tank.getCapacity(), true);

        } else if ( mode == Mode.OUTPUT && !wantsFluid() && hasFluid() ) {
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
            if ( handler != null )
                FluidUtil.tryFluidTransfer(handler, fluidHandler, tank.getCapacity(), true);
        }

        // TODO: Inventory?
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

        if ( sideTransferAugment && enabled )
            executeSidedTransfer();

        if ( enabled && augmentDrain > 0 ) {
            if ( augmentDrain > getEnergyStored() )
                enabled = false;
            else
                extractEnergy(augmentDrain, false);
        }

        if ( !enabled || getEnergyStored() < baseEnergy || gatherTick != 0 ) {
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
        List<EntityItem> drops = event.getDrops();

        drops.removeIf(item -> {
            ItemStack stack = insertOutputStack(item.getItem());
            if ( stack.isEmpty() )
                return true;
            item.setItem(stack);
            return false;
        });
    }

    public void onExperienceDrops(LivingExperienceDropEvent event) {
        if ( !hasFluid() )
            return;

        int amount = ModConfig.vaporizers.mbPerPoint * event.getDroppedExperience();
        int used = tank.fill(amount, true);
        if ( used > 0 )
            markChunkDirty();

        int remaining = Math.floorDiv(amount - used, ModConfig.vaporizers.mbPerPoint);
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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setByte("GatherTick", gatherTick);
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

        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);

        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setWorldTickRate(payload.getInt());

        for (int i = 0; i < sideTransfer.length; i++)
            setSideTransferMode(i, Mode.byIndex(payload.getByte()));
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

        boolean canInvert(@Nonnull TileBaseVaporizer vaporizer);

        Class<? extends Entity> getEntityClass();

        boolean isInputUnlocked(int slot, @Nonnull TileBaseVaporizer vaporizer);

        boolean isValidInput(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer);

        boolean isModifierUnlocked(@Nonnull TileBaseVaporizer vaporizer);

        boolean isValidModifier(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer);

        void updateModifier(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer);

        default boolean wantsFluid(@Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        boolean process(@Nonnull Entity entity, @Nonnull TileBaseVaporizer vaporizer);

        void postProcess(@Nonnull VaporizerTarget target, @Nonnull AxisAlignedBB box, @Nonnull World world, @Nonnull TileBaseVaporizer vaporizer);

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

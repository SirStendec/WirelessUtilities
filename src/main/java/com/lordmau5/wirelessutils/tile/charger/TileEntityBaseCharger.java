package com.lordmau5.wirelessutils.tile.charger;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.MathHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.packet.PacketParticleLine;
import com.lordmau5.wirelessutils.plugins.JEI.charger.ChargerRecipeCategory;
import com.lordmau5.wirelessutils.tile.base.IRoundRobinMachine;
import com.lordmau5.wirelessutils.tile.base.ISidedTransfer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseAE2;
import com.lordmau5.wirelessutils.tile.base.Worker;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBusAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ICapacityAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IChunkLoadAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInventoryAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IInvertAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ITransferAugmentable;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.crafting.IWUCraftingMachine;
import com.lordmau5.wirelessutils.utils.crafting.IWURecipe;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;

public abstract class TileEntityBaseCharger extends TileEntityBaseAE2 implements
        IChunkLoadAugmentable, IInvertAugmentable, IRoundRobinMachine, ICapacityAugmentable, IWUCraftingMachine,
        ITransferAugmentable, IInventoryAugmentable, ISidedTransfer, ITickable, ISidedTransferAugmentable,
        IBusAugmentable,
        IWorkProvider<TileEntityBaseCharger.ChargerTarget> {

    //protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;

    protected boolean inverted = false;
    protected boolean chunkLoading = false;

    protected long transferLimit = -1;
    protected int capacityAugment = 0;
    protected int transferAugment = 0;

    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;
    protected long roundRobin = -1;

    protected boolean didCraftingTicks = false;
    protected ChargerRecipeManager.ChargerRecipe lastRecipe = null;
    protected int craftingSlot = -1;
    protected int craftingEnergy = 0;
    protected int craftingTicks = 0;
    protected long remainingPerTick;
    protected int activeTargetsPerTick;
    protected int validTargetsPerTick;

    protected boolean sideTransferAugment = false;
    protected final Mode[] sideTransfer;
    protected final boolean[] sideIsCached;
    protected final TileEntity[] sideCache;

    protected boolean processItems = false;

    public TileEntityBaseCharger() {
        super();
        sideTransfer = new Mode[6];
        sideIsCached = new boolean[6];
        sideCache = new TileEntity[6];
        Arrays.fill(sideTransfer, Mode.PASSIVE);
        Arrays.fill(sideIsCached, false);
        Arrays.fill(sideCache, null);

        worker = new Worker<>(this);

        updateTextures();
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();

        System.out.println("  Transfer Limit: " + transferLimit);
        System.out.println("      Iter. Mode: " + iterationMode);
        System.out.println("     Round Robin: " + roundRobin);
        System.out.println("        Inverted: " + inverted);
        System.out.println(" Capacity Factor: " + capacityAugment);
        System.out.println(" Transfer Factor: " + transferAugment);
        System.out.println("        Capacity: " + getEnergyStorage().getFullMaxEnergyStored());
        System.out.println("   Stored Energy: " + getEnergyStorage().getFullEnergyStored());
        System.out.println("     Max Extract: " + getEnergyStorage().getFullMaxExtract());

        System.out.println("   Process Items: " + processItems);
        System.out.println(" Crafting Recipe: " + lastRecipe);
        System.out.println("  Crafting Ticks: " + craftingTicks);
        System.out.println(" Crafting Energy: " + craftingEnergy);

        System.out.println("   Side Transfer: " + Arrays.toString(sideTransfer));

        System.out.println("  Remaining/Tick: " + remainingPerTick);
        System.out.println("    Maximum/Tick: " + getFullMaxEnergyPerTick());
        //System.out.println("   Valid Targets: " + (validTargets == null ? "NULL" : validTargets.size()));

        if ( worker != null )
            worker.debugPrint();
    }

    /* Comparator */

    @Override
    public int calculateComparatorInput() {
        if ( isCreative() )
            return 15;

        long energy = getFullEnergyStored();
        if ( energy == 0 )
            return 0;

        return 1 + MathHelper.round(energy * 14 / (double) getFullMaxEnergyStored());
    }

    /* Augments */

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void energyChanged() {
        if ( world != null && !world.isRemote )
            calculateTargets();
    }

    @Override
    public void setChunkLoadAugmented(boolean augmented) {
        if ( augmented == chunkLoading )
            return;

        chunkLoading = augmented;
        if ( world != null && !world.isRemote )
            calculateTargets();
    }

    @Override
    public void setInvertAugmented(boolean inverted) {
        if ( inverted == this.inverted )
            return;

        this.inverted = inverted;
        updateTextures();
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

    public double getWorkLastTick() {
        return energyPerTick;
    }

    public boolean hasSustainedRate() {
        return false;
    }

    public double getWorkMaxRate() {
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
        long max = (long) getWorkMaxRate();
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

    public int getEnergyCost(@Nonnull BlockPosDimension target) {
        BlockPosDimension worker = getPosition();

        boolean interdimensional = worker.getDimension() != target.getDimension();
        double distance = worker.getDistance(target.getX(), target.getY(), target.getZ()) - 1;

        return getEnergyCost(distance, interdimensional);
    }

    public abstract int getEnergyCost(double distance, boolean isInterdimensional);

    /* Charger Crafting */

    @Nullable
    public String getRecipeCategory() {
        return ChargerRecipeCategory.UID;
    }

    @Nullable
    public IWURecipe getCurrentRecipe() {
        return lastRecipe;
    }

    public float getCraftingProgress() {
        if ( lastRecipe == null )
            return 0;

        return Math.min(
                (float) craftingTicks / lastRecipe.ticks,
                (float) craftingEnergy / lastRecipe.cost
        );
    }

    public boolean canCraft() {
        return processItems && !ChargerRecipeManager.getAllRecipes().isEmpty();
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

    public void onTargetCacheRebuild() {
        validTargetsPerTick = 0;
        craftingSlot = -1;
        lastRecipe = null;
    }

    @Override
    public void onInactive() {
        super.onInactive();
        onTargetCacheRebuild();
        worker.clearTargetCache();
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

    public boolean shouldProcessEntities() {
        return true;
    }

    public ChargerTarget createInfo(@Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        int cost = target == null ? 0 : (int) (getEnergyCost(target, source) * augmentMultiplier);
        return new ChargerTarget(target, source, tile, entity, cost);
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

    public boolean canWorkBlock(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState block, @Nullable TileEntity tile) {
        return false;
    }

    @Nonnull
    public WorkResult performWorkBlock(@Nonnull ChargerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile) {
        return WorkResult.FAILURE_REMOVE;
    }

    public boolean canWorkTile(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nonnull TileEntity tile) {
        if ( !tile.hasCapability(CapabilityEnergy.ENERGY, target.getFacing()) )
            return false;

        validTargetsPerTick++;
        return true;
    }

    public boolean canWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nullable BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nullable IBlockState block, @Nullable TileEntity tile, @Nullable Entity entity) {
        if ( stack.isEmpty() )
            return false;

        if ( stack.hasCapability(CapabilityEnergy.ENERGY, null) || (!inverted && ChargerRecipeManager.recipeExists(stack)) ) {
            validTargetsPerTick++;
            return true;
        }

        return false;
    }

    public boolean canWorkEntity(@Nonnull ItemStack source, @Nonnull World world, @Nonnull Entity entity) {
        if ( entity.hasCapability(CapabilityEnergy.ENERGY, null) ) {
            validTargetsPerTick++;
            return true;
        }

        return false;
    }

    @Nonnull
    public WorkResult performWorkEntity(@Nonnull ChargerTarget target, @Nonnull World world, @Nonnull Entity entity) {
        IEnergyStorage storage = entity.getCapability(CapabilityEnergy.ENERGY, null);
        if ( storage == null )
            return WorkResult.FAILURE_REMOVE;

        // TODO: Entity cost.
        return chargeStorage(storage, 0);
    }

    @Nonnull
    public WorkResult performWorkTile(@Nonnull ChargerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nonnull TileEntity tile) {
        IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, target.pos.getFacing());
        if ( storage == null )
            return WorkResult.FAILURE_REMOVE;

        return chargeStorage(storage, target.cost);
    }

    @Nonnull
    public WorkResult performWorkItem(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull ChargerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile, @Nullable Entity entity) {
        if ( stack.isEmpty() ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return WorkResult.FAILURE_REMOVE;
        }

        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if ( storage != null ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return chargeStorage(storage, target.cost);
        }

        if ( lastRecipe != null && slot != craftingSlot )
            return WorkResult.FAILURE_CONTINUE;

        if ( !target.canCharge || inverted ) {
            if ( slot == craftingSlot ) {
                lastRecipe = null;
                craftingSlot = -1;
            }

            return WorkResult.FAILURE_REMOVE;
        }

        ChargerRecipeManager.ChargerRecipe recipe = ChargerRecipeManager.getRecipe(stack);
        lastRecipe = recipe;
        if ( recipe == null ) {
            craftingSlot = -1;
            return WorkResult.FAILURE_REMOVE;
        }

        craftingSlot = slot;

        int cost = (int) (recipe.cost * augmentMultiplier);
        if ( craftingEnergy < cost && craftingEnergy + getEnergyStored() >= cost ) {
            long added = Math.min(cost - craftingEnergy, remainingPerTick);
            added = isCreative ? added : getEnergyStorage().extractEnergy(added, false);
            craftingEnergy += added;
            remainingPerTick -= added;

            if ( craftingEnergy < cost || remainingPerTick <= 0 ) {
                if ( added > 0 ) {
                    activeTargetsPerTick++;
                    return WorkResult.SUCCESS_STOP_IN_PLACE;
                }

                return WorkResult.FAILURE_STOP_IN_PLACE;
            }
        }

        if ( craftingTicks < recipe.ticks && !didCraftingTicks ) {
            didCraftingTicks = true;
            craftingTicks += level.craftingTPT;
        }

        if ( craftingTicks < recipe.ticks || remainingPerTick < target.cost )
            return WorkResult.FAILURE_CONTINUE;

        if ( inventory.extractItem(slot, 1, true).isEmpty() ) {
            lastRecipe = null;
            craftingSlot = -1;
            return WorkResult.FAILURE_REMOVE;
        }

        IItemHandler destination = (target.useSingleChest && tile instanceof TileEntityChest) ?
                tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.pos.getFacing()) :
                inventory;
        if ( destination == null ) {
            lastRecipe = null;
            craftingSlot = -1;
            return WorkResult.FAILURE_REMOVE;
        }

        ItemStack outStack = recipe.output.copy();
        int destSlot = getInsertSlot(destination, outStack);
        if ( destSlot == -1 ) {
            lastRecipe = null;
            craftingSlot = -1;
            target.canCharge = false;
            return WorkResult.FAILURE_REMOVE;
        }

        inventory.extractItem(slot, 1, false);
        destination.insertItem(destSlot, outStack, false);
        remainingPerTick -= getEnergyStorage().extractEnergy(target.cost, false);
        craftingEnergy -= cost;
        craftingTicks = 0;
        lastRecipe = null;
        craftingSlot = -1;
        activeTargetsPerTick++;

        if ( stack.getCount() == 0 )
            return remainingPerTick <= 0 ? WorkResult.SUCCESS_STOP_REMOVE : WorkResult.SUCCESS_REMOVE;
        else
            return remainingPerTick <= 0 ? WorkResult.SUCCESS_STOP : WorkResult.SUCCESS_CONTINUE;
    }

    public WorkResult chargeStorage(@Nonnull IEnergyStorage storage, long cost) {
        long transferred = transmitEnergy(remainingPerTick, storage, cost);
        remainingPerTick -= transferred;

        boolean cont = remainingPerTick > 0;
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

    public abstract EnumFacing getEffectOriginFace();

    @Override
    public boolean performEffect(@Nonnull ChargerTarget target, @Nonnull World world, boolean isEntity) {
        if ( world.isRemote || world != this.world || pos == null || !ModConfig.rendering.particlesEnabled )
            return false;

        PacketParticleLine packet;
        if ( isEntity && target.entity != null )
            packet = PacketParticleLine.betweenPoints(
                    EnumParticleTypes.REDSTONE, true,
                    pos, getEffectOriginFace(), target.entity,
                    3, 0, 0, 0
            );
        else if ( target.pos != null )
            packet = PacketParticleLine.betweenPoints(
                    EnumParticleTypes.REDSTONE, true,
                    pos, getEffectOriginFace(), target.pos, target.pos.getFacing(),
                    3, 0, 0, 0
            );
        else
            return false;

        if ( packet == null )
            return false;

        packet.sendToNearbyWorkers(this);
        return true;
    }

    /* Bus */

    @Override
    public boolean getBusShouldExtractEnergy(TickPhase phase) {
        return inverted;
    }

    @Override
    public boolean getBusShouldInsertEnergy(TickPhase phase) {
        return phase == TickPhase.PRE && !inverted;
    }

    @Nullable
    public IItemHandler getBusItemOutputHandler(TickPhase phase) {
        return null;
    }

    @Nullable
    public IItemHandler getBusItemInputHandler(TickPhase phase) {
        return null;
    }

    @Nullable
    public ItemStack[] getBusItemInputRequest(TickPhase phase) {
        return null;
    }

    @Nullable
    public IFluidHandler getBusFluidOutputHandler(TickPhase phase) {
        return null;
    }

    @Nullable
    public IFluidHandler getBusFluidInputHandler(TickPhase phase) {
        return null;
    }

    @Nullable
    public FluidStack[] getBusFluidInputRequest(TickPhase phase) {
        return null;
    }

    /* Sided Transfer */

    @Nonnull
    @Override
    public EnumSet<EnumFacing> getAE2ConnectableSides() {
        EnumSet<EnumFacing> out = null;
        for (TransferSide side : TransferSide.VALUES)
            if ( getSideTransferMode(side) != Mode.DISABLED ) {
                EnumFacing face = getFacingForSide(side);
                if ( out == null )
                    out = EnumSet.of(face);
                else
                    out.add(face);
            }

        if ( out == null )
            return EnumSet.noneOf(EnumFacing.class);
        else
            return out;
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
            updateNode();
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
        if ( !tile.hasCapability(CapabilityEnergy.ENERGY, opposite) )
            return;

        IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, opposite);
        if ( storage == null )
            return;

        if ( inverted && storage.canReceive() ) {
            int maxTransfer = getEnergyStored();
            if ( maxTransfer > getMaxExtract() )
                maxTransfer = getMaxExtract();

            int received = storage.receiveEnergy(maxTransfer, false);
            if ( received > 0 )
                extractEnergy(received, false);

        } else if ( !inverted && storage.canExtract() ) {
            long maxReceive = getFullMaxEnergyStored() - getFullEnergyStored();
            if ( maxReceive > getMaxReceive() )
                maxReceive = getMaxReceive();

            int received = storage.extractEnergy((int) maxReceive, false);
            if ( received > 0 )
                receiveEnergy(received, false);
        }
    }

    @Override
    public void update() {
        super.update();
        if ( world.isRemote )
            return;

        WirelessUtils.profiler.startSection("charger:update"); // 1 - update

        worker.tickDown();

        activeTargetsPerTick = 0;
        energyPerTick = 0;

        boolean enabled = redstoneControlOrDisable();
        if ( sideTransferAugment && enabled )
            executeSidedTransfer();

        preTickAugments(enabled);

        long energy = getFullEnergyStored();

        if ( !enabled || (inverted ?
                (energy == getFullMaxEnergyStored() || getMaxReceive() == 0) :
                (energy == 0 || getMaxExtract() == 0 || energy < augmentDrain)) ) {
            tickInactive();
            tickAugments(false);
            setActive(false);
            updateTrackers();
            saveEnergyHistory(energyPerTick);
            WirelessUtils.profiler.endSection(); // 1 - update
            return;
        }

        tickActive();
        didCraftingTicks = false;

        long total;

        if ( inverted ) {
            total = getMaxReceive();
            long remaining = getFullMaxEnergyStored() - energy;
            if ( remaining < total )
                total = remaining;

        } else {
            total = getMaxExtract();
            if ( energy < total )
                total = energy;
        }

        remainingPerTick = total;
        activeTargetsPerTick = 0;

        if ( !inverted && augmentDrain > 0 )
            remainingPerTick -= extractEnergy(augmentDrain, false);

        tickAugments(true);
        setActive(worker.performWork());
        postTickAugments();

        // While crafting, we always want to advance the ticks, even if we
        // didn't hit a crafting target this loop.
        if ( lastRecipe != null && !didCraftingTicks )
            craftingTicks += level.craftingTPT;

        if ( inverted && augmentDrain > 0 )
            extractEnergy(augmentDrain, false);

        energyPerTick = total - remainingPerTick;
        saveEnergyHistory(energyPerTick);
        updateTrackers();
        WirelessUtils.profiler.endSection(); // 1 - update
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

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = Mode.byIndex(tag.getByte("TransferSide" + i));

        updateTextures();

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

        for (int i = 0; i < sideTransfer.length; i++)
            tag.setByte("TransferSide" + i, (byte) sideTransfer[i].index);

        return tag;
    }

    /* Packets */

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addLong(transferLimit);
        payload.addByte(iterationMode.ordinal());
        payload.addLong(roundRobin);
        for (int i = 0; i < sideTransfer.length; i++)
            payload.addByte(sideTransfer[i].index);
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        setTransferLimit(payload.getLong());
        setIterationMode(IterationMode.fromInt(payload.getByte()));
        setRoundRobin(payload.getLong());
        for (int i = 0; i < sideTransfer.length; i++)
            setSideTransferMode(i, Mode.byIndex(payload.getByte()));
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addLong(transferLimit);
        payload.addByte(iterationMode.ordinal());
        payload.addLong(roundRobin);
        payload.addInt(validTargetsPerTick);
        payload.addInt(activeTargetsPerTick);
        payload.addInt(craftingTicks);
        payload.addInt(craftingEnergy);
        payload.addItemStack(lastRecipe == null ? ItemStack.EMPTY : lastRecipe.input);

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
        craftingTicks = payload.getInt();
        craftingEnergy = payload.getInt();
        ItemStack recipe = payload.getItemStack();
        lastRecipe = recipe.isEmpty() ? null : ChargerRecipeManager.getRecipe(recipe);
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

    /* Capabilities */

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if ( getSideTransferMode(facing) == Mode.DISABLED )
            return false;

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if ( getSideTransferMode(facing) == Mode.DISABLED )
            return null;

        return super.getCapability(capability, facing);
    }

    /* Target Info */

    public static class ChargerTarget extends TargetInfo {

        public int cost;
        public boolean canCharge = true;

        public ChargerTarget(BlockPosDimension target, ItemStack source, TileEntity tile, Entity entity, int cost) {
            super(target, source, tile, entity);
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

package com.lordmau5.wirelessutils.tile.vaporizer;

import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
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
import com.lordmau5.wirelessutils.tile.base.augmentable.IBudgetInfoProvider;
import com.lordmau5.wirelessutils.tile.base.augmentable.IChunkLoadAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFilterAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFluidGenAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.FluidTank;
import com.lordmau5.wirelessutils.utils.ItemHandlerProxy;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
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
import net.minecraftforge.common.DimensionManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class TileBaseVaporizer extends TileEntityBaseEnergy implements
        IWorkInfoProvider, IBudgetInfoProvider, ISidedTransfer, ISidedTransferAugmentable,
        IConfigurableWorldTickRate, IUnlockableSlots, IFilterAugmentable, IChunkLoadAugmentable,
        IFluidGenAugmentable,
        IWorkProvider<TileBaseVaporizer.VaporizerTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker<VaporizerTarget> worker;
    private final Map<Integer, WUVaporizerPlayer> fakePlayerMap = new Int2ObjectOpenHashMap<>();

    protected final IFluidHandler fluidHandler;
    protected final FluidTank tank;
    protected Fluid lastFluid;
    private final boolean hasFluid;

    private boolean fluidGen = false;
    private FluidStack fluidGenStack = null;
    private int fluidGenCost = 0;

    private int fluidRate = 0;
    private int excessFuel = 0;

    protected double energyMultiplier = 1;
    protected int energyAddition = 0;
    protected int energyDrain = 0;

    protected double moduleMultiplier = 1;
    protected int moduleEnergy = 0;
    protected int moduleDrain = 0;

    private boolean[] emptySlots;
    private boolean[] fullSlots;
    private int fullInput = 0;
    private int emptyInput = 0;
    private int fullOutput = 0;
    private int emptyOutput = 0;

    protected ItemHandlerProxy inputProxy;
    protected ItemHandlerProxy outputProxy;
    protected PassiveItemHandler passiveProxy;

    protected IterationMode iterationMode = IterationMode.RANDOM;

    private IVaporizerBehavior behavior = null;
    private boolean wantTargets = false;

    public int remainingBudget;
    private int maximumBudget;
    private int budgetPerTick;

    private byte gatherTick;
    private int gatherTickRate = -1;

    private int activeTargetsPerTick;
    private int validTargetsPerTick;

    private int maxEnergyPerEntity;
    private int maxEnergyPerBlock;

    protected boolean chunkLoading = false;

    private int remainingPerTick;
    private byte fluidSwap = 0;

    private boolean sideTransferAugment = false;
    private final ISidedTransfer.Mode[] sideTransfer;
    private final boolean[] sideIsCached;
    private final TileEntity[] sideCache;

    private boolean temporarilyAllowInsertion = false;
    private boolean didFullEntities = false;

    private Item previousModule = null;
    private Predicate<ItemStack> itemFilter = null;
    private boolean voidingItems = false;

    public TileBaseVaporizer() {
        super();

        if ( ModConfig.vaporizers.useFluid ) {
            hasFluid = getExperienceFluid() != null;
        } else
            hasFluid = false;

        sideTransfer = new ISidedTransfer.Mode[6];
        sideIsCached = new boolean[6];
        sideCache = new TileEntity[6];
        Arrays.fill(sideTransfer, Mode.PASSIVE);
        Arrays.fill(sideIsCached, false);
        Arrays.fill(sideCache, null);

        worker = new Worker<>(this);
        tank = new FluidTank(calculateFluidCapacity());

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
                if ( !wantsFluid() || resource == null )
                    return 0;

                FluidStack existing = tank.getFluid();
                int rate = 0;
                if ( existing == null ) {
                    Fluid fluid = resource.getFluid();
                    if ( hasSpecialFluid() ) {
                        if ( behavior == null || !behavior.isFluidValid(fluid) )
                            return 0;

                    } else {
                        rate = getFluidRate(fluid);

                        if ( rate == 0 )
                            return 0;
                    }
                }

                int out = tank.fill(resource, doFill);
                if ( doFill && existing == null && tank.getFluid() != null ) {
                    if ( !hasSpecialFluid() ) {
                        lastFluid = resource.getFluid();
                        fluidRate = rate;
                    }

                    updateFluidGen();
                }

                return out;
            }

            @Nullable
            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                FluidStack out = tank.drain(resource, doDrain);
                if ( doDrain && (tank.getFluid() == null || tank.getFluidAmount() == 0) ) {
                    fluidRate = 0;
                    updateFluidGen();
                }
                return out;
            }

            @Nullable
            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                FluidStack out = tank.drain(maxDrain, doDrain);
                if ( doDrain && (tank.getFluid() == null || tank.getFluidAmount() == 0) ) {
                    fluidRate = 0;
                    updateFluidGen();
                }
                return out;
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
        System.out.println(" World Tick Rate: " + gatherTickRate);
        System.out.println("          Budget: " + remainingBudget + " (max: " + maximumBudget + ")");
        System.out.println("        Budget/t: " + budgetPerTick);

        System.out.println("    Energy Drain: " + energyDrain);
        System.out.println(" Energy Addition: " + energyAddition);
        System.out.println("     Energy Mult: " + energyMultiplier);

        System.out.println("       Has Fluid: " + hasFluid);
        System.out.println("      Last Fluid: " + lastFluid);
        System.out.println("   Tank Contents: " + debugPrintStack(tank.getFluid()));
        System.out.println("      Fluid Rate: " + fluidRate);
        System.out.println("   Overflow Fuel: " + excessFuel);

        System.out.println("   Valid Targets: " + validTargetsPerTick);
        System.out.println("  Active Targets: " + activeTargetsPerTick);

        System.out.println("Max Energy Block: " + maxEnergyPerBlock);
        System.out.println("  Max Energy Ent: " + maxEnergyPerEntity);

        System.out.println("       Fluid Gen: " + fluidGen);
        System.out.println(" Fluid Gen Fluid: " + debugPrintStack(fluidGenStack));
        System.out.println("  Fluid Gen Cost: " + fluidGenCost);

        System.out.println("     Input Slots: empty=" + emptyInput + ", full=" + fullInput);
        System.out.println("    Output Slots: empty=" + emptyOutput + ", full=" + fullOutput);

        System.out.println("        Behavior: " + behavior);
        if ( behavior != null )
            behavior.debugPrint();
    }

    /* Fluid Stuff */

    public void setFluidSwap(byte direction) {
        fluidSwap = direction;
    }

    public void performFluidSwap(byte direction) {
        if ( !hasFluid() || hasSpecialFluid() || tank == null || world == null || world.isRemote || !ModConfig.vaporizers.allowConversion )
            return;

        FluidStack stack = tank.getFluid();
        if ( stack == null || stack.amount == 0 )
            return;

        Fluid fluid = stack.getFluid();
        String name = fluid == null ? null : fluid.getName();
        if ( name == null )
            return;

        int idx = -1;
        int l = Math.min(ModConfig.vaporizers.fluids.length, ModConfig.vaporizers.mbPerPoint.length);
        for (int i = 0; i < l; i++) {
            if ( ModConfig.vaporizers.fluids[i].equalsIgnoreCase(name) ) {
                idx = i;
                break;
            }
        }

        if ( idx == -1 )
            return;

        double amount = stack.amount / (double) fluidRate;

        int i = idx + direction;
        if ( i < 0 )
            i = l - 1;
        else if ( i >= l )
            i = 0;

        while ( i != idx ) {
            Fluid newFluid = FluidRegistry.getFluid(ModConfig.vaporizers.fluids[i]);
            if ( newFluid != null ) {
                int newRate = ModConfig.vaporizers.mbPerPoint[i];
                int newAmount = (int) Math.floor(amount * newRate);

                if ( newAmount <= tank.getCapacity() ) {
                    tank.setFluid(new FluidStack(newFluid, newAmount));
                    fluidRate = newRate;
                    lastFluid = newFluid;
                    updateFluidGen();
                    markChunkDirty();
                    return;
                }
            }

            i += direction;
            if ( i < 0 )
                i = l - 1;
            else if ( i >= l )
                i = 0;
        }
    }

    public FluidTankCore getTank() {
        return tank;
    }

    public FluidStack getTankFluid() {
        return tank.getFluid();
    }

    public int getFluidRate() {
        if ( hasSpecialFluid() )
            return 0;

        if ( fluidRate == 0 )
            return ModConfig.vaporizers.mbPerPoint[0];

        return fluidRate;
    }

    public boolean hasSpecialFluid() {
        return behavior != null && behavior.hasSpecialFluid();
    }

    public boolean wantsFluid() {
        if ( !ModConfig.vaporizers.acceptFluid )
            return false;

        return behavior != null && behavior.wantsFluid();
    }

    public boolean hasFluid() {
        return hasFluid;
    }

    public boolean isTankFull() {
        return tank.getFluidAmount() >= tank.getCapacity();
    }

    public boolean isFluidValid(@Nullable Fluid fluid) {
        if ( fluid == null )
            return false;

        if ( behavior != null && behavior.hasSpecialFluid() )
            return behavior.isFluidValid(fluid);

        String fluidName = fluid.getName();
        for (String name : ModConfig.vaporizers.fluids)
            if ( name.equalsIgnoreCase(fluidName) )
                return true;

        return false;
    }

    public static int getFluidRate(@Nullable Fluid fluid) {
        if ( fluid == null )
            return 0;

        String fluidName = fluid.getName();
        int max = Math.min(ModConfig.vaporizers.fluids.length, ModConfig.vaporizers.mbPerPoint.length);
        for (int i = 0; i < max; i++) {
            if ( ModConfig.vaporizers.fluids[i].equalsIgnoreCase(fluidName) )
                return ModConfig.vaporizers.mbPerPoint[i];
        }

        return 0;
    }

    @Nullable
    public static Fluid getExperienceFluid() {
        Fluid result = null;
        for (String fluid : ModConfig.vaporizers.fluids) {
            result = FluidRegistry.getFluid(fluid);
            if ( result != null )
                break;
        }

        return result;
    }

    public int calculateFluidCapacity() {
        return level.maxVaporizerFluid;
    }

    public void setFluidGenAugmented(boolean enabled, FluidStack fluidStack, int energy) {
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
        if ( fluidGenStack == null || tank == null || !wantsFluid() ) {
            fluidGen = false;
            return;
        }

        FluidStack fluid = tank.getFluid();
        if ( fluid != null && !fluid.isFluidEqual(fluidGenStack) ) {
            fluidGen = false;
            return;
        } else if ( fluid == null && !isFluidValid(fluidGenStack.getFluid()) ) {
            fluidGen = false;
            return;
        }

        fluidGen = true;
    }

    /* Inventory */

    @Override
    public boolean canUpgrade(ItemStack upgrade) {
        if ( upgrade.getItem() instanceof ItemModule )
            return getModule().isEmpty() && isValidModule(upgrade);

        return super.canUpgrade(upgrade);
    }

    @Override
    public boolean installUpgrade(ItemStack upgrade) {
        if ( upgrade.getItem() instanceof ItemModule ) {
            if ( !getModule().isEmpty() || !isValidModule(upgrade) )
                return false;

            setModule(upgrade);
            return true;
        }

        return super.installUpgrade(upgrade);
    }

    @Nonnull
    public ItemStack insertOutputStack(@Nonnull ItemStack stack) {
        return insertOutputStack(stack, false);
    }

    @Nonnull
    public ItemStack insertOutputStack(@Nonnull ItemStack stack, boolean simulate) {
        temporarilyAllowInsertion = true;
        stack = ItemHandlerHelper.insertItemStacked(outputProxy, stack, simulate);
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

        passiveProxy = new PassiveItemHandler(this);
        inputProxy = new ItemHandlerProxy(itemStackHandler, getInputOffset(), 8, true, true);
        outputProxy = new ItemHandlerProxy(itemStackHandler, getOutputOffset(), 8, true, true);
    }

    @Override
    public boolean shouldVoidItem(int slot, @Nonnull ItemStack stack) {
        if ( voidingItems && itemFilter != null )
            return !itemFilter.apply(stack);

        return super.shouldVoidItem(slot, stack);
    }

    public void setVoidingItems(boolean voiding) {
        voidingItems = voiding;
    }

    public void setItemFilter(@Nullable Predicate<ItemStack> filter) {
        itemFilter = filter;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if ( !isSlotUnlocked(slot) )
            return false;

        if ( !voidingItems && itemFilter != null && !itemFilter.apply(stack) )
            return false;

        if ( slot == getModuleOffset() )
            return isValidModule(stack);

        else if ( slot == getModifierOffset() )
            return behavior != null && behavior.isValidModifier(stack);

        else if ( slot >= getInputOffset() && slot < getOutputOffset() )
            return behavior != null && behavior.isValidInput(stack, slot - getInputOffset());

        return temporarilyAllowInsertion;
    }

    @Override
    public int getStackLimit(int slot) {
        if ( slot == getModuleOffset() || slot == getModifierOffset() )
            return 1;

        if ( behavior != null && slot >= getInputOffset() && slot < getOutputOffset() )
            return behavior.getInputLimit(slot - getInputOffset());

        return super.getStackLimit(slot);
    }

    @Override
    public boolean isSlotUnlocked(int slot) {
        if ( slot == getModuleOffset() ) {
            if ( !itemStackHandler.getStackInSlot(getModifierOffset()).isEmpty() )
                return false;

            if ( fluidGen )
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

        if ( behavior != null && slot >= getInputOffset() && slot < getOutputOffset() )
            behavior.updateInput(slot - getInputOffset());

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

    @Override
    public boolean updateBaseEnergy() {
        int newAddition = augmentEnergy + moduleEnergy;
        int newDrain = augmentDrain + moduleDrain;
        double newMultiplier = augmentMultiplier + moduleMultiplier;

        boolean changed = newAddition != energyAddition || newDrain != energyDrain || newMultiplier != energyMultiplier;

        energyAddition = newAddition;
        energyMultiplier = newMultiplier;
        energyDrain = newDrain;

        int newEnergy = (int) Math.floor(energyAddition * energyMultiplier);
        if ( newEnergy < 0 )
            newEnergy = 0;

        changed |= newEnergy != baseEnergy;
        baseEnergy = newEnergy;

        if ( changed )
            energyChanged();
        return changed;
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

    public void updateTargetEnergy() {
        if ( world == null || world.isRemote )
            return;

        maxEnergyPerEntity = 0;
        maxEnergyPerBlock = 0;

        List<VaporizerTarget> targets = worker.getTargetCache();
        if ( targets == null )
            return;

        int dimension = world.provider.getDimension();

        for (VaporizerTarget target : targets) {
            target.cost = target.pos == null ? 0 : (int) (getEnergyCost(target.pos, target.source) * energyMultiplier);

            int blockCost = target.cost + baseEnergy;
            int entityCost = 0;

            if ( behavior != null ) {
                BlockPosDimension pos = target.pos;
                int dim = pos.getDimension();
                World world;
                if ( dim == dimension )
                    world = this.world;
                else
                    world = DimensionManager.getWorld(dim);

                if ( world != null )
                    blockCost += (int) (behavior.getBlockEnergyCost(target, world) * energyMultiplier);

                entityCost += (int) (behavior.getMaxEntityEnergyCost(target) * energyMultiplier);
            }

            if ( blockCost > maxEnergyPerBlock )
                maxEnergyPerBlock = blockCost;

            if ( entityCost > maxEnergyPerEntity )
                maxEnergyPerEntity = entityCost;
        }
    }

    public void updateModule() {
        ItemStack stack = itemStackHandler.getStackInSlot(getModuleOffset());
        if ( stack.isEmpty() || !isValidModule(stack) ) {
            if ( behavior != null )
                behavior.onRemove();

            behavior = null;
            previousModule = null;
            return;
        }

        ItemModule module = (ItemModule) stack.getItem();

        if ( module == previousModule && behavior != null ) {
            behavior.updateModule(stack);
        } else {
            if ( behavior != null )
                behavior.onRemove();

            behavior = module.getBehavior(stack, this);
        }

        if ( behavior != null ) {
            moduleEnergy = behavior.getEnergyAddition();
            moduleMultiplier = behavior.getEnergyMultiplier();
            moduleDrain = behavior.getEnergyDrain();
        }

        previousModule = module;
        updateBaseEnergy();
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
        updateTargetEnergy();
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

        tank.setCapacity(calculateFluidCapacity());

        budgetPerTick = level.vaporizerBudgetPerTick;
        maximumBudget = level.vaporizerMaxBudget;

        if ( behavior != null )
            behavior.updateAugmentsAndLevel();

        updateTargetEnergy();
    }

    @Override
    public void updateAugmentStatus() {
        super.updateAugmentStatus();

        if ( behavior != null )
            behavior.updateAugmentsAndLevel();

        updateTargetEnergy();
    }

    @Override
    public boolean isValidAugment(int slot, ItemStack augment) {
        if ( augment.getItem() == ModItems.itemFluidGenAugment ) {
            if ( !wantsFluid() )
                return false;

            FluidStack fluid = ModItems.itemFluidGenAugment.getFluid(augment);
            if ( fluid == null || !isFluidValid(fluid.getFluid()) )
                return false;
        }

        return super.isValidAugment(slot, augment);
    }

    @Override
    public boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement) {
        if ( behavior != null && !behavior.canRemoveAugment(player, slot, augment, replacement) )
            return false;

        return super.canRemoveAugment(player, slot, augment, replacement);
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

    /* Budget Info */

    public int getBudgetCurrent() {
        return remainingBudget;
    }

    public int getBudgetMax() {
        return maximumBudget;
    }

    public int getBudgetPerTick() {
        return budgetPerTick;
    }

    public int getBudgetPerOperation() {
        if ( behavior == null )
            return 0;

        return (int) (behavior.getActionCost() * augmentBudgetMult) + augmentBudgetAdd;
    }

    /* Work Info */

    @Override
    public boolean getWorkConfigured() {
        if ( behavior == null )
            return false;

        if ( getBudgetPerOperation() > maximumBudget )
            return false;

        return behavior.canRun(true);
    }

    @Nullable
    public String getWorkUnconfiguredExplanation() {
        if ( behavior == null )
            return "info." + WirelessUtils.MODID + ".vaporizer.missing_module";

        if ( getBudgetPerOperation() > maximumBudget )
            return "info." + WirelessUtils.MODID + ".vaporizer.insufficient_budget";

        return behavior.getUnconfiguredExplanation();
    }

    @Override
    public String formatWorkUnit(double value) {
        String unit;
        if ( value != 0 && value < 1 ) {
            value *= 20;
            if ( value < 1 ) {
                value = 1 / value;
                unit = StringHelper.localize("info." + WirelessUtils.MODID + ".vaporizer_unit.item");
            } else
                unit = StringHelper.localize("info." + WirelessUtils.MODID + ".vaporizer_unit.second");

        } else
            unit = StringHelper.localize("info." + WirelessUtils.MODID + ".vaporizer_unit.tick");

        if ( value == Math.floor(value) )
            return StringHelper.isShiftKeyDown() || value < 1000 ?
                    StringHelper.formatNumber((long) value) + " " + unit :
                    TextHelpers.getScaledNumber((long) value, unit, true);

        return String.format("%.2f %s", value, unit);
    }

    public String getWorkUnit() {
        return StringHelper.localize("info." + WirelessUtils.MODID + ".vaporizer_unit.tick");
    }

    public boolean hasSustainedRate() {
        return maximumBudget != budgetPerTick;
    }

    @Override
    public double getWorkSustainedRate() {
        int budget = 0;
        if ( behavior != null )
            budget = getBudgetPerOperation();

        if ( budget == 0 )
            return 0;

        double count;
        if ( budget == 1 )
            count = budgetPerTick;
        else {
            long budgetPerSecond = budgetPerTick * 20L;
            count = (budgetPerSecond / (double) budget) / 20;
        }

        if ( count > level.maxVaporizerEntities )
            return level.maxVaporizerEntities;

        return count;
    }

    public double getWorkMaxRate() {
        int budget = 0;
        if ( behavior != null )
            budget = getBudgetPerOperation();

        if ( budget == 0 )
            return 0;

        double count;
        if ( budget == 1 )
            count = maximumBudget;
        else
            count = maximumBudget / (double) budget;

        if ( count > level.maxVaporizerEntities )
            return level.maxVaporizerEntities;

        return count;
    }

    @Override
    public double getWorkLastTick() {
        return remainingPerTick;
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
        if ( hasFluid() && !hasSpecialFluid() ) {
            Fluid fillFluid;
            FluidStack existing = tank.getFluid();
            int rate;

            if ( existing == null || existing.amount == 0 ) {
                fillFluid = lastFluid;
                if ( fillFluid == null )
                    fillFluid = getExperienceFluid();

                rate = getFluidRate(fillFluid);
                if ( rate == 0 ) {
                    fillFluid = getExperienceFluid();
                    rate = getFluidRate(fillFluid);
                }

            } else {
                fillFluid = existing.getFluid();
                rate = fluidRate;
            }

            if ( fillFluid != null && rate != 0 ) {
                int room = Math.floorDiv(tank.getCapacity() - tank.getFluidAmount(), rate);
                if ( room > 0 ) {
                    lastFluid = fillFluid;
                    if ( fluidRate == 0 )
                        fluidRate = rate;

                    if ( room < amount ) {
                        tank.fill(new FluidStack(fillFluid, room * rate), true);
                        amount -= room;

                    } else {
                        tank.fill(new FluidStack(fillFluid, amount * rate), true);
                        return;
                    }
                }
            }
        }

        excessFuel += amount;
    }

    public int removeFuel(int amount) {
        // This is always dirtying.
        markChunkDirty();

        int used = 0;

        // Do we have excess fuel?
        if ( excessFuel > amount ) {
            excessFuel -= amount;
            markChunkDirty();
            return amount;

        } else if ( excessFuel > 0 ) {
            amount -= excessFuel;
            used = excessFuel;
            excessFuel = 0;
        }

        // Use fluid before burning items.
        if ( amount > 0 && hasFluid() && !hasSpecialFluid() ) {
            FluidStack existing = tank.getFluid();
            if ( existing != null && fluidRate != 0 ) {
                int points = Math.min(amount, Math.floorDiv(tank.getFluidAmount(), fluidRate));
                if ( points > 0 ) {
                    tank.drain(points * fluidRate, true);
                    used += points;
                    amount -= points;
                }
            }
        }

        // Burn items for fluid.
        if ( ModConfig.vaporizers.useEntitiesFuel && amount > 0 && hasInput() && hasEmptyOutput() ) {
            for (int i = 0; i < inputProxy.getSlots(); i++) {
                ItemStack slotted = inputProxy.extractItem(i, 64, true);
                if ( !EntityUtilities.canEmptyBall(slotted) )
                    continue;

                int value = EntityUtilities.getBaseExperience(slotted, world);
                if ( EntityUtilities.containsBabyEntity(slotted) )
                    value = (int) Math.floor(value * ModConfig.vaporizers.babyMultiplier);

                if ( value == 0 )
                    continue;

                // We do this after checking if the value is zero as a zero value is how we disallow
                // certain mobs from being used.
                value += ModConfig.vaporizers.entityAddition;

                int number = Math.min(slotted.getCount(), (int) Math.ceil((double) amount / value));
                if ( number == 0 )
                    continue;

                ItemStack output = EntityUtilities.removeEntity(slotted.copy());
                output = insertOutputStack(output, true);

                if ( !output.isEmpty() )
                    number -= output.getCount();

                slotted = inputProxy.extractItem(i, number, false);
                if ( slotted.getCount() < number )
                    number = slotted.getCount();

                amount -= number * value;
                used += number * value;

                output = EntityUtilities.removeEntity(slotted.copy());
                output = insertOutputStack(output, false);

                if ( !output.isEmpty() )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(output, world, pos);
            }
        }

        return used;
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
        int cost = target == null ? 0 : (int) (getEnergyCost(target, source) * energyMultiplier);
        VaporizerTarget info = new VaporizerTarget(target, source, tile, entity, cost);

        int blockCost = info.cost + baseEnergy;
        int entityCost = 0;

        if ( behavior != null ) {
            blockCost += (int) (behavior.getBlockEnergyCost(info, world) * energyMultiplier);
            entityCost += (int) (behavior.getMaxEntityEnergyCost(info) * energyMultiplier);
        }

        if ( blockCost > maxEnergyPerBlock )
            maxEnergyPerBlock = blockCost;

        if ( entityCost > maxEnergyPerEntity )
            maxEnergyPerEntity = entityCost;

        return info;
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

        if ( world != null && !world.isRemote ) {
            validTargetsPerTick = 0;
            maxEnergyPerBlock = 0;
            maxEnergyPerEntity = 0;
        }

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
        int fullCost = target.cost + baseEnergy + (int) (behavior.getBlockEnergyCost(target, world) * energyMultiplier);
        int actionCost = getBudgetPerOperation();
        if ( remainingBudget < actionCost )
            return WorkResult.FAILURE_STOP_IN_PLACE;

        int stored = getEnergyStored();
        if ( stored < fullCost ) {
            if ( stored < baseEnergy )
                return WorkResult.FAILURE_STOP_IN_PLACE;
            return WorkResult.FAILURE_CONTINUE;
        }

        WorkResult result = behavior.processBlock(target, world);
        boolean worked = result.success;
        boolean stop = !result.keepProcessing;
        boolean noAdvance = result.noAdvance;

        if ( result.success ) {
            activeTargetsPerTick++;
            remainingPerTick--;

            stored = getEnergyStored();
            remainingBudget -= actionCost;
            if ( stored < fullCost || remainingBudget < actionCost || remainingPerTick <= 0 ) {
                stop = true;
                noAdvance = true;
            }
        }

        if ( !result.remove )
            validTargetsPerTick++;

        if ( !didFullEntities && !stop ) {
            Class<? extends Entity> klass = behavior.getEntityClass();
            Predicate<? super Entity> filter = behavior.getEntityFilter();

            if ( klass != null ) {
                AxisAlignedBB box = null;
                HashSet<BlockPos> visitedTargets = null;

                if ( canGetFullEntities() ) {
                    didFullEntities = true;
                    visitedTargets = new HashSet<>();
                    if ( worked )
                        visitedTargets.add(target.pos);

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
                    if ( visitedTargets != null ) {
                        BlockPos pos = entity.getPosition();
                        if ( !visitedTargets.contains(pos) ) {
                            int cost = fullCost + (int) (getEnergyCost(pos.getDistance(pos.getX(), pos.getY(), pos.getZ()), false) * energyMultiplier);
                            if ( cost > stored )
                                continue;

                            fullCost = cost;
                            visitedTargets.add(pos);
                        }
                    }

                    int cost = fullCost + (int) (behavior.getEntityEnergyCost(entity, target) * energyMultiplier);
                    if ( cost > stored )
                        continue;

                    result = behavior.processEntity(entity, target);
                    if ( result.success ) {
                        worked = true;
                        remainingBudget -= actionCost;
                        activeTargetsPerTick++;
                        fullCost = cost;
                    }

                    remainingPerTick--;
                    if ( remainingBudget < actionCost || remainingPerTick <= 0 ) {
                        noAdvance = true;
                        stop = true;
                        break;

                    } else {
                        noAdvance = result.noAdvance;

                        if ( !result.keepProcessing ) {
                            stop = true;
                            break;
                        }
                    }
                }
            }
        }

        if ( worked ) {
            extractEnergy(fullCost, false);
            if ( getEnergyStored() < baseEnergy || remainingBudget < actionCost || remainingPerTick <= 0 )
                stop = true;

            if ( noAdvance && stop )
                return WorkResult.SUCCESS_STOP_IN_PLACE;

            else if ( noAdvance )
                return WorkResult.SUCCESS_REPEAT;

            return stop ? WorkResult.SUCCESS_STOP : WorkResult.SUCCESS_CONTINUE;
        }

        if ( noAdvance && stop )
            return WorkResult.FAILURE_STOP_IN_PLACE;

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

    /* Effects */

    @Override
    public void performEffect(@Nonnull VaporizerTarget target, @Nonnull World world, boolean isEntity) {

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
        if ( behavior == null )
            return 0;

        int actionCost = getBudgetPerOperation();
        int rate;
        if ( actionCost == 0 )
            rate = Integer.MAX_VALUE;
        else
            rate = Math.floorDiv(maximumBudget, actionCost);

        if ( rate > level.maxVaporizerEntities )
            rate = level.maxVaporizerEntities;

        if ( rate < 1 )
            return 0;

        return energyDrain + (fluidGen ? fluidGenCost : 0) + (rate * (maxEnergyPerBlock + maxEnergyPerEntity));
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

        if ( remainingBudget < maximumBudget ) {
            if ( remainingBudget < 0 )
                remainingBudget = 0;
            remainingBudget += budgetPerTick;
            if ( remainingBudget > maximumBudget )
                remainingBudget = maximumBudget;
        }

        if ( gatherTick > 0 )
            gatherTick--;

        activeTargetsPerTick = 0;
        validTargetsPerTick = 0;
        energyPerTick = 0;
        remainingPerTick = 0;

        boolean enabled = redstoneControlOrDisable();

        if ( enabled && sideTransferAugment )
            executeSidedTransfer();

        if ( enabled && (gatherTick != 0 || behavior == null || getBudgetPerOperation() > remainingBudget || !behavior.canRun()) )
            enabled = false;

        if ( enabled && energyDrain > 0 ) {
            if ( energyDrain > getEnergyStored() )
                enabled = false;
            else
                extractEnergy(energyDrain, false);
        }

        if ( fluidGen && getEnergyStored() >= fluidGenCost ) {
            int filled = fluidHandler.fill(fluidGenStack, true);
            if ( filled > 0 )
                extractEnergy(fluidGenCost, false);
        }

        if ( !enabled || getEnergyStored() < baseEnergy ) {
            if ( wantTargets ) {
                wantTargets = false;
                worker.updateTargetCache();
            }

            tickInactive();
            setActive(false);
            updateTrackers();
            saveEnergyHistory(energyPerTick);
            return;
        }

        gatherTick = (byte) getActualWorldTickRate();

        int total = level.maxVaporizerEntities;
        remainingPerTick = total;
        tickActive();
        behavior.preWork();
        setActive(worker.performWork());
        updateTrackers();

        saveEnergyHistory(energyPerTick);
        remainingPerTick = total - remainingPerTick;
    }

    /* Event Handling */

    public void onItemDrops(LivingDropsEvent event) {
        int mode = behavior == null ? 0 : behavior.getDropMode();
        if ( mode == 0 )
            return;

        Entity entity = event.getEntity();
        boolean player = ModConfig.vaporizers.modules.slaughter.neverVoidPlayers && entity instanceof EntityPlayer;

        List<EntityItem> drops = event.getDrops();
        if ( mode == 3 ) {
            if ( !player )
                drops.clear();
            return;
        }

        drops.removeIf(item -> {
            ItemStack stack = insertOutputStack(item.getItem());
            if ( (mode == 2 && !player) || stack.isEmpty() )
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

        if ( !hasFluid() || hasSpecialFluid() )
            return;

        FluidStack existing = tank.getFluid();
        Fluid fillFluid;
        int rate;

        if ( existing == null || existing.amount == 0 ) {
            fillFluid = lastFluid;
            if ( fillFluid == null )
                fillFluid = getExperienceFluid();

            rate = getFluidRate(fillFluid);
            if ( rate == 0 ) {
                fillFluid = getExperienceFluid();
                rate = getFluidRate(fillFluid);
            }

        } else {
            fillFluid = existing.getFluid();
            rate = fluidRate;
        }

        int remaining = event.getDroppedExperience();

        if ( fillFluid != null && rate != 0 ) {
            int amount = rate * remaining;
            int used = tank.fill(new FluidStack(fillFluid, amount), true);
            if ( used > 0 ) {
                markChunkDirty();
                lastFluid = fillFluid;
                if ( fluidRate == 0 )
                    fluidRate = rate;
            }

            remaining = Math.floorDiv(amount - used, rate);
        }

        if ( mode == 2 || remaining == 0 )
            event.setCanceled(true);
        else
            event.setDroppedExperience(remaining);
    }

    /* NBT Read and Write */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        remainingBudget = tag.getInteger("Budget");
        gatherTick = tag.getByte("GatherTick");
        excessFuel = tag.getInteger("ExcessFuel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setInteger("Budget", remainingBudget);
        tag.setByte("GatherTick", gatherTick);
        tag.setInteger("ExcessFuel", excessFuel);
        return tag;
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);

        if ( gatherTickRate != -1 )
            tag.setInteger("WorldTickRate", gatherTickRate);

        tank.writeToNBT(tag);
        if ( lastFluid != null )
            tag.setString("LastFluid", lastFluid.getName());

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

        tank.readFromNBT(tag);

        fluidRate = 0;
        FluidStack fluid = tank.getFluid();
        if ( fluid != null && fluid.amount > 0 ) {
            lastFluid = fluid.getFluid();
            fluidRate = getFluidRate(lastFluid);
        } else if ( tag.hasKey("LastFluid") ) {
            lastFluid = FluidRegistry.getFluid(tag.getString("LastFluid"));
        } else
            lastFluid = null;

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = Mode.byIndex(tag.getByte("TransferSide" + i));

        updateTextures();
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();

        wantTargets = true;

        payload.addFluidStack(tank.getFluid());
        payload.addInt(fluidRate);

        payload.addByte(iterationMode.ordinal());
        payload.addShort(validTargetsPerTick);
        payload.addShort(activeTargetsPerTick);
        payload.addInt(maxEnergyPerBlock);
        payload.addInt(maxEnergyPerEntity);
        payload.addInt(gatherTickRate);
        payload.addInt(remainingPerTick);
        payload.addInt(remainingBudget);

        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);

        tank.setFluid(payload.getFluidStack());
        fluidRate = payload.getInt();

        setIterationMode(IterationMode.fromInt(payload.getByte()));
        validTargetsPerTick = payload.getShort();
        activeTargetsPerTick = payload.getShort();
        maxEnergyPerBlock = payload.getInt();
        maxEnergyPerEntity = payload.getInt();
        setWorldTickRate(payload.getInt());
        remainingPerTick = payload.getInt();
        remainingBudget = payload.getInt();
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();

        payload.addByte(fluidSwap);
        payload.addByte(iterationMode.ordinal());
        payload.addInt(gatherTickRate);

        for (int i = 0; i < sideTransfer.length; i++)
            payload.addByte(sideTransfer[i].index);

        if ( behavior != null )
            behavior.updateModePacket(payload);

        fluidSwap = 0;

        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);

        byte fluidSwap = payload.getByte();
        if ( fluidSwap != 0 )
            performFluidSwap(fluidSwap);

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
        public int cost;

        public VaporizerTarget(BlockPosDimension pos, ItemStack source, TileEntity tile, Entity entity, int cost) {
            super(pos, source, tile, entity);
            this.cost = cost;
        }

        @Override
        public MoreObjects.ToStringHelper getStringBuilder() {
            return super.getStringBuilder().add("cost", cost);
        }
    }

    /* Passive Item Handler */

    private static class PassiveItemHandler implements IItemHandler {
        public final ItemStackHandler handler;

        public final int offset;
        public final int slots;

        public PassiveItemHandler(TileBaseVaporizer vaporizer) {
            handler = vaporizer.itemStackHandler;
            offset = vaporizer.getInputOffset();
            slots = 16;
        }

        public int getSlots() {
            return slots;
        }

        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            if ( slot < 0 || slot >= slots )
                return ItemStack.EMPTY;

            return handler.getStackInSlot(slot + offset);
        }

        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if ( slot < 0 || slot >= 8 )
                return stack;

            return handler.insertItem(slot + offset, stack, simulate);
        }

        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if ( handler == null || slot < 8 || slot >= slots )
                return ItemStack.EMPTY;

            return handler.extractItem(slot + offset, amount, simulate);
        }

        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if ( slot < 0 || slot >= slots )
                return false;

            return handler.isItemValid(slot + offset, stack);
        }

        public int getSlotLimit(int slot) {
            if ( slot < 0 || slot >= slots )
                return 0;

            return handler.getSlotLimit(slot + offset);
        }
    }

    @Override
    public void onRenderAreasCleared() {
        super.onRenderAreasCleared();
        if ( behavior != null )
            behavior.onRenderAreasCleared();
    }

    @Override
    public void onRenderAreasEnabled() {
        super.onRenderAreasEnabled();
        if ( behavior != null )
            behavior.onRenderAreasEnabled();
    }

    @Override
    public void onRenderAreasDisabled() {
        super.onRenderAreasDisabled();
        if ( behavior != null )
            behavior.onRenderAreasDisabled();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( behavior != null )
            behavior.onDestroy();
    }

    @Override
    public void onInactive() {
        super.onInactive();
        if ( behavior != null )
            behavior.onInactive();
    }

    /* Behaviors */

    public interface IVaporizerBehavior {

        default void onInactive() {

        }

        default void onDestroy() {

        }

        default void onRemove() {

        }

        default void onRenderAreasEnabled() {

        }

        default void onRenderAreasDisabled() {

        }

        default void onRenderAreasCleared() {

        }

        @Nonnull
        default ItemStack pickGhost(ItemStack[] ghosts) {
            return pickGhost(ghosts, 0);
        }

        @Nonnull
        default ItemStack pickGhost(ItemStack[] ghosts, int offset) {
            if ( ghosts == null || ghosts.length == 0 )
                return ItemStack.EMPTY;

            long now = (Minecraft.getSystemTime() / 1000) + offset;
            ItemStack out = ghosts[(int) now % ghosts.length];
            if ( out == null )
                return ItemStack.EMPTY;

            return out;
        }

        default void debugPrint() {

        }

        @Nullable
        default String getUnconfiguredExplanation() {
            return null;
        }

        void updateModule(@Nonnull ItemStack stack);

        boolean isModifierUnlocked();

        boolean isValidModifier(@Nonnull ItemStack stack);

        @Nonnull
        ItemStack getModifierGhost();

        void updateModifier(@Nonnull ItemStack stack);

        boolean isInputUnlocked(int slot);

        boolean isValidInput(@Nonnull ItemStack stack, int slot);

        default int getInputLimit(int slot) {
            return 64;
        }

        @Nonnull
        default ItemStack getInputGhost(int slot) {
            return ItemStack.EMPTY;
        }

        default void updateInput(int slot) {

        }

        default boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement) {
            return true;
        }

        default void updateAugmentsAndLevel() {

        }

        default void getItemToolTip(@Nonnull List<String> tooltip, @Nullable Slot slot, @Nonnull ItemStack stack) {

        }

        ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui);

        default void updateModePacket(@Nonnull PacketBase packet) {

        }

        default void handleModePacket(@Nonnull PacketBase packet) {

        }

        default boolean hasSpecialFluid() {
            return false;
        }

        default boolean isFluidValid(Fluid fluid) {
            return false;
        }

        @Nullable
        default FluidStack getSpecialFluid() {
            return null;
        }

        boolean wantsFluid();

        default boolean canRun() {
            return canRun(false);
        }

        boolean canRun(boolean ignorePower);

        int getExperienceMode();

        int getDropMode();

        @Nullable
        Class<? extends Entity> getEntityClass();

        @Nullable
        Predicate<? super Entity> getEntityFilter();

        int getBlockEnergyCost(@Nonnull VaporizerTarget target, @Nonnull World world);

        int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull VaporizerTarget target);

        int getMaxEntityEnergyCost(@Nonnull VaporizerTarget target);

        default double getEnergyMultiplier() {
            return 1;
        }

        default int getEnergyAddition() {
            return 0;
        }

        default int getEnergyDrain() {
            return 0;
        }

        int getActionCost();

        default void preWork() {

        }

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

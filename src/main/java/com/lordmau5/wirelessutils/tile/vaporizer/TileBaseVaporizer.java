package com.lordmau5.wirelessutils.tile.vaporizer;

import cofh.core.network.PacketBase;
import com.google.common.base.MoreObjects;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.ISidedTransfer;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseEnergy;
import com.lordmau5.wirelessutils.tile.base.Worker;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public abstract class TileBaseVaporizer extends TileEntityBaseEnergy implements
        IWorkInfoProvider, ISidedTransfer, ISidedTransferAugmentable,
        IWorkProvider<TileBaseVaporizer.VaporizerTarget> {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected final Worker worker;

    protected IterationMode iterationMode = IterationMode.ROUND_ROBIN;

    private int activeTargetsPerTick;
    private int validTargetsPerTick;
    private int maxEnergyPerTick;

    private boolean sideTransferAugment = false;
    private ISidedTransfer.Mode[] sideTransfer;

    public TileBaseVaporizer() {
        super();
        sideTransfer = new ISidedTransfer.Mode[6];
        Arrays.fill(sideTransfer, ISidedTransfer.Mode.PASSIVE);
        worker = new Worker<>(this);

        updateTextures();
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("   Side Transfer: " + Arrays.toString(sideTransfer));
        System.out.println("   Valid Targets: " + validTargetsPerTick);
        System.out.println("  Active Targets: " + activeTargetsPerTick);
    }

    /* Augments */

    @Override
    public void updateLevel() {
        super.updateLevel();
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

        super.enableRenderAreas();
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

    @Nonnull
    public WorkResult performWorkBlock(@Nonnull VaporizerTarget target, @Nonnull World world, @Nullable IBlockState state, @Nullable TileEntity tile) {
        return WorkResult.FAILURE_REMOVE;
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
                        getTextureForMode(getSideTransferMode(side), false)
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

        // TODO: Inventory?
        // TODO: Fluid?
    }

    /* ITickable */

    @Override
    public void update() {
        super.update();

        worker.tickDown();
    }

    /* NBT Read and Write */

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);

        tag.setByte("IterationMode", (byte) iterationMode.ordinal());

        for (int i = 0; i < sideTransfer.length; i++)
            tag.setByte("TransferSide" + i, (byte) sideTransfer[i].index);

        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        iterationMode = IterationMode.fromInt(tag.getByte("IterationMode"));

        for (int i = 0; i < sideTransfer.length; i++)
            sideTransfer[i] = Mode.byIndex(tag.getByte("TransferSide" + i));

        updateTextures();
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();

        payload.addByte(iterationMode.ordinal());
        payload.addShort(validTargetsPerTick);
        payload.addShort(activeTargetsPerTick);
        payload.addInt(maxEnergyPerTick);

        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);

        setIterationMode(IterationMode.fromInt(payload.getByte()));
        validTargetsPerTick = payload.getShort();
        activeTargetsPerTick = payload.getShort();
        maxEnergyPerTick = payload.getInt();
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();

        payload.addByte(iterationMode.ordinal());

        for (int i = 0; i < sideTransfer.length; i++)
            payload.addByte(sideTransfer[i].index);

        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);

        setIterationMode(IterationMode.fromInt(payload.getByte()));

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
}

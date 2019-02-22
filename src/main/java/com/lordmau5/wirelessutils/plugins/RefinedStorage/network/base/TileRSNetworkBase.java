package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base;

import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeManager;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.api.util.Action;
import com.raoulvdberge.refinedstorage.apiimpl.util.OneSixMigrationHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class TileRSNetworkBase<N extends NetworkNodeBase> extends TileEntityBaseMachine implements
        IRangeAugmentable, ITickable,
        INetworkNodeProxy<N>, EventDispatcher.IEventListener {

    public List<BlockPosDimension> validTargets;
    private int energyCost;

    private boolean needsRecalculation;
    private int recalculationDelay = 10;

    private N clientNode;
    private EnumFacing directionToMigrate;

    public TileRSNetworkBase() {
        super();

        setWatchUnload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventDispatcher.PLACE_BLOCK.removeListener(this);
        EventDispatcher.BREAK_BLOCK.removeListener(this);

        rebuildGraphNetwork();
    }

    @Override
    public abstract void handleEvent(@Nonnull Event event);

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("Targets: " + (validTargets == null ? "null" : validTargets.size()));
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    public Iterable<BlockPosDimension> getTargets() {
        if ( validTargets == null )
            calculateAndRebuild();

        return validTargets;
    }

    @Override
    public void update() {
        if ( needsRecalculation && recalculationDelay-- < 0 ) {
            needsRecalculation = false;

            calculateAndRebuild();
        }
    }

    public void setNeedsRecalculation() {
        if ( world != null && world.isRemote ) {
            calculateTargets();
        } else {
            needsRecalculation = true;
            recalculationDelay = 10;
        }
    }

    @Override
    protected boolean sendRedstoneUpdates() {
        return true;
    }

    @Override
    public void onRedstoneUpdate() {
        getNode().onConnectedStateChange(getNode().getNetwork() != null);
        setNeedsRecalculation();
    }

    public void rebuildGraphNetwork() {
        if ( getNode().getNetwork() != null && getNode().getNetwork().getNodeGraph() != null ) {
            World world = getNode().getNetwork().world();
            BlockPos pos = getNode().getNetwork().getPosition();
            getNode().getNetwork().getNodeGraph().invalidate(Action.PERFORM, world, pos);
        }
    }

    @Override
    public void validate() {
        super.validate();

        setNeedsRecalculation();
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }

    public abstract int calculateEnergyCost(double distance, boolean isInterdimensional);

    public boolean isNodeValid(BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if ( tile == null )
            return false;

        return tile.hasCapability(RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY, null);
    }

    /* Targeting */

    @Override
    public void enableRenderAreas(boolean enabled) {
        // Make sure we've run calculateTargets at least once.
        if ( enabled )
            getTargets();

        super.enableRenderAreas(enabled);
    }

    public abstract void calculateTargets();

    public void calculateAndRebuild() {
        calculateTargets();
        rebuildGraphNetwork();
    }

    /* NBT Save and Load */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if ( tag.hasKey("DirectionToMigrate") ) {
            directionToMigrate = EnumFacing.values()[tag.getByte("DirectionToMigrate")];
        }
    }

    @Nonnull
    @Override
    public N getNode() {
        if ( world.isRemote ) {
            if ( clientNode == null ) {
                clientNode = createNode(world, pos);
            }

            return clientNode;
        }

        INetworkNodeManager manager = RefinedStoragePlugin.RSAPI.getNetworkNodeManager(world);

        INetworkNode node = manager.getNode(pos);

        if ( node == null || !node.getId().equals(getNodeId()) ) {
            manager.setNode(pos, node = createNode(world, pos));
            manager.markForSaving();
        }

        OneSixMigrationHelper.removalHook();
        if ( directionToMigrate != null ) {
            ((N) node).setDirection(directionToMigrate);

            directionToMigrate = null;

            markDirty();
        }

        return (N) node;
    }

    public abstract N createNode(World world, BlockPos pos);

    public abstract String getNodeId();


    /* Capabilities */
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side) {
        if ( capability == RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY ) {
            return true;
        }

        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
        if ( capability == RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY ) {
            return RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }
}

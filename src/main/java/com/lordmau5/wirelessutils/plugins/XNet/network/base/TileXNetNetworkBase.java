package com.lordmau5.wirelessutils.plugins.XNet.network.base;

import com.lordmau5.wirelessutils.plugins.XNet.XNetPlugin;
import com.lordmau5.wirelessutils.tile.base.ITargetProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import mcjty.xnet.api.channels.IConnectable;
import mcjty.xnet.api.net.IWorldBlob;
import mcjty.xnet.api.tiles.IConnectorTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TileXNetNetworkBase extends TileEntityBaseMachine implements
        IRangeAugmentable, ITickable, ITargetProvider,
        IConnectable, EventDispatcher.IEventListener {

    public List<BlockPosDimension> validTargets;

    private boolean needsRecalculation;
    private int recalculationDelay = 10;

    public TileXNetNetworkBase() {
        super();

        setWatchUnload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventDispatcher.PLACE_BLOCK.removeListener(this);
        EventDispatcher.BREAK_BLOCK.removeListener(this);
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
            calculateTargetsAndMarkDirty();

        return validTargets;
    }

    @Override
    public void update() {
        if ( needsRecalculation && recalculationDelay-- < 0 ) {
            needsRecalculation = false;

            calculateTargetsAndMarkDirty();
        }
    }

    public void setNeedsRecalculation() {
        needsRecalculation = true;
        recalculationDelay = 10;
    }

    @Override
    protected boolean sendRedstoneUpdates() {
        return true;
    }

    @Override
    public void onRedstoneUpdate() {
        setNeedsRecalculation();
    }

    @Override
    public void validate() {
        super.validate();

        setNeedsRecalculation();
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

    public void calculateTargetsAndMarkDirty() {
        calculateTargets();

        if ( !world.isRemote ) {
            IWorldBlob blob = XNetPlugin.XNetAPI.getWorldBlob(world);
            if ( blob != null ) {
                blob.getNetworksAt(pos).forEach(blob::markNetworkDirty);
            }
        }

    }
    /* IConnectable */

    public ConnectResult canConnect(@Nonnull IBlockAccess access, @Nonnull BlockPos connectorPos, @Nonnull BlockPos blockPos,
                                    @Nullable TileEntity tileEntity,
                                    @Nonnull EnumFacing facing) {
        return ConnectResult.YES;
    }

    public boolean isConnectorTile(BlockPosDimension pos) {
        World world = pos.getDimension() == getWorld().provider.getDimension()
                ? getWorld()
                : DimensionManager.getWorld(pos.getDimension());

        if ( world == null )
            return false;

        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof IConnectorTile;
    }

    /* IConsumerProvider */

    public Set<BlockPos> getConsumers() {
        Set<BlockPos> consumers = Collections.emptySet();
        if ( validTargets != null ) {
            consumers = new HashSet<>(validTargets);
        }

        return consumers;
    }
}

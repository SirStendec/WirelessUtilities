package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.*;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import cofh.core.util.helpers.StringHelper;
import com.google.common.collect.Lists;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.ITargetProvider;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public abstract class TileAENetworkBase extends TileEntityBaseMachine implements
        IRangeAugmentable, ITickable,
        IGridHost, IGridBlock,
        EventDispatcher.IEventListener, ITileInfoProvider {

    public int IDLE_ENERGY_COST = 2;

    private Map<BlockPosDimension, IGridConnection> connections;
    private Map<BlockPosDimension, Integer> placedCacheMap;
    public List<BlockPosDimension> validTargets;
    private int energyCost;

    private boolean needsRecalculation;
    private int recalculationDelay = 10;

    private int activeUpdateDelay = 10;

    private IGridNode node;
    private int lastUsedChannels = 0;

    public TileAENetworkBase() {
        super();

        setWatchUnload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventDispatcher.PLACE_BLOCK.removeListener(this);
        EventDispatcher.BREAK_BLOCK.removeListener(this);

        unregisterNode();
    }

    @Override
    public void onChunkUnload() {
        unregisterNode();

        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        unregisterNode();

        super.invalidate();
    }

    @Override
    public abstract void handleEvent(@Nonnull Event event);


    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("Targets: " + (validTargets == null ? "null" : validTargets.size()));
        System.out.println("Energy Cost: " + getEnergyCost());
    }

    public BlockPosDimension getPosition() {
        if ( !hasWorld() )
            return null;

        return new BlockPosDimension(getPos(), getWorld().provider.getDimension());
    }

    public Iterable<BlockPosDimension> getTargets() {
        if ( validTargets == null )
            calculateTargets();

        return validTargets;
    }

    @Override
    public void update() {
        if ( needsRecalculation && recalculationDelay-- < 0 ) {
            needsRecalculation = false;

            calculateTargets();

            if ( getNode() != null ) {
                getNode().updateState();
            }
        }

        if ( activeUpdateDelay-- < 0 ) {
            activeUpdateDelay = 10;

            updateActiveState();
        }

        if ( getNode() != null && getNode().isActive() && !getPlacedCacheMap().isEmpty() ) {
            List<BlockPosDimension> toRemove = new ArrayList<>();

            for (Map.Entry<BlockPosDimension, Integer> entry : getPlacedCacheMap().entrySet()) {
                IGridNode node = getGridNode(entry.getKey());
                if ( node != null ) {
                    addConnection(entry.getKey(), node);
                    toRemove.add(entry.getKey());
                    continue;
                } else if ( entry.getValue() > 5 ) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                getPlacedCacheMap().put(entry.getKey(), entry.getValue() + 1);
            }

            for (BlockPosDimension pos : toRemove) {
                getPlacedCacheMap().remove(pos);
            }
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
        super.onRedstoneUpdate();

        setNeedsRecalculation();
    }

    public void updateActiveState() {
        IGridNode node = getNode();
        boolean hasConnections = node != null && !getConnections().isEmpty();
        boolean isNodeActive = node != null && node.isActive();
        boolean redstone = redstoneControlOrDisable();
        setActive(node != null && isNodeActive && hasConnections && redstone);
    }

    @Override
    public void validate() {
        super.validate();

        setNeedsRecalculation();
    }

    public Map<BlockPosDimension, IGridConnection> getConnections() {
        if ( connections == null ) {
            connections = new Object2ObjectOpenHashMap<>();
        }
        return connections;
    }

    public void addConnection(BlockPosDimension pos, IGridNode otherNode) {
        if ( getConnections().containsKey(pos) ) {
            getConnections().get(pos).destroy();
        }

        if ( !validTargets.contains(pos) ) {
            return;
        }

        try {
            getConnections().put(pos, AEApi.instance().grid().createGridConnection(getNode(), otherNode));

            boolean sameDimension = pos.getDimension() == getWorld().provider.getDimension();
            double distance = getPos().getDistance(pos.getX(), pos.getY(), pos.getZ()) - 1;
            setEnergyCost(getEnergyCost() + calculateEnergyCost(distance, !sameDimension));
        } catch (FailedConnectionException e) {
            setNeedsRecalculation();
        }
    }

    public void destroyConnection(BlockPosDimension pos) {
        if ( getConnections().containsKey(pos) ) {
            IGridConnection connection = getConnections().get(pos);
            if ( connection != null ) {
                IGridHost host = connection.b().getMachine();
                if ( host instanceof TileAENetworkBase ) {
                    ((TileAENetworkBase) host).setNeedsRecalculation();
                }

                connection.destroy();
            }
            getConnections().remove(pos);

            boolean sameDimension = pos.getDimension() == getWorld().provider.getDimension();
            double distance = getPos().getDistance(pos.getX(), pos.getY(), pos.getZ()) - 1;
            setEnergyCost(getEnergyCost() - calculateEnergyCost(distance, !sameDimension));
        }
    }

    public void cachePlacedPosition(BlockPosDimension pos) {
        if ( validTargets.contains(pos) && !getPlacedCacheMap().containsKey(pos) ) {
            getPlacedCacheMap().put(pos, 0);
        }
    }

    public Map<BlockPosDimension, Integer> getPlacedCacheMap() {
        if ( placedCacheMap == null ) {
            placedCacheMap = new Object2ObjectOpenHashMap<>();
        }
        return placedCacheMap;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = Math.max(0, energyCost);

        if ( getNode() != null && getNode().getGrid() != null ) {
            getNode().getGrid().postEvent(new MENetworkPowerIdleChange(getNode()));
        }
    }

    public abstract int calculateEnergyCost(double distance, boolean isInterdimensional);

    @Override
    public void enableRenderAreas(boolean enabled) {
        if ( enabled )
            getTargets();

        super.enableRenderAreas(enabled);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if ( FMLCommonHandler.instance().getEffectiveSide().isServer() ) {
            unregisterNode();
            IGridNode node = getNode();

            if ( node != null && tag.hasKey("ae_node") ) {
                node.loadFromNBT("ae_node", tag);
            }

            setNeedsRecalculation();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        if ( FMLCommonHandler.instance().getEffectiveSide().isServer() ) {
            if ( node != null ) {
                node.saveToNBT("ae_node", tag);

                byte usedChannels = 0;
                for (IGridConnection con : getConnections().values()) {
                    usedChannels += con.getUsedChannels();
                }
                tag.setByte("channels", usedChannels);
            }
        }

        return tag;
    }

    public abstract void calculateTargets();

    @Override
    public List<String> getInfoTooltips(@Nullable NBTTagCompound tag) {
        List<String> tooltips = Lists.newArrayList();

        boolean dense = ModConfig.plugins.appliedEnergistics.denseCableConnection;

        if (tag == null) {
            if (getNode() != null && getNode().isActive()) {
                tag = new NBTTagCompound();
                writeToNBT(tag);
                lastUsedChannels = tag.getByte("channels");
            }
        } else {
            lastUsedChannels = tag.getByte("channels");
        }

        tooltips.add(new TextComponentTranslation("info." + WirelessUtils.MODID + ".ae2.channels", lastUsedChannels, dense ? 32 : 8).getUnformattedText());

        return tooltips;
    }

    @Override
    public NBTTagCompound getInfoNBT(NBTTagCompound tag) {
        NBTTagCompound tempTag = new NBTTagCompound();
        writeToNBT(tempTag);

        if(tempTag.hasKey("channels")) {
            byte usedChannels = tempTag.getByte("channels");
            tag.setByte("channels", usedChannels);
        }

        return tag;
    }

    /* Grid */

    private void unregisterNode() {
        if ( node != null ) {
            node.destroy();
            node = null;

            setNeedsRecalculation();
        }
    }

    private IGridNode getNode() {
        if ( getWorld() == null || getWorld().isRemote ) {
            return null;
        }

        if ( node == null ) {
            node = AEApi.instance().grid().createGridNode(this);
        }

        return node;
    }

    public IGridNode getGridNode(BlockPosDimension pos) {
        World world = pos.getDimension() == getWorld().provider.getDimension()
                ? getWorld()
                : DimensionManager.getWorld(pos.getDimension());

        if ( world == null )
            return null;

        TileEntity tile = world.getTileEntity(pos);
        if ( tile == null )
            return null;

        if ( tile instanceof IGridNode )
            return (IGridNode) tile;

        if ( tile instanceof IGridHost )
            return ((IGridHost) tile).getGridNode(AEPartLocation.INTERNAL);

        return null;
    }


    /* IGridHost */

    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation var1) {
        return getNode();
    }

    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation var1) {
        return ModConfig.plugins.appliedEnergistics.denseCableConnection ? AECableType.DENSE_COVERED : AECableType.COVERED;
    }

    @Override
    public void securityBreak() {
        // TODO
    }

    /* IGridBlock */

    @Override
    public double getIdlePowerUsage() {
        return getEnergyCost();
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(GridNotification gridNotification) {
        // Not used
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setNetworkStatus(IGrid grid, int status) {
        // Deprecated
    }

    @Override
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Override
    public IGridHost getMachine() {
        return this;
    }

    @Override
    public void gridChanged() {
        // Not used
    }

    @Override
    public abstract ItemStack getMachineRepresentation();
}

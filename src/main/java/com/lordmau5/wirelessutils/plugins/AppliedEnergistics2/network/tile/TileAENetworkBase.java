package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.worlddata.WorldData;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import com.google.common.base.MoreObjects;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AEColorHelpers;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TileAENetworkBase extends TileEntityBaseMachine implements
        IRangeAugmentable, ITickable,
        IGridHost, IGridBlock,
        EventDispatcher.IEventListener, ITileInfoProvider {

    private Map<BlockPosDimension, TargetInfo> connections = new Object2ObjectOpenHashMap<>();
    private Map<BlockPosDimension, ItemStack> sources = new Object2ObjectOpenHashMap<>();

    List<BlockPosDimension> validTargets;
    private Set<BlockPosDimension> staleTargets;
    private Map<BlockPosDimension, Byte> checkTargets;

    private int energyCost = 0;

    private boolean needsRecalculation;

    private AEColor aeColor = AEColor.TRANSPARENT;
    private boolean enabled = false;

    private IGridNode node;
    private int usedChannels = 0;
    private int usedConnections = 0;
    private int powerDraw = 0;

    public TileAENetworkBase() {
        super();
        setWatchUnload();
    }

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("    Enabled: " + enabled + " (RS:" + redstoneControlOrDisable() + ")");
        System.out.println("       Node: " + node);
        System.out.println("     Recalc: " + needsRecalculation);
        System.out.println("      Color: " + getAEColor().toString());
        System.out.println("Base Energy: " + baseEnergy);
        System.out.println("Energy Cost: " + energyCost);
        System.out.println("   Channels: " + usedChannels);
        System.out.println("    Targets: " + (validTargets == null ? "null" : validTargets.size()));
        if ( validTargets != null )
            for (int i = 0; i < validTargets.size(); i++)
                System.out.println("  " + i + ": " + validTargets.get(i));

        System.out.println("Connections: " + usedConnections);
        if ( connections != null ) {
            for (TargetInfo info : connections.values())
                System.out.println("  " + info);
        }
    }

    /* Life Cycle */

    @Override
    public void validate() {
        super.validate();

        if ( validTargets == null )
            recalculate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventDispatcher.PLACE_BLOCK.removeListener(this);
        EventDispatcher.BREAK_BLOCK.removeListener(this);

        unregisterNode();
    }

    @Override
    public void updateLevel() {
        super.updateLevel();

        if ( world != null && !world.isRemote && node != null ) {
            unregisterNode();
            recalculate();
        }
    }

    @Override
    public void handleEvent(@Nonnull Event rawEvent) {
        super.handleEvent(rawEvent);

        if ( enabled && validTargets != null && rawEvent instanceof BlockEvent ) {
            final BlockEvent event = (BlockEvent) rawEvent;
            final BlockPos pos = event.getPos();
            final World world = event.getWorld();

            if ( pos != null && world != null ) {
                final BlockPosDimension target = new BlockPosDimension(pos, world);
                if ( validTargets.contains(target) ) {
                    if ( event instanceof BlockEvent.PlaceEvent ) {
                        checkTarget(target);

                    } else if ( event instanceof BlockEvent.BreakEvent )
                        destroyConnection(target);
                }
            }
        }
    }

    /* Network Colors */

    public AEColor getAEColor() {
        if ( !ModConfig.plugins.appliedEnergistics.enableColor )
            return AEColor.TRANSPARENT;

        return aeColor;
    }

    public void setAEColor(AEColor color) {
        if ( aeColor == color )
            return;

        aeColor = color;

        if ( world != null && !world.isRemote ) {
            markChunkDirty();

            if ( ModConfig.plugins.appliedEnergistics.enableColor )
                recalculate();
        }

        if ( ModConfig.plugins.appliedEnergistics.enableColor ) {
            callBlockUpdate();
            callNeighborStateChange();
        }
    }

    public void setAEColor(int color) {
        setAEColor(AEColorHelpers.fromByte((byte) color));
    }

    /* Targeting */

    public BlockPosDimension getPosition() {
        if ( world == null || pos == null )
            return null;

        return new BlockPosDimension(pos, world.provider.getDimension());
    }

    public Iterable<BlockPosDimension> getTargets() {
        if ( validTargets == null )
            calculateTargets();

        return validTargets;
    }

    public void calculateTargets() {
        if ( world == null || pos == null || world.provider == null || !world.isBlockLoaded(pos) )
            return;

        staleTargets = new HashSet<>();
        if ( validTargets == null )
            validTargets = new ArrayList<>();
        else if ( !validTargets.isEmpty() ) {
            staleTargets.addAll(validTargets);
            validTargets.clear();
        }

        sources.clear();
        calculateTargetsDelegate();

        for (BlockPosDimension target : staleTargets) {
            if ( !world.isRemote ) {
                EventDispatcher.PLACE_BLOCK.removeListener(target, this);
                EventDispatcher.BREAK_BLOCK.removeListener(target, this);
                destroyConnection(target);
            }

            if ( checkTargets != null )
                checkTargets.remove(target);
        }

        staleTargets = null;
        needsRecalculation = false;
    }

    public void checkTarget(@Nonnull BlockPosDimension pos) {
        if ( validTargets == null || !validTargets.contains(pos) )
            return;

        if ( connections != null && connections.containsKey(pos) )
            return;

        if ( checkTargets == null )
            checkTargets = new Object2ByteOpenHashMap<>();

        checkTargets.put(pos, (byte) 0);
    }

    public void addValidTarget(@Nonnull BlockPosDimension pos, @Nonnull ItemStack stack) {
        if ( staleTargets == null )
            throw new RuntimeException("Called addValidTarget outside calculateTargetsDelegate");

        if ( pos.getFacing() != null )
            pos = pos.facing(null);

        pos = pos.toImmutable();

        final boolean stale = staleTargets.contains(pos);
        validTargets.add(pos);
        sources.put(pos, stack);

        if ( stale )
            staleTargets.remove(pos);
        else if ( world != null && !world.isRemote ) {
            if ( enabled )
                checkTarget(pos);

            EventDispatcher.PLACE_BLOCK.addListener(pos, this);
            EventDispatcher.BREAK_BLOCK.addListener(pos, this);
        }
    }

    public abstract void calculateTargetsDelegate();

    @Override
    public void enableRenderAreas(boolean enabled) {
        if ( enabled )
            getTargets();

        super.enableRenderAreas(enabled);
    }

    /* Power */

    public abstract int getBaseEnergy();

    @Override
    public boolean updateBaseEnergy() {
        int newEnergy = (int) ((getBaseEnergy() + augmentEnergy) * augmentMultiplier);
        if ( newEnergy < 0 )
            newEnergy = 0;

        if ( newEnergy == baseEnergy )
            return false;

        baseEnergy = newEnergy;
        energyChanged();
        return true;
    }

    @Override
    public void energyChanged() {
        if ( node != null ) {
            IGrid grid = node.getGrid();
            if ( grid != null )
                grid.postEvent(new MENetworkPowerIdleChange(node));
        }
    }

    public void setEnergyCost(int cost) {
        if ( cost < 0 )
            cost = 0;

        if ( energyCost == cost )
            return;

        energyCost = cost;
        energyChanged();
    }

    public int getPowerDraw() {
        if ( world == null || world.isRemote )
            return powerDraw;

        return (int) getIdlePowerUsage();
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
        BlockPosDimension self = getPosition();

        boolean interdimensional = self.getDimension() != target.getDimension();
        double distance = self.getDistance(target.getX(), target.getY(), target.getZ()) - 1;

        return getEnergyCost(distance, interdimensional);
    }

    public abstract int getEnergyCost(double distance, boolean interdimensional);


    /* The Update */

    @Override
    public void update() {
        super.update();

        // TODO: tickActive / tickInactive
        // And shutting down completely when we're off for an extended period.

        final IGridNode node = getNode();
        final boolean enabled = redstoneControlOrDisable() && node != null && node.isActive();
        final boolean connected = connections != null && !connections.isEmpty();

        setActive(enabled && connected);

        if ( timeCheckQuarter() )
            updateChannels();

        // If we aren't enabled, we don't want to update any connections.
        if ( !enabled ) {
            // If we were previously enabled, we want to destroy our old connections
            // and clear our queue.
            if ( this.enabled ) {
                updateBaseEnergy();
                clearConnections();
                checkTargets = null;
                this.enabled = false;
            }

            updateTrackers();
            return;
        }

        // Alright, so we are enabled.

        // If we weren't previously enabled, or if we've been told to recalculate,
        // we want to recalculate now.
        if ( !this.enabled || needsRecalculation ) {
            if ( !this.enabled )
                updateBaseEnergy();

            this.enabled = true;

            recalculate();
            updateTrackers();
            return;
        }

        // Still here? Then we want to check blocks we're interested in.
        handleWaitingTargets();
        updateTrackers();
    }


    /* Connection Management */

    public void scheduleRecalculate() {
        if ( world != null && world.isRemote )
            recalculate();
        else
            needsRecalculation = true;
    }

    public void recalculate() {
        calculateTargets();
        pruneExistingConnections();
        checkAllValidTargets();
    }

    public void checkAllValidTargets() {
        if ( !enabled || validTargets == null || validTargets.isEmpty() )
            return;

        for (BlockPosDimension target : validTargets)
            checkTarget(target);
    }

    public void pruneExistingConnections() {
        if ( node == null || connections == null || connections.isEmpty() )
            return;

        final IReadOnlyCollection<IGridConnection> cons = node.getConnections();
        for (Iterator<TargetInfo> it = connections.values().iterator(); it.hasNext(); ) {
            final TargetInfo target = it.next();
            final IGridNode targetNode = target.connection.getOtherSide(node);
            final boolean should = shouldConnect(target.pos, targetNode);
            final boolean contains = should && cons.contains(target.connection);

            if ( !should || !contains ) {
                destroyConnection(target.pos, false);
                it.remove();

                // Try to reestablish a connection.
                if ( enabled && validTargets != null && validTargets.contains(target.pos) )
                    checkTarget(target.pos);
            }
        }
    }

    public void handleWaitingTargets() {
        if ( !enabled || node == null || !node.isActive() || checkTargets == null || checkTargets.isEmpty() )
            return;

        for (Iterator<Map.Entry<BlockPosDimension, Byte>> it = checkTargets.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<BlockPosDimension, Byte> entry = it.next();
            final BlockPosDimension target = entry.getKey();
            final byte count = entry.getValue();

            final IGridNode targetNode = getNodeAt(target);
            if ( targetNode != null ) {
                updateConnection(target, targetNode);
                it.remove();
            } else if ( count > 2 )
                it.remove();
            else
                checkTargets.put(target, (byte) (count + 1));
        }
    }

    @Nonnull
    public Map<BlockPosDimension, TargetInfo> getConnections() {
        if ( connections == null )
            connections = new Object2ObjectOpenHashMap<>();

        return connections;
    }

    public void clearConnections() {
        if ( connections == null )
            return;

        for (TargetInfo target : connections.values())
            target.connection.destroy();

        connections.clear();
        setEnergyCost(0);
        usedConnections = 0;
    }

    void destroyConnection(@Nonnull BlockPosDimension targetPos) {
        destroyConnection(targetPos, true);
    }

    private void destroyConnection(@Nonnull BlockPosDimension targetPos, boolean remove) {
        if ( connections == null )
            return;

        final TargetInfo target = connections.get(targetPos);
        if ( target == null )
            return;

        // This is optional in case we're iterating.
        if ( remove )
            connections.remove(targetPos);

        target.connection.destroy();
        usedConnections--;
        setEnergyCost(energyCost - target.cost);
    }

    public void updateConnection(@Nonnull BlockPosDimension target, @Nonnull IGridNode targetNode) {
        final IGridNode node = getNode();
        if ( node == null || !node.isActive() )
            return;

        final boolean wanted = shouldConnect(target, targetNode);
        if ( !wanted && connections == null )
            return;

        final Map<BlockPosDimension, TargetInfo> connections = getConnections();

        if ( connections.containsKey(target) ) {
            final TargetInfo info = connections.get(target);
            final boolean same = info.connection.getOtherSide(node).equals(targetNode);
            final boolean valid = node.getConnections().contains(info.connection);

            if ( !wanted || !same || !valid )
                destroyConnection(target);

            if ( same && valid )
                return;
        }

        if ( !wanted )
            return;

        final IGridConnection con;

        try {
            con = AEApi.instance().grid().createGridConnection(node, targetNode);
        } catch (FailedConnectionException e) {
            // Now what?
            return;
        }

        final ItemStack source = sources.getOrDefault(target, ItemStack.EMPTY);
        final TargetInfo info = new TargetInfo(target, source, getEnergyCost(target, source), con);
        connections.put(target, info);
        usedConnections++;
        setEnergyCost(energyCost + info.cost);
    }

    /**
     * Check to see if we want to connect to a given node.
     *
     * @param targetPos  The absolute position of the node in the multiverse.
     * @param targetNode The node.
     * @return Whether or not we want to connect.
     */
    public boolean shouldConnect(@Nonnull BlockPosDimension targetPos, @Nonnull IGridNode targetNode) {
        if ( !enabled || !validTargets.contains(targetPos) )
            return false;

        final IGridBlock block = targetNode.getGridBlock();
        if ( block == null )
            return false;

        // Dense cables are broken and will not be fixed by AE2's developers
        // So we need to just detect them and purposefully not connect.
        // See: https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/4254
        final ItemStack stack = block.getMachineRepresentation();
        if ( stack.getItem() == AppliedEnergistics2Plugin.itemPart && targetNode.hasFlag(GridFlags.DENSE_CAPACITY) )
            return false;

        return getAEColor().matches(block.getGridColor());
    }


    /* NBT */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if ( node != null && tag.hasKey("ae_node") )
            node.loadFromNBT("ae_node", tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        if ( node != null )
            node.saveToNBT("ae_node", tag);

        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        if ( tag.hasKey("AEColor", Constants.NBT.TAG_BYTE) )
            aeColor = AEColorHelpers.fromByte(tag.getByte("AEColor"));
        else
            aeColor = AEColor.TRANSPARENT;
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);
        if ( aeColor != AEColor.TRANSPARENT )
            tag.setByte("AEColor", (byte) aeColor.ordinal());

        return tag;
    }


    public void updateChannels() {
        if ( FMLCommonHandler.instance().getEffectiveSide().isServer() && AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) ) {
            IGridNode node = getNode();

            usedChannels = 0;

            if ( node != null )
                for (IGridConnection n : node.getConnections()) {
                    if ( n != null )
                        usedChannels = Math.max(n.getUsedChannels(), usedChannels);
                }
        }
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addByte((byte) usedChannels);
        payload.addByte((byte) usedConnections);
        payload.addInt((int) getIdlePowerUsage());
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        usedChannels = payload.getByte();
        usedConnections = payload.getByte();
        powerDraw = payload.getInt();
    }

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(aeColor.ordinal());
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        setAEColor(payload.getByte());
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addByte(aeColor.ordinal());
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        setAEColor(payload.getByte());
    }


    /* ITileInfoProvider */

    public void getInfoTooltip(@Nonnull List<String> tooltip, @Nullable NBTTagCompound tag) {
        if ( tag != null && tag.hasKey("channels", Constants.NBT.TAG_BYTE) )
            usedChannels = tag.getByte("channels");

        else if ( world != null && !world.isRemote )
            updateChannels();

        if ( tag != null && tag.hasKey("connections", Constants.NBT.TAG_INT) )
            usedConnections = tag.getInteger("connections");

        int targets;
        if ( tag != null && tag.hasKey("targets", Constants.NBT.TAG_INT) )
            targets = tag.getInteger("targets");
        else
            targets = validTargets == null ? 0 : validTargets.size();

        tooltip.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".work_info.tooltip.targets",
                TextHelpers.getComponent(usedConnections),
                TextHelpers.getComponent(targets)
        ).getFormattedText());

        if ( AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) )
            tooltip.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".ae2.channels",
                    TextHelpers.getComponent(usedChannels),
                    TextHelpers.getComponent(getMaxChannels())
            ).getFormattedText());
    }

    @Nonnull
    @Override
    public NBTTagCompound getInfoNBT(@Nonnull NBTTagCompound tag, @Nullable EntityPlayerMP player) {
        updateChannels();

        tag.setByte("channels", (byte) usedChannels);
        tag.setInteger("connections", usedConnections);
        tag.setInteger("targets", validTargets == null ? 0 : validTargets.size());

        return tag;
    }

    public abstract boolean isDense();

    public int getMaxChannels() {
        return isDense() ? 32 : 8;
    }

    public int getUsedChannels() {
        return usedChannels;
    }

    public int getUsedConnections() {
        return usedConnections;
    }

    public int getMaxTargets() {
        if ( validTargets == null )
            return 0;

        return validTargets.size();
    }


    /* AE2 Node */

    /**
     * Get the IGridNode for this AE Network machine.
     *
     * @return IGridNode instance or null if running on the client.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public IGridNode getNode() {
        if ( world == null || world.isRemote )
            return null;

        if ( node == null ) {
            node = AEApi.instance().grid().createGridNode(this);
            node.setPlayerID(WorldData.instance().playerData().getPlayerID(owner));
            node.updateState();
        }

        return node;
    }

    /**
     * Destroy our grid node, if we have one.
     */
    private void unregisterNode() {
        if ( node != null ) {
            clearConnections();

            node.destroy();
            node = null;
        }
    }

    /**
     * Get the IGridNode for the block at the given position.
     *
     * @param pos The position to get a node for.
     * @return The IGridNode if one exists, or null if there is no node.
     */
    @Nullable
    private IGridNode getNodeAt(@Nonnull BlockPosDimension pos) {
        final World world;
        if ( this.world != null && this.world.provider.getDimension() == pos.getDimension() )
            world = this.world;
        else
            world = DimensionManager.getWorld(pos.getDimension());

        if ( world == null )
            return null;

        final TileEntity tile = world.getTileEntity(pos);
        if ( tile == null )
            return null;

        if ( tile instanceof IGridNode )
            return (IGridNode) tile;

        if ( tile instanceof IGridHost )
            return ((IGridHost) tile).getGridNode(AEPartLocation.INTERNAL);

        return null;
    }


    /* IGridHost */

    @Nullable
    public IGridNode getGridNode(@Nonnull AEPartLocation part) {
        return getNode();
    }

    @Nonnull
    public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        return isDense() ? AECableType.DENSE_COVERED : AECableType.COVERED;
    }

    public void securityBreak() {
        IBlockState state = world.getBlockState(pos);

        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, pos, state, 0);

        for (ItemStack stack : drops)
            CoreUtils.dropItemStackIntoWorldWithVelocity(stack, world, pos);

        dropContents();

        world.playEvent(null, 2001, pos, Block.getStateId(state));
        world.setBlockToAir(pos);
    }

    /* IGridBlock */

    public double getIdlePowerUsage() {
        return (enabled ? baseEnergy : 0) + energyCost;
    }

    @Nonnull
    public EnumSet<GridFlags> getFlags() {
        final boolean dense = isDense();
        final boolean channel = ModConfig.plugins.appliedEnergistics.requireChannels;

        if ( channel && dense )
            return EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
        else if ( channel )
            return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
        else if ( dense )
            return EnumSet.of(GridFlags.DENSE_CAPACITY);
        else
            return EnumSet.noneOf(GridFlags.class);
    }

    public boolean isWorldAccessible() {
        return true;
    }

    @Nonnull
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Nonnull
    public AEColor getGridColor() {
        if ( !ModConfig.plugins.appliedEnergistics.enableColor || ModConfig.plugins.appliedEnergistics.colorsWireless )
            return AEColor.TRANSPARENT;

        return getAEColor();
    }

    public void onGridNotification(@Nonnull GridNotification type) {
        if ( type == GridNotification.CONNECTIONS_CHANGED )
            pruneExistingConnections();
    }

    @Deprecated
    public void setNetworkStatus(IGrid grid, int status) {
        // Deprecated Interfaces without Default Implementations are Cool
    }

    @Nonnull
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Nonnull
    public IGridHost getMachine() {
        return this;
    }

    public void gridChanged() {
        // Not used
    }

    /* Target Info */

    private static class TargetInfo {
        public final BlockPosDimension pos;
        public final ItemStack source;
        public final int cost;
        public final IGridConnection connection;

        public TargetInfo(@Nonnull BlockPosDimension pos, @Nonnull ItemStack source, int cost, @Nonnull IGridConnection connection) {
            this.pos = pos;
            this.source = source;
            this.cost = cost;
            this.connection = connection;
        }

        public MoreObjects.ToStringHelper getStringBuilder() {
            return MoreObjects.toStringHelper(this)
                    .add("pos", pos)
                    .add("source", source)
                    .add("cost", cost)
                    .add("con", connection);
        }

        public String toString() {
            return getStringBuilder().toString();
        }
    }
}

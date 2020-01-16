package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base;

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
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.worlddata.WorldData;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AEColorHelpers;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TileAENetworkBase extends TileEntityBaseMachine implements
        IRangeAugmentable, ITickable,
        IGridHost, IGridBlock,
        EventDispatcher.IEventListener, ITileInfoProvider {

    public int IDLE_ENERGY_COST = 2;

    private Map<BlockPosDimension, IGridConnection> connections;

    private int activeUpdateDelay = 0;
    private Map<BlockPosDimension, Integer> placedCacheMap;

    public List<BlockPosDimension> validTargets;
    Set<BlockPosDimension> staleTargets;
    Set<BlockPosDimension> freshTargets;

    private int energyCost;

    private boolean needsRecalculation;
    private int recalculationDelay = 10;

    private AEColor aeColor = AEColor.TRANSPARENT;

    private IGridNode node;
    private int usedChannels = 0;
    private int usedConnections = 0;

    public TileAENetworkBase() {
        super();

        setWatchUnload();
    }

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("Targets: " + (validTargets == null ? "null" : validTargets.size()));
        System.out.println("Energy Cost: " + getEnergyCost());
    }

    /* Life Cycle */

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
            markDirty();

            if ( ModConfig.plugins.appliedEnergistics.enableColor )
                setNeedsRecalculation();
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


    /* The Update */

    @Override
    public void update() {
        super.update();

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
        if ( connections == null )
            connections = new Object2ObjectOpenHashMap<>();

        return connections;
    }

    public void clearConnections() {
        if ( connections == null )
            return;

        // TODO: Concurrent modification fix
        for (BlockPosDimension pos : connections.keySet())
            destroyConnection(pos);

        connections.clear();
        usedConnections = 0;
    }

    public void addConnection(BlockPosDimension pos, IGridNode otherNode) {
        final IGridNode node = getNode();
        if ( node == null )
            return;

        final Map<BlockPosDimension, IGridConnection> connections = getConnections();

        if ( connections.containsKey(pos) ) {
            connections.get(pos).destroy();
            connections.remove(pos);
            usedConnections--;
        }

        if ( !validTargets.contains(pos) )
            return;

        final IGridBlock block = otherNode.getGridBlock();
        if ( block == null )
            return;

        // Dense cables are broken and will not be fixed by AE2's developers
        // So we need to just detect them and purposefully not connect.
        final ItemStack stack = block.getMachineRepresentation();
        if ( stack.getItem() == AppliedEnergistics2Plugin.itemPart && otherNode.hasFlag(GridFlags.DENSE_CAPACITY) )
            return;

        if ( !getAEColor().matches(block.getGridColor()) )
            return;

        try {
            connections.put(pos, AEApi.instance().grid().createGridConnection(node, otherNode));
            usedConnections++;

            boolean sameDimension = pos.getDimension() == getWorld().provider.getDimension();
            double distance = getPos().getDistance(pos.getX(), pos.getY(), pos.getZ()) - 1;
            setEnergyCost(getEnergyCost() + calculateEnergyCost(distance, !sameDimension));
        } catch (FailedConnectionException e) {
            setNeedsRecalculation();
        }
    }

    public void destroyConnection(BlockPosDimension pos) {
        Map<BlockPosDimension, IGridConnection> connections = getConnections();

        if ( connections.containsKey(pos) ) {
            IGridConnection con = connections.get(pos);
            connections.remove(pos);
            usedConnections--;

            if ( con != null ) {
                IGridHost host = con.b().getMachine();
                if ( host instanceof TileAENetworkBase ) {
                    ((TileAENetworkBase) host).setNeedsRecalculation();
                }

                con.destroy();
            }

            boolean sameDimension = pos.getDimension() == getWorld().provider.getDimension();
            double distance = getPos().getDistance(pos.getX(), pos.getY(), pos.getZ()) - 1;
            setEnergyCost(getEnergyCost() - calculateEnergyCost(distance, !sameDimension));
        }
    }

    public void cachePlacedPosition(BlockPosDimension pos) {
        if ( validTargets.contains(pos) && !getPlacedCacheMap().containsKey(pos) )
            getPlacedCacheMap().put(pos, 0);
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

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if ( node != null && tag.hasKey("ae_node") )
            node.loadFromNBT("ae_node", tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);

        updateChannels();
        tag.setByte("channels", (byte) usedChannels);

        if ( node != null )
            node.saveToNBT("ae_node", tag);

        return tag;
    }

    public void updateChannels() {
        if ( FMLCommonHandler.instance().getEffectiveSide().isServer() && AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) ) {
            IGridNode node = getNode();

            if ( node == null )
                usedChannels = 0;
            else {
                for (IGridConnection n : node.getConnections()) {
                    if ( n != null )
                        usedChannels = Math.max(n.getUsedChannels(), usedChannels);
                }
            }
        }
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addByte((byte) usedChannels);
        payload.addByte((byte) usedConnections);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        usedChannels = payload.getByte();
        usedConnections = payload.getByte();
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

    public abstract void calculateTargets();


    /* ITileInfoProvider */

    public void getInfoTooltip(@Nonnull List<String> tooltip, @Nullable NBTTagCompound tag) {
        final boolean dense = ModConfig.plugins.appliedEnergistics.denseCableConnection;

        if ( tag != null && tag.hasKey("channels", Constants.NBT.TAG_BYTE) )
            usedChannels = tag.getByte("channels");

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
                    TextHelpers.getComponent(usedChannels).setStyle(TextHelpers.WHITE),
                    TextHelpers.getComponent(dense ? 32 : 8).setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
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

    public int getUsedChannels() {
        return usedChannels;
    }

    public int getUsedConnections() {
        return usedConnections;
    }

    /* Grid */

    private void unregisterNode() {
        if ( node != null ) {
            node.destroy();
            node = null;

            setNeedsRecalculation();
        }
    }

    @SuppressWarnings("deprecation")
    private void applyOwnerToNode() {
        if ( this.node != null ) {
            int playerID = WorldData.instance().playerData().getPlayerID(this.owner);
            this.node.setPlayerID(playerID);
        }
    }

    public IGridNode getNode() {
        if ( world == null || world.isRemote )
            return null;

        if ( node == null ) {
            node = AEApi.instance().grid().createGridNode(this);
            applyOwnerToNode();
            node.updateState();
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
        IBlockState state = world.getBlockState(pos);

        NonNullList<ItemStack> drops = NonNullList.create();
        getBlockType().getDrops(drops, world, pos, state, 0);

        Vec3d vPos = new Vec3d(pos);
        for (ItemStack stack : drops) {
            CoreUtils.dropItemStackIntoWorld(stack, world, vPos);
        }

        world.playEvent(null, 2001, pos, Block.getStateId(state));
        world.setBlockToAir(pos);
    }

    /* IGridBlock */

    @Override
    public double getIdlePowerUsage() {
        return getEnergyCost();
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        if ( ModConfig.plugins.appliedEnergistics.denseCableConnection )
            return EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);

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
        if ( !ModConfig.plugins.appliedEnergistics.enableColor || ModConfig.plugins.appliedEnergistics.colorsWireless )
            return AEColor.TRANSPARENT;

        return getAEColor();
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

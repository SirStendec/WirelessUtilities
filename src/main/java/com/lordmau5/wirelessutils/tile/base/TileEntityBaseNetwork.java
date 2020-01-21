package com.lordmau5.wirelessutils.tile.base;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.INetworkToolAgent;
import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.RSMachineNode;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.api.util.Action;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Objects;

@Optional.InterfaceList({
        @Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
        @Optional.Interface(iface = "appeng.api.networking.IGridBlock", modid = "appliedenergistics2"),
        @Optional.Interface(iface = "appeng.api.networking.security.IActionHost", modid = "appliedenergistics2"),
        @Optional.Interface(iface = "appeng.api.util.INetworkToolAgent", modid = "appliedenergistics2")
})
public abstract class TileEntityBaseNetwork extends TileEntityBaseEnergy implements
        IGridHost, IGridBlock, INetworkToolAgent, IActionHost // AE2
{

    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(INetworkNodeProxy.class)
    private static Capability<INetworkNodeProxy> NETWORK_NODE_PROXY_CAPABILITY = null;

    private boolean enableRS = false;
    private Object rsNode;

    private boolean enableAE = false;
    private Object aeNode;
    private NBTTagCompound aeNodeTag;

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyRSNode();
        destroyAENode();
    }

    public void setRSEnabled(boolean enabled) {
        if ( !Loader.isModLoaded("refinedstorage") )
            return;

        if ( enabled == enableRS )
            return;

        enableRS = enabled;
        if ( !enabled )
            destroyRSNode();

        if ( world != null && !world.isRemote )
            sendTilePacket(Side.CLIENT);
    }

    public void setAE2Enabled(boolean enabled) {
        if ( !Loader.isModLoaded("appliedenergistics2") )
            return;

        if ( enabled == enableAE )
            return;

        enableAE = enabled;
        if ( !enabled )
            destroyAENode();

        if ( world != null && !world.isRemote )
            sendTilePacket(Side.CLIENT);
    }

    public void updateNodes() {
        updateAENode();
        updateRSNode();
    }

    /* Packets are Fun for The Whole Family */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addBool(enableRS);
        payload.addBool(enableAE);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        enableRS = payload.getBool();
        enableAE = payload.getBool();
    }

    /* Loading and Saving */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if ( Loader.isModLoaded("appliedenergistics2") && tag.hasKey("node") && aeNode == null )
            aeNodeTag = tag.getCompoundTag("node");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound out = super.writeToNBT(tag);

        if ( hasAENode() ) {
            NBTTagCompound data = _saveAENode();
            if ( data != null )
                tag.setTag("node", data);
        }

        return out;
    }

    /* RS Node Management */

    public boolean hasRSNode() {
        return Loader.isModLoaded("refinedstorage") && rsNode != null;
    }

    public void updateRSNode() {
        if ( hasRSNode() )
            this._updateRSNode();
    }

    public void destroyRSNode() {
        if ( hasRSNode() )
            this._destroyRSNode();
    }

    @Optional.Method(modid = "refinedstorage")
    public void _updateRSNode() {
        if ( !hasRSNode() )
            return;

        RSMachineNode node = (RSMachineNode) rsNode;
        INetwork network = node.getNetwork();
        if ( network != null )
            network.getNodeGraph().invalidate(Action.PERFORM, network.world(), network.getPosition());
    }

    @Optional.Method(modid = "refinedstorage")
    public void _destroyRSNode() {
        if ( !hasRSNode() )
            return;

        RSMachineNode node = (RSMachineNode) rsNode;
        node.destroy();

        rsNode = null;
    }

    @Optional.Method(modid = "refinedstorage")
    public RSMachineNode getRSNode() {
        if ( !enableRS || world == null || world.isRemote || pos == null )
            return null;

        if ( hasRSNode() )
            return (RSMachineNode) rsNode;

        RSMachineNode node = new RSMachineNode(this);
        rsNode = node;
        node.discover();

        return node;
    }


    /* AE Node Management */

    public boolean hasAENode() {
        return Loader.isModLoaded("appliedenergistics2") && aeNode != null;
    }

    public void updateAENode() {
        if ( Loader.isModLoaded("appliedenergistics2") && aeNode != null )
            this._updateAENode();
    }

    public void destroyAENode() {
        if ( Loader.isModLoaded("appliedenergistics2") && aeNode != null )
            this._destroyAENode();
    }

    @Optional.Method(modid = "appliedenergistics2")
    private NBTTagCompound _saveAENode() {
        if ( aeNode instanceof IGridNode ) {
            IGridNode n = (IGridNode) aeNode;
            NBTTagCompound data = new NBTTagCompound();
            n.saveToNBT("n", data);
            return data;
        }

        return null;
    }

    @Optional.Method(modid = "appliedenergistics2")
    private void _updateAENode() {
        if ( aeNode instanceof IGridNode ) {
            IGridNode n = (IGridNode) aeNode;
            n.updateState();
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    private void _destroyAENode() {
        if ( aeNode instanceof IGridNode ) {
            IGridNode n = (IGridNode) aeNode;
            aeNode = null;
            n.destroy();
        }
    }

    /* INetworkToolAgent */

    @Optional.Method(modid = "appliedenergistics2")
    public boolean showNetworkInfo(RayTraceResult where) {
        return enableAE;
    }

    /* IGridHost */

    @Optional.Method(modid = "appliedenergistics2")
    @Nullable
    public IGridNode getGridNode(@Nonnull AEPartLocation dir) {
        if ( !enableAE )
            return null;

        IGridNode n = null;
        if ( aeNode instanceof IGridNode )
            n = (IGridNode) aeNode;
        else if ( aeNode == null && world != null && !world.isRemote ) {
            n = AEApi.instance().grid().createGridNode(this);
            n.setPlayerID(AEApi.instance().registries().players().getID(owner));
            if ( aeNodeTag != null ) {
                n.loadFromNBT("n", aeNodeTag);
                aeNodeTag = null;
            }

            n.updateState();
            aeNode = n;
        }

        return n;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Optional.Method(modid = "appliedenergistics2")
    public void securityBreak() {

    }

    /* IGridBlock */

    @Optional.Method(modid = "appliedenergistics2")
    public double getIdlePowerUsage() {
        return 0;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Optional.Method(modid = "appliedenergistics2")
    public boolean isWorldAccessible() {
        return enableAE;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Optional.Method(modid = "appliedenergistics2")
    public void onGridNotification(@Nonnull GridNotification notification) {
        // Nothing
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Deprecated
    public void setNetworkStatus(IGrid grid, int channelsInUse) {

    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public EnumSet<EnumFacing> getConnectableSides() {
        return getAE2ConnectableSides();
    }

    public EnumSet<EnumFacing> getAE2ConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public IGridHost getMachine() {
        return this;
    }

    @Optional.Method(modid = "appliedenergistics2")
    public void gridChanged() {

    }

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public ItemStack getMachineRepresentation() {
        if ( enableAE && world != null && pos != null ) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            return new ItemStack(block, 1, level.toInt());
        }

        return ItemStack.EMPTY;
    }

    /* IActionHost */

    @Optional.Method(modid = "appliedenergistics2")
    @Nonnull
    public IGridNode getActionableNode() {
        return Objects.requireNonNull(getGridNode(AEPartLocation.INTERNAL));
    }

    /* Capabilities */

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if ( enableRS && capability != null && capability == RefinedStoragePlugin.NETWORK_NODE_PROXY_CAPABILITY )
            return true;

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if ( enableRS && capability != null && capability == NETWORK_NODE_PROXY_CAPABILITY ) {
            RSMachineNode node = getRSNode();
            return node == null ? null : NETWORK_NODE_PROXY_CAPABILITY.cast(node);
        }

        return super.getCapability(capability, facing);
    }
}

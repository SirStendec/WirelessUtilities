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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
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
public abstract class TileEntityBaseAE2 extends TileEntityBaseEnergy implements IGridHost, IGridBlock, INetworkToolAgent, IActionHost {

    private boolean enableAE = false;
    private Object node;
    private NBTTagCompound nodeTag;

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyNode();
    }

    public void setAE2Enabled(boolean enabled) {
        if ( !Loader.isModLoaded("appliedenergistics2") )
            return;

        if ( enabled == enableAE )
            return;

        enableAE = enabled;
        if ( !enabled )
            destroyNode();

        if ( world != null && !world.isRemote )
            sendTilePacket(Side.CLIENT);
    }

    /* Packets are Fun for The Whole Family */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addBool(enableAE);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        enableAE = payload.getBool();
    }

    /* Loading and Saving */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if ( Loader.isModLoaded("appliedenergistics2") && tag.hasKey("node") && node == null )
            nodeTag = tag.getCompoundTag("node");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagCompound out = super.writeToNBT(tag);

        if ( hasNode() ) {
            NBTTagCompound data = saveNode();
            if ( data != null )
                tag.setTag("node", data);
        }

        return out;
    }

    public boolean hasNode() {
        return Loader.isModLoaded("appliedenergistics2") && node != null;
    }

    public void updateNode() {
        if ( Loader.isModLoaded("appliedenergistics2") && node != null )
            this._updateNode();
    }

    public void destroyNode() {
        if ( Loader.isModLoaded("appliedenergistics2") && node != null )
            this._destroyNode();
    }

    @Optional.Method(modid = "appliedenergistics2")
    private NBTTagCompound saveNode() {
        if ( node instanceof IGridNode ) {
            IGridNode n = (IGridNode) node;
            NBTTagCompound data = new NBTTagCompound();
            n.saveToNBT("n", data);
            return data;
        }

        return null;
    }

    @Optional.Method(modid = "appliedenergistics2")
    private void _updateNode() {
        if ( node instanceof IGridNode ) {
            IGridNode n = (IGridNode) node;
            n.updateState();
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    private void _destroyNode() {
        if ( node instanceof IGridNode ) {
            IGridNode n = (IGridNode) node;
            node = null;
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
        if ( node instanceof IGridNode )
            n = (IGridNode) node;
        else if ( node == null && world != null && !world.isRemote ) {
            n = AEApi.instance().grid().createGridNode(this);
            n.setPlayerID(AEApi.instance().registries().players().getID(owner));
            if ( nodeTag != null ) {
                n.loadFromNBT("n", nodeTag);
                nodeTag = null;
            }

            n.updateState();
            node = n;
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

}

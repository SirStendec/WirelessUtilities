package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base;

import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseNetwork;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeManager;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class RSMachineNode implements INetworkNode, INetworkNodeProxy {
    public final static String ID = "wirelessutils:machine_node";

    private final TileEntityBaseNetwork machine;
    private boolean discovered = false;
    private INetwork network;

    public RSMachineNode(TileEntityBaseNetwork machine) {
        Objects.requireNonNull(machine);
        this.machine = machine;

        INetworkNodeManager manager = getManager();
        Objects.requireNonNull(manager);
    }

    public void discover() {
        if ( getWorld() == null || getPos() == null || discovered )
            return;

        RefinedStoragePlugin.RSAPI.discoverNode(getWorld(), getPos());
        discovered = true;
    }

    public void destroy() {
        INetworkNodeManager manager = getManager();
        if ( manager != null ) {
            manager.removeNode(getPos());
            discovered = false;
        }
    }

    public TileEntityBaseNetwork getMachine() {
        return machine;
    }

    /* INetworkNodeProxy */

    @Nonnull
    public INetworkNode getNode() {
        return this;
    }

    /* INetworkNode */

    public int getEnergyUsage() {
        return 0;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return machine.getMachineRepresentation();
    }

    public void onConnected(INetwork network) {
        this.network = network;
    }

    public void onDisconnected(INetwork network) {
        this.network = null;
    }

    public boolean canUpdate() {
        return false;
    }

    @Nullable
    public INetwork getNetwork() {
        return network;
    }

    public void update() {

    }

    public NBTTagCompound write(NBTTagCompound tag) {
        return tag;
    }

    public BlockPos getPos() {
        return machine.getPos();
    }

    public World getWorld() {
        return machine.getWorld();
    }

    public void markDirty() {
        // Should we be marking the machine dirty and not the network? What even calls this?
        INetworkNodeManager manager = getManager();
        if ( manager != null )
            manager.markForSaving();
    }

    @Nullable
    public INetworkNodeManager getManager() {
        World world = getWorld();
        if ( world != null && !world.isRemote )
            return RefinedStoragePlugin.RSAPI.getNetworkNodeManager(world);

        return null;
    }

    public String getId() {
        return ID;
    }
}

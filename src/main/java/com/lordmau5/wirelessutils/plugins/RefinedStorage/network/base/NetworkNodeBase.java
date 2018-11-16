package com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base;

import com.lordmau5.wirelessutils.plugins.RefinedStorage.RefinedStoragePlugin;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.INetworkNodeVisitor;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class NetworkNodeBase implements INetworkNode, INetworkNodeVisitor {

    @Nullable
    protected INetwork network;
    protected World world;
    protected BlockPos pos;
    protected int ticks;

    private EnumFacing direction = EnumFacing.NORTH;

    private boolean throttlingDisabled;
    private boolean needsUpdate;
    private int ticksSinceUpdateChanged;

    public NetworkNodeBase(World world, BlockPos pos) {
        if ( world == null ) {
            throw new IllegalArgumentException("World cannot be null");
        }

        this.world = world;
        this.pos = pos;
    }

    public boolean canConduct(@Nullable EnumFacing direction) {
        return true;
    }

    @Override
    public void visit(Operator operator) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if ( canConduct(facing) ) {
                operator.apply(world, pos.offset(facing), facing.getOpposite());
            }
        }
    }

    @Override
    public int getEnergyUsage() {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        IBlockState state = world.getBlockState(pos);

        return new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().getMetaFromState(state));
    }

    @Override
    public void onConnected(INetwork network) {
        onConnectedStateChange(true);

        this.network = network;
    }

    @Override
    public void onDisconnected(INetwork network) {
        this.network = null;

        onConnectedStateChange(false);
    }

    protected void onConnectedStateChange(boolean state) {
        TileRSNetworkBase tile = (TileRSNetworkBase) world.getTileEntity(pos);
        if ( tile == null )
            return;

        tile.setActive(state && tile.redstoneControlOrDisable());
    }

    @Override
    public void markDirty() {
        if ( !world.isRemote ) {
            RefinedStoragePlugin.RSAPI.getNetworkNodeManager(world).markForSaving();
        }
    }

    @Override
    public boolean canUpdate() {
        if ( network != null ) {
            return network.canRun();
        }

        return false;
    }

    protected int getUpdateThrottleInactiveToActive() {
        return 60;
    }

    @Override
    public void update() {
        ++ticks;

        boolean canUpdate = getNetwork() != null && canUpdate();

        if ( needsUpdate && canUpdate ) {
            ++ticksSinceUpdateChanged;

            if ( ticksSinceUpdateChanged > getUpdateThrottleInactiveToActive() || throttlingDisabled ) {
                ticksSinceUpdateChanged = 0;
                needsUpdate = false;
                throttlingDisabled = false;

                if ( network != null ) {
                    onConnectedStateChange(true);

                    if ( shouldRebuildGraphOnChange() ) {
                        network.getNodeGraph().rebuild();
                    }
                }
            }
        } else {
            ticksSinceUpdateChanged = 0;
        }
    }

    public boolean shouldRebuildGraphOnChange() {
        return false;
    }

    public void setDirection(EnumFacing direction) {
        this.direction = direction;

        onDirectionChanged();

        markDirty();
    }

    protected void onDirectionChanged() {
        // NO OP
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        tag.setByte("Direction", (byte) direction.ordinal());

        return tag;
    }

    public void read(NBTTagCompound tag) {
        if ( tag.hasKey("Direction") ) {
            direction = EnumFacing.values()[tag.getByte("Direction")];
        }
    }

    @Nullable
    @Override
    public INetwork getNetwork() {
        return network;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public abstract String getId();
}

package com.lordmau5.wirelessutils.tile.base;

import cofh.core.block.TileRSControl;
import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public abstract class TileEntityBase extends TileRSControl implements EventDispatcher.IEventListener, IWorldNameable {
    private boolean watchUnload = false;
    protected boolean wasDismantled = false;

    /* Inventory */
    protected ItemStackHandler itemStackHandler;

    /* Comparator-ing */
    private int comparatorState = 0;

    /* Rendering */
    private final Map<String, String> MODEL_PROPERTIES = new HashMap<>();


    /* Comparator Logic */

    @Override
    public int getComparatorInputOverride() {
        return comparatorState;
    }

    public int calculateComparatorInput() {
        return 0;
    }

    public void runTrackers() {
        int comparatorState = calculateComparatorInput();
        if ( comparatorState != this.comparatorState ) {
            this.comparatorState = comparatorState;
            callNeighborTileChange();
        }
    }

    public void updateTrackers() {
        if ( timeCheck() )
            runTrackers();
    }


    /* Inventory */

    public void onContentsChanged(int slot) {
        markChunkDirty();
    }

    public int getStackLimit(int slot) {
        return 64;
    }

    public int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getStackLimit(slot), stack.getMaxStackSize());
    }

    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    public boolean shouldVoidItem(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    public void readInventoryFromNBT(NBTTagCompound tag) {
        if ( itemStackHandler != null && tag.hasKey("Inventory") )
            itemStackHandler.deserializeNBT(tag.getCompoundTag("Inventory"));
    }

    public void writeInventoryToNBT(NBTTagCompound tag) {
        if ( itemStackHandler == null )
            return;

        NBTTagCompound inventory = itemStackHandler.serializeNBT();
        if ( inventory.getInteger("Size") > 0 && !inventory.getTagList("Items", Constants.NBT.TAG_COMPOUND).isEmpty() )
            tag.setTag("Inventory", inventory);
    }

    protected void initializeItemStackHandler(int size) {
        itemStackHandler = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                TileEntityBase.this.onContentsChanged(slot);
                TileEntityBase.this.markChunkDirty();
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if ( !isItemValid(slot, stack) )
                    return stack;

                if ( shouldVoidItem(slot, stack) )
                    return ItemStack.EMPTY;

                return super.insertItem(slot, stack, simulate);
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return TileEntityBase.this.getStackLimit(slot, stack);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return isItemValidForSlot(slot, stack);
            }
        };
    }

    public ItemStackHandler getInventory() {
        return itemStackHandler;
    }

    @Override
    public int getInvSlotCount() {
        if ( itemStackHandler == null )
            return 0;

        return itemStackHandler.getSlots();
    }


    /* Tile Info */

    @Override
    protected Object getMod() {
        return WirelessUtils.instance;
    }

    @Override
    protected String getModVersion() {
        return WirelessUtils.VERSION;
    }


    /* Helper Methods for States */

    public void callNeighborStateChange(EnumFacing facing) {
        if ( world == null || pos == null )
            return;

        if ( ForgeEventFactory.onNeighborNotify(world, pos, world.getBlockState(pos), EnumSet.of(facing), false).isCanceled() )
            return;

        world.neighborChanged(pos.offset(facing), getBlockType(), pos);
    }

    @Override
    public void callNeighborStateChange() {
        if ( world == null || pos == null )
            return;

        super.callNeighborStateChange();
    }

    @Override
    public void callNeighborTileChange() {
        if ( world == null || pos == null )
            return;

        super.callNeighborTileChange();
    }


    /* Life Cycle */

    @Override
    public void validate() {
        super.validate();
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.addListener(world.provider.getDimension(), 0, 0, this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        onDestroy();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if ( !oldState.getBlock().equals(newState.getBlock()) )
            return super.shouldRefresh(world, pos, oldState, newState);

        onRefresh(world, pos, oldState, newState);
        return false;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        onDestroy();
    }

    @Override
    protected void finalize() throws Throwable {
        onDestroy();
        super.finalize();
    }

    @Override
    public void handleEvent(@Nonnull Event event) {
        if ( event instanceof WorldEvent.Unload ) {
            onDestroy();
        }
    }


    public void dropContents() {
        if ( itemStackHandler != null ) {
            int length = itemStackHandler.getSlots();
            for (int i = 0; i < length; i++)
                CoreUtils.dropItemStackIntoWorldWithVelocity(itemStackHandler.getStackInSlot(i), world, pos);
        }
    }

    @Override
    public void blockBroken() {
        if ( world != null && pos != null && !wasDismantled )
            dropContents();

        super.blockBroken();
    }

    @Override
    public void blockDismantled() {
        wasDismantled = true;
        super.blockDismantled();
    }


    /* Life Cycle Events */

    public void onDestroy() {
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.removeListener(world.provider.getDimension(), 0, 0, this);
    }

    protected void onRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {

    }

    /* Watch for World Unload */
    public void setWatchUnload() {
        setWatchUnload(true);
    }

    public void setWatchUnload(boolean watch) {
        if ( watch == watchUnload )
            return;

        watchUnload = watch;
        if ( !isInvalid() && world != null && world.provider != null ) {
            int dimension = world.provider.getDimension();
            if ( watchUnload )
                EventDispatcher.WORLD_UNLOAD.addListener(dimension, 0, 0, this);
            else
                EventDispatcher.WORLD_UNLOAD.removeListener(dimension, 0, 0, this);
        }

    }

    @Override
    public void setWorld(World worldIn) {
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.removeListener(world.provider.getDimension(), 0, 0, this);
        super.setWorld(worldIn);
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.addListener(world.provider.getDimension(), 0, 0, this);
    }


    /* IWorldNameable */

    @Override
    public String getName() {
        return customName.isEmpty() ? getTileName() : customName;
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }


    /* Client <-> Server Sync Methods */

    // TODO: Reevaluate these three methods.
    public void notifyBlockUpdate() {
        final IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        notifyBlockUpdate();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        notifyBlockUpdate();
    }


    /* NBT Save and Load */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readExtraFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeExtraToNBT(tag);
        return tag;
    }

    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        writeInventoryToNBT(tag);
        return tag;
    }

    public void readExtraFromNBT(NBTTagCompound tag) {
        readInventoryFromNBT(tag);
    }

    public void setProperty(String key, String value) {
        synchronized (MODEL_PROPERTIES) {
            MODEL_PROPERTIES.put(key, value);
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if ( state instanceof IExtendedBlockState ) {
            synchronized (MODEL_PROPERTIES) {
                state = ((IExtendedBlockState) state).withProperty(Properties.MODEL_PROPERTIES, new HashMap<>(MODEL_PROPERTIES));
            }
        }
        return state;
    }
}

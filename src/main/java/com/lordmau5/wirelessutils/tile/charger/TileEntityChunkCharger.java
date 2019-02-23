package com.lordmau5.wirelessutils.tile.charger;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.gui.client.charger.GuiChunkCharger;
import com.lordmau5.wirelessutils.gui.container.charger.ContainerChunkCharger;
import com.lordmau5.wirelessutils.tile.base.IFacing;
import com.lordmau5.wirelessutils.tile.base.ITargetProvider;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;

@Machine(name = "chunk_charger")
public class TileEntityChunkCharger extends TileEntityBaseCharger implements
        IFacing {

    private EnumFacing facing = EnumFacing.NORTH;
    private boolean calculated = false;

    @Override
    public void calculateTargets() {
        World world = getWorld();
        BlockPos pos = getPos();
        if ( world == null || pos == null )
            return;

        calculated = true;
        clearRenderAreas();

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        ChunkPos chunkPos = world.getChunk(chunkX, chunkZ).getPos();
        int dimension = world.provider.getDimension();

        addRenderArea(
                new BlockPosDimension(chunkPos.getXStart(), 0, chunkPos.getZStart(), dimension),
                new BlockPosDimension(chunkPos.getXEnd(), 255, chunkPos.getZEnd(), dimension)
        );
    }

    /* IFacing */

    @Override
    public boolean canSideTransfer(TransferSide side) {
        return true;
    }

    @Override
    public boolean onWrench(EntityPlayer player, EnumFacing side) {
        return rotateBlock(side);
    }

    @Override
    public EnumFacing getEnumFacing() {
        return facing;
    }

    @Override
    public boolean getRotationX() {
        return false;
    }

    @Override
    public boolean setRotationX(boolean rotationX) {
        return false;
    }

    @Override
    public boolean allowYAxisFacing() {
        return false;
    }

    @Override
    public boolean setFacing(EnumFacing facing) {
        if ( facing == this.facing )
            return true;

        if ( facing == EnumFacing.UP || facing == EnumFacing.DOWN )
            return false;

        this.facing = facing;
        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    @Override
    public int getEnergyCost(double distance, boolean isInterdimensional) {
        return 0;
    }

    @Override
    public Iterable<Tuple<BlockPosDimension, ItemStack>> getTargets() {
        validTargetsPerTick = 0;

        World world = getWorld();
        BlockPos pos = getPos();
        if ( world == null || world.provider == null || pos == null )
            return null;

        if ( !calculated )
            calculateTargets();

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if ( chunk == null || !chunk.isLoaded() )
            return null;

        Map<BlockPos, TileEntity> entities = chunk.getTileEntityMap();
        if ( entities == null )
            return null;

        List<Tuple<BlockPosDimension, ItemStack>> output = new ObjectArrayList<>();
        int dimension = world.provider.getDimension();

        for (Map.Entry<BlockPos, TileEntity> entry : entities.entrySet()) {
            TileEntity tile = entry.getValue();
            if ( tile == this )
                continue;

            if ( tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, null) )
                output.add(new Tuple<>(new BlockPosDimension(entry.getKey(), dimension), ItemStack.EMPTY));
        }

        ITargetProvider.sortTargetList(getPosition(), output);
        return output;
    }

    @Override
    public Iterable<Tuple<Entity, ItemStack>> getEntityTargets() {
        return null;
    }

    /* NBT */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        facing = EnumFacing.byIndex(tag.getByte("Facing"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setByte("Facing", (byte) facing.ordinal());
        return tag;
    }

    /* Packets */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(getFacing());
        return payload;
    }

    @Override
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        setFacing(payload.getByte(), false);
    }

    /* GUI */

    @Override
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiChunkCharger(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerChunkCharger(inventory, this);
    }
}

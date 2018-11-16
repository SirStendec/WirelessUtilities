package com.lordmau5.wirelessutils.tile.charger;

import com.lordmau5.wirelessutils.gui.client.charger.GuiChunkCharger;
import com.lordmau5.wirelessutils.gui.container.charger.ContainerChunkCharger;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

@Machine(name = "chunk_charger")
public class TileEntityChunkCharger extends TileEntityBaseCharger {

    @Override
    public void calculateTargets() {
        World world = getWorld();
        if ( world == null )
            return;

        clearRenderAreas();

        BlockPos pos = getPos();
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        ChunkPos chunkPos = world.getChunk(chunkX, chunkZ).getPos();

        addRenderArea(new BlockArea(
                world.provider.getDimension(),
                chunkPos.getXStart(),
                0,
                chunkPos.getZStart(),
                chunkPos.getXEnd(),
                255,
                chunkPos.getZEnd(),
                null,
                NiceColors.HANDY_COLORS[0],
                true
        ));
    }

    @Override
    public int getEnergyCost(double distance, boolean isInterdimensional) {
        return 0;
    }

    @Override
    public Iterable<BlockPosDimension> getTargets() {
        World world = getWorld();
        BlockPos pos = getPos();
        if ( world == null || world.provider == null || pos == null )
            return null;

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if ( chunk == null || !chunk.isLoaded() )
            return null;

        Map<BlockPos, TileEntity> entities = chunk.getTileEntityMap();

        return () -> BlockPosDimension.iterateWithDimension(world.provider.getDimension(), entities.keySet());
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

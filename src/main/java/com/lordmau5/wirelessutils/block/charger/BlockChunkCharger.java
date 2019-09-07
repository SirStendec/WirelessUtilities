package com.lordmau5.wirelessutils.block.charger;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityChunkCharger;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockChunkCharger extends BlockBaseMachine {
    public BlockChunkCharger() {
        super();

        setName("chunk_charger");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityChunkCharger.class;
    }
}

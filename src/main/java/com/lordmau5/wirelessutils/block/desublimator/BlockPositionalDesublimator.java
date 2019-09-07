package com.lordmau5.wirelessutils.block.desublimator;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockPositionalDesublimator extends BlockBaseMachine {
    public BlockPositionalDesublimator() {
        super();

        setName("positional_desublimator");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TilePositionalDesublimator.class;
    }
}

package com.lordmau5.wirelessutils.block.desublimator;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.desublimator.TileDirectionalDesublimator;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockDirectionalDesublimator extends BlockBaseMachine {

    public BlockDirectionalDesublimator() {
        super();

        setName("directional_desublimator");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileDirectionalDesublimator.class;
    }
}

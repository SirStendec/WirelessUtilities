package com.lordmau5.wirelessutils.block.condenser;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityPositionalCondenser;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockPositionalCondenser extends BlockBaseMachine {
    public BlockPositionalCondenser() {
        super();

        setName("positional_condenser");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPositionalCondenser.class;
    }
}

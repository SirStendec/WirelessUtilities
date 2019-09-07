package com.lordmau5.wirelessutils.block.condenser;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityDirectionalCondenser;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockDirectionalCondenser extends BlockBaseMachine {

    public BlockDirectionalCondenser() {
        super();

        setName("directional_condenser");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDirectionalCondenser.class;
    }
}

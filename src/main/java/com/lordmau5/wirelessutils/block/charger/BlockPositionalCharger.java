package com.lordmau5.wirelessutils.block.charger;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityPositionalCharger;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockPositionalCharger extends BlockBaseMachine {

    public BlockPositionalCharger() {
        super();

        setName("positional_charger");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPositionalCharger.class;
    }
}

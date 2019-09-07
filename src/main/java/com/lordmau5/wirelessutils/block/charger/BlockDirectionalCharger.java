package com.lordmau5.wirelessutils.block.charger;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockDirectionalCharger extends BlockBaseMachine {
    public BlockDirectionalCharger() {
        super();

        setName("directional_charger");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDirectionalCharger.class;
    }
}

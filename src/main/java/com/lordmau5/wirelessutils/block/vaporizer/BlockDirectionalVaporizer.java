package com.lordmau5.wirelessutils.block.vaporizer;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.vaporizer.TileDirectionalVaporizer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class BlockDirectionalVaporizer extends BlockBaseMachine {

    public BlockDirectionalVaporizer() {
        super();

        setName("directional_vaporizer");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileDirectionalVaporizer.class;
    }
}

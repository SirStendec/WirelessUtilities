package com.lordmau5.wirelessutils.block.charger;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDirectionalCharger extends BlockBaseMachine {
    public BlockDirectionalCharger() {
        super();

        setName("directional_charger");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityDirectionalCharger();
    }
}

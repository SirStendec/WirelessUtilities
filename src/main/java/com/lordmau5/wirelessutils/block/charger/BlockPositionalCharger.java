package com.lordmau5.wirelessutils.block.charger;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityPositionalCharger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPositionalCharger extends BlockBaseMachine {

    public BlockPositionalCharger() {
        super();

        setName("positional_charger");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPositionalCharger();
    }
}

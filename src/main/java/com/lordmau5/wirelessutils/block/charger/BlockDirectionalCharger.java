package com.lordmau5.wirelessutils.block.charger;

import com.lordmau5.wirelessutils.block.base.BlockBaseDirectionalMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDirectionalCharger extends BlockBaseDirectionalMachine {
    public BlockDirectionalCharger() {
        super();

        setName("directional_charger");
    }

    /*@Override
    public boolean hasSidedTransfer() {
        return true;
    }*/

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityDirectionalCharger();
    }
}

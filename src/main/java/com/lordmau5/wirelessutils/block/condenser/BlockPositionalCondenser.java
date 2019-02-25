package com.lordmau5.wirelessutils.block.condenser;

import com.lordmau5.wirelessutils.block.base.BlockBaseDirectionalMachine;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityPositionalCondenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPositionalCondenser extends BlockBaseDirectionalMachine {
    public BlockPositionalCondenser() {
        super();

        setName("positional_condenser");
    }

    /*@Override
    public boolean hasSidedTransfer() {
        return true;
    }*/

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPositionalCondenser();
    }
}

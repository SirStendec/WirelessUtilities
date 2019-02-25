package com.lordmau5.wirelessutils.block.condenser;

import com.lordmau5.wirelessutils.block.base.BlockBaseDirectionalMachine;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityDirectionalCondenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDirectionalCondenser extends BlockBaseDirectionalMachine {

    public BlockDirectionalCondenser() {
        super();

        setName("directional_condenser");
    }

    /*@Override
    public boolean hasSidedTransfer() {
        return true;
    }*/

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityDirectionalCondenser();
    }


}

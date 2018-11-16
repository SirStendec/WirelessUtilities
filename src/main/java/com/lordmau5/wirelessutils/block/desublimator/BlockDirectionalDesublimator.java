package com.lordmau5.wirelessutils.block.desublimator;

import com.lordmau5.wirelessutils.block.base.BlockBaseDirectionalMachine;
import com.lordmau5.wirelessutils.tile.desublimator.TileDirectionalDesublimator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDirectionalDesublimator extends BlockBaseDirectionalMachine {

    public BlockDirectionalDesublimator() {
        super();

        setName("directional_desublimator");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileDirectionalDesublimator();
    }
}

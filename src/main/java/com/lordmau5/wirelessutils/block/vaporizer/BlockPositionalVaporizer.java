package com.lordmau5.wirelessutils.block.vaporizer;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.vaporizer.TilePositionalVaporizer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPositionalVaporizer extends BlockBaseMachine {

    public BlockPositionalVaporizer() {
        super();
        setName("positional_vaporizer");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePositionalVaporizer();
    }
}

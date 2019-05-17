package com.lordmau5.wirelessutils.block.vaporizer;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.vaporizer.TileDirectionalVaporizer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDirectionalVaporizer extends BlockBaseMachine {

    public BlockDirectionalVaporizer() {
        super();

        setName("directional_vaporizer");
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileDirectionalVaporizer();
    }
}

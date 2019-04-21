package com.lordmau5.repack.codechicken.lib.bakedmodel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by covers1624 on 19/01/19.
 */
public interface ILayeredBakedModel extends IBakedModel {

    @Override
    default List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if ( layer == null ) {
            layer = getFallbackLayer();
        }
        return layer != null ? getQuads(state, side, layer, rand) : Collections.emptyList();
    }

    BlockRenderLayer getFallbackLayer();

    List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, BlockRenderLayer layer, long rand);

}

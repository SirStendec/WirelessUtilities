package com.lordmau5.repack.codechicken.lib.bakedmodel;

import com.google.common.collect.ImmutableMap;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IModelProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by covers1624 on 19/01/19.
 */
public class LayeredWrappedModel extends BakedPropertiesModel implements ILayeredBakedModel {

    private final BlockRenderLayer fallbackLayer;
    private final Map<BlockRenderLayer, IBakedModel> layerModels;

    public LayeredWrappedModel(IModelProperties properties, Map<BlockRenderLayer, IBakedModel> layerModels) {
        this(properties, BlockRenderLayer.SOLID, layerModels);
    }

    public LayeredWrappedModel(IModelProperties properties, BlockRenderLayer fallbackLayer, Map<BlockRenderLayer, IBakedModel> layerModels) {
        super(properties);
        this.fallbackLayer = fallbackLayer;
        this.layerModels = ImmutableMap.copyOf(layerModels);
    }

    @Override
    public BlockRenderLayer getFallbackLayer() {
        return fallbackLayer;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, BlockRenderLayer layer, long rand) {
        IBakedModel model = layerModels.get(layer);
        return model != null ? model.getQuads(state, side, rand) : Collections.emptyList();
    }
}

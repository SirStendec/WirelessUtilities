package com.lordmau5.repack.codechicken.lib.bakedmodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IModelProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by covers1624 on 19/01/19.
 */
public class SimpleLayeredModel extends BakedPropertiesModel implements ILayeredBakedModel {

    private final BlockRenderLayer fallbackLayer;
    private final Map<BlockRenderLayer, Map<EnumFacing, List<BakedQuad>>> layerFaceQuads;
    private final Map<BlockRenderLayer, List<BakedQuad>> layerGeneralQuads;

    public SimpleLayeredModel(IModelProperties properties, Map<BlockRenderLayer, Map<EnumFacing, List<BakedQuad>>> layerFaceQuads, Map<BlockRenderLayer, List<BakedQuad>> layerGeneralQuads) {
        this(properties, BlockRenderLayer.SOLID, layerFaceQuads, layerGeneralQuads);
    }

    public SimpleLayeredModel(IModelProperties properties, BlockRenderLayer fallbackLayer, Map<BlockRenderLayer, Map<EnumFacing, List<BakedQuad>>> layerFaceQuads, Map<BlockRenderLayer, List<BakedQuad>> layerGeneralQuads) {
        super(properties);
        this.fallbackLayer = fallbackLayer;
        //Deep copy the maps.
        ImmutableMap.Builder<BlockRenderLayer, Map<EnumFacing, List<BakedQuad>>> newLayerFaceQuads = ImmutableMap.builder();
        ImmutableMap.Builder<BlockRenderLayer, List<BakedQuad>> newLayerGeneralQuads = ImmutableMap.builder();
        if ( !layerFaceQuads.isEmpty() ) {
            for (Map.Entry<BlockRenderLayer, Map<EnumFacing, List<BakedQuad>>> entry : layerFaceQuads.entrySet()) {
                BlockRenderLayer layer = entry.getKey();
                ImmutableMap.Builder<EnumFacing, List<BakedQuad>> faceQuads = ImmutableMap.builder();
                for (Map.Entry<EnumFacing, List<BakedQuad>> entry2 : entry.getValue().entrySet()) {
                    faceQuads.put(entry2.getKey(), ImmutableList.copyOf(entry2.getValue()));
                }
                newLayerFaceQuads.put(layer, faceQuads.build());
            }
        }

        if ( !layerGeneralQuads.isEmpty() ) {
            for (Map.Entry<BlockRenderLayer, List<BakedQuad>> entry : layerGeneralQuads.entrySet()) {
                newLayerGeneralQuads.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
            }
        }

        this.layerFaceQuads = newLayerFaceQuads.build();
        this.layerGeneralQuads = newLayerGeneralQuads.build();

    }

    @Override
    public BlockRenderLayer getFallbackLayer() {
        return fallbackLayer;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, BlockRenderLayer layer, long rand) {
        List<BakedQuad> quads = null;
        if ( side == null ) {
            quads = layerGeneralQuads.get(layer);
        } else {
            Map<EnumFacing, List<BakedQuad>> faceQuads = layerFaceQuads.get(layer);
            if ( faceQuads != null ) {
                quads = faceQuads.get(side);
            }
        }
        return quads != null ? quads : Collections.emptyList();
    }
}

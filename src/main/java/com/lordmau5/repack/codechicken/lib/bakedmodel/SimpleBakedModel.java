package com.lordmau5.repack.codechicken.lib.bakedmodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IModelProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by covers1624 on 19/01/19.
 */
public class SimpleBakedModel extends BakedPropertiesModel {

    private final Map<EnumFacing, List<BakedQuad>> faceQuads;
    private final List<BakedQuad> generalQuads;

    public SimpleBakedModel(IModelProperties properties, Map<EnumFacing, List<BakedQuad>> faceQuads) {
        this(properties, faceQuads, Collections.emptyList());
    }

    public SimpleBakedModel(IModelProperties properties, List<BakedQuad> generalQuads) {
        this(properties, Collections.emptyMap(), generalQuads);
    }

    public SimpleBakedModel(IModelProperties properties, Map<EnumFacing, List<BakedQuad>> faceQuads, List<BakedQuad> generalQuads) {
        super(properties);
        ImmutableMap.Builder<EnumFacing, List<BakedQuad>> newFaceQuads = ImmutableMap.builder();
        if ( !faceQuads.isEmpty() ) {
            for (Map.Entry<EnumFacing, List<BakedQuad>> entry : faceQuads.entrySet()) {
                newFaceQuads.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
            }
        }
        this.faceQuads = newFaceQuads.build();
        this.generalQuads = ImmutableList.copyOf(generalQuads);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads;
        if ( side == null ) {
            quads = generalQuads;
        } else {
            quads = faceQuads.get(side);
        }
        return quads != null ? quads : Collections.emptyList();
    }
}

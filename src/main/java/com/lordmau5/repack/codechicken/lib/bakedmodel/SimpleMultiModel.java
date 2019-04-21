package com.lordmau5.repack.codechicken.lib.bakedmodel;

import com.google.common.collect.ImmutableList;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IModelProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by covers1624 on 19/01/19.
 */
public class SimpleMultiModel extends BakedPropertiesModel {

    private final List<IBakedModel> models;

    public SimpleMultiModel(IModelProperties properties, IBakedModel parent, List<IBakedModel> models) {
        this(properties, ImmutableList.<IBakedModel>builder().add(parent).addAll(models).build());
    }

    public SimpleMultiModel(IModelProperties properties, List<IBakedModel> models) {
        super(properties);
        this.models = ImmutableList.copyOf(models);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = null;
        for (IBakedModel model : models) {
            List<BakedQuad> tmp = model.getQuads(state, side, rand);
            if ( !tmp.isEmpty() ) {
                if ( quads == null ) {
                    quads = new ArrayList<>();
                }
                quads.addAll(tmp);
            }

        }
        return quads != null ? quads : Collections.emptyList();
    }
}

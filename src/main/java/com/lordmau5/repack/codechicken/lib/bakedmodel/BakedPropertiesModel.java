package com.lordmau5.repack.codechicken.lib.bakedmodel;

import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IModelProperties;
import com.lordmau5.repack.codechicken.lib.bakedmodel.properties.IPerspectiveProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 19/01/19.
 */
public abstract class BakedPropertiesModel implements IBakedModel, IParticleProviderModel {

    private final ThreadLocal<Quad> unpackerCache = ThreadLocal.withInitial(Quad::new);

    protected final IModelProperties properties;

    public BakedPropertiesModel(IModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return properties.isAO();
    }

    @Override
    public boolean isGui3d() {
        return properties.isGui3D();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return properties.isBuiltinRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return properties.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType transformType) {
        if ( properties instanceof IPerspectiveProperties ) {
            return PerspectiveMapWrapper.handlePerspective(this, ((IPerspectiveProperties) properties).getState(), transformType);
        }
        return ForgeHooksClient.handlePerspective(this, transformType);
    }

//    @Override
//    public Iterable<TextureAtlasSprite> getHitEffects(Vector3 hitVec, EnumFacing hitFace, IBlockAccess world, BlockPos pos, IBlockState state) {
//        Vector3 vec = hitVec.copy().subtract(pos);
//        return getAllQuads(state).parallelStream()//
//                .filter(e -> e.getFace() == hitFace)//
//                .filter(e -> depthCheck(e, vec, hitFace))//
//                .map(BakedQuad::getSprite)//
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Iterable<TextureAtlasSprite> getDestroyEffects(IBlockAccess world, BlockPos pos, IBlockState state) {
//        return getAllQuads(state).parallelStream().map(BakedQuad::getSprite).collect(Collectors.toList());
//    }
//
//    protected boolean depthCheck(BakedQuad quad, Vector3 hitVec, EnumFacing hitFace) {
//        CachedFormat format = CachedFormat.lookup(quad.getFormat());
//        Quad unpacker = unpackerCache.get();
//        unpacker.reset(format);
//        quad.pipe(unpacker);
//        Vector3 posVec = new Vector3();
//        for (Quad.Vertex v : unpacker.vertices) {
//            posVec.add(v.vec[0], v.vec[1], v.vec[2]);
//        }
//        posVec.divide(4);
//
//        double diff = 0;
//        switch (hitFace.getAxis()) {
//            case X:
//                diff = Math.abs(hitVec.x - posVec.x);
//                break;
//            case Y:
//                diff = Math.abs(hitVec.y - posVec.y);
//                break;
//            case Z:
//                diff = Math.abs(hitVec.z - posVec.z);
//                break;
//        }
//        return !(diff > 0.01);
//    }

    protected List<BakedQuad> getAllQuads(IBlockState state) {
        List<BakedQuad> quads = new ArrayList<>();
        if ( this instanceof ILayeredBakedModel ) {
            BlockRenderLayer currentLayer = MinecraftForgeClient.getRenderLayer();
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                ForgeHooksClient.setRenderLayer(layer);
                quads.addAll(getQuads(state, null, 0L));
                for (EnumFacing face : EnumFacing.values()) {
                    quads.addAll(getQuads(state, face, 0L));
                }
            }
            ForgeHooksClient.setRenderLayer(currentLayer);
        } else {
            quads.addAll(getQuads(state, null, 0L));
            for (EnumFacing face : EnumFacing.VALUES) {
                quads.addAll(getQuads(state, face, 0L));
            }
        }
        return quads;
    }
}

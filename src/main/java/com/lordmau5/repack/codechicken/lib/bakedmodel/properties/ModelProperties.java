package com.lordmau5.repack.codechicken.lib.bakedmodel.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.IModelState;

/**
 * Created by covers1624 on 19/01/19.
 */
public class ModelProperties implements IModelProperties {

    public static final IModelProperties DEFAULT_BLOCK = new ModelProperties(true, true);
    public static final IModelProperties DEFAULT_ITEM = new ModelProperties(true, false);

    private final boolean isAO;
    private final boolean isGui3D;
    private final boolean isBuiltinRenderer;
    private final ResourceLocation particleLocation;
    private TextureAtlasSprite particleTexture;

    public ModelProperties(boolean isAO) {
        this(isAO, false, false, null);
    }

    public ModelProperties(boolean isAO, boolean isGui3D) {
        this(isAO, isGui3D, false, null);
    }

    public ModelProperties(boolean isAO, boolean isGui3D, boolean isBuiltinRenderer, ResourceLocation particleLocation) {
        this.isAO = isAO;
        this.isGui3D = isGui3D;
        this.isBuiltinRenderer = isBuiltinRenderer;
        this.particleLocation = particleLocation;
    }

    @Override
    public boolean isAO() {
        return isAO;
    }

    @Override
    public boolean isGui3D() {
        return isGui3D;
    }

    @Override
    public boolean isBuiltinRenderer() {
        return isBuiltinRenderer;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        if ( particleTexture == null && particleLocation != null ) {
            particleTexture = ModelLoader.defaultTextureGetter().apply(particleLocation);
        }
        if ( particleTexture == null ) {
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }
        return particleTexture;
    }

    @Override
    public ResourceLocation getParticleLocation() {
        return particleLocation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(IModelProperties other) {
        if ( other instanceof IPerspectiveProperties ) {
            return PerspectiveProperties.builder(other);
        }
        return new Builder(other.isAO(), other.isGui3D(), other.isBuiltinRenderer(), other.getParticleLocation());
    }

    public static class Builder {

        private boolean isAO;
        private boolean isGui3D;
        private boolean isBuiltinRenderer;
        private ResourceLocation particleTexture;

        protected Builder() {
        }

        protected Builder(Builder other) {
            this(other.isAO, other.isGui3D, other.isBuiltinRenderer, other.particleTexture);
        }

        protected Builder(boolean isAO, boolean isGui3D, boolean isBuiltinRenderer, ResourceLocation particleTexture) {
            this.isAO = isAO;
            this.isGui3D = isGui3D;
            this.isBuiltinRenderer = isBuiltinRenderer;
            this.particleTexture = particleTexture;
        }

        public Builder copyFrom(IBakedModel bakedModel) {
            TextureAtlasSprite particle = bakedModel.getParticleTexture();
            return withAO(bakedModel.isAmbientOcclusion())//
                    .withGui3D(bakedModel.isGui3d())//
                    .withBuiltinRenderer(bakedModel.isBuiltInRenderer())//
                    .withParticleTexture(particle != null ? new ResourceLocation(particle.getIconName()) : null);
        }

        public Builder withAO(boolean value) {
            isAO = value;
            return this;
        }

        public Builder withGui3D(boolean value) {
            isGui3D = value;
            return this;
        }

        public Builder withBuiltinRenderer(boolean value) {
            isBuiltinRenderer = value;
            return this;
        }

        public Builder withParticleTexture(ResourceLocation value) {
            particleTexture = value;
            return this;
        }

        public PerspectiveProperties.Builder withState(IModelState value) {
            PerspectiveProperties.Builder builder = new PerspectiveProperties.Builder(this);
            builder.withState(value);
            return builder;
        }

        public IModelProperties build() {
            return new ModelProperties(isAO, isGui3D, isBuiltinRenderer, particleTexture);
        }

    }
}

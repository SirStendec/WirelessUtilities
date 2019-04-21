package com.lordmau5.repack.codechicken.lib.bakedmodel.properties;

import com.lordmau5.repack.codechicken.lib.bakedmodel.TransformUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;

/**
 * Created by covers1624 on 19/01/19.
 */
public class PerspectiveProperties extends ModelProperties implements IPerspectiveProperties {

    public static final IPerspectiveProperties DEFAULT_BLOCK = new PerspectiveProperties(ModelProperties.DEFAULT_BLOCK, TransformUtils.DEFAULT_BLOCK);
    public static final IPerspectiveProperties DEFAULT_ITEM = new PerspectiveProperties(ModelProperties.DEFAULT_ITEM, TransformUtils.DEFAULT_ITEM);

    private final IModelState state;

    public PerspectiveProperties(IModelProperties other, IModelState state) {
        this(other.isAO(), other.isGui3D(), other.isBuiltinRenderer(), other.getParticleLocation(), state);
    }

    protected PerspectiveProperties(boolean isAO, boolean isGui3D, boolean isBuiltinRenderer, ResourceLocation particleTexture, IModelState state) {
        super(isAO, isGui3D, isBuiltinRenderer, particleTexture);
        this.state = state;
    }

    @Override
    public IModelState getState() {
        return state;
    }

    public static ModelProperties.Builder builder(IPerspectiveProperties other) {
        return new Builder(other.isAO(), other.isGui3D(), other.isBuiltinRenderer(), other.getParticleLocation(), other.getState());
    }

    public static class Builder extends ModelProperties.Builder {

        private IModelState state;

        protected Builder(ModelProperties.Builder builder) {
            super(builder);
            if ( builder instanceof Builder ) {
                this.state = ((Builder) builder).state;
            }
        }

        protected Builder(boolean isAO, boolean isGui3D, boolean isBuiltinRenderer, ResourceLocation particleTexture, IModelState state) {
            super(isAO, isGui3D, isBuiltinRenderer, particleTexture);
            this.state = state;
        }

        @Override
        public Builder withState(IModelState value) {
            this.state = value;
            return this;
        }

        @Override
        public IPerspectiveProperties build() {
            return new PerspectiveProperties(super.build(), state);
        }
    }

}

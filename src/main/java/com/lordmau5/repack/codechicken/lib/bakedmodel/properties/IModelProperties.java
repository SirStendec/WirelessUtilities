package com.lordmau5.repack.codechicken.lib.bakedmodel.properties;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/**
 * Created by covers1624 on 19/01/19.
 */
public interface IModelProperties {

    /**
     * @return If Ambient Occlusion should be used.
     */
    boolean isAO();

    /**
     * @return If the model should be rendered like a block in the gui.
     */
    boolean isGui3D();

    /**
     * @return If the model has a builtin item renderer.
     */
    boolean isBuiltinRenderer();

    /**
     * @return The breaking particle texture.
     */
    TextureAtlasSprite getParticleTexture();

    ResourceLocation getParticleLocation();

}

package com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline;

import com.lordmau5.repack.codechicken.lib.bakedmodel.CachedFormat;
import com.lordmau5.repack.codechicken.lib.bakedmodel.ISmartVertexConsumer;
import com.lordmau5.repack.codechicken.lib.bakedmodel.Quad;
import com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.transformers.QuadReInterpolator;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * Anything implementing this may be used in the BakedPipeline.
 *
 * @author covers1624
 */
public interface IPipelineConsumer extends ISmartVertexConsumer {

    /**
     * The quad at the start of the transformation.
     * This is useful for obtaining the vertex data before any transformations have been applied,
     * such as interpolation, See {@link QuadReInterpolator}.
     * When overriding this make sure you call setInputQuad on your parent consumer too.
     *
     * @param quad The quad.
     */
    void setInputQuad(Quad quad);

    /**
     * Resets the Consumer to the new format.
     * This should resize any internal arrays if needed, ready for the new vertex data.
     *
     * @param format The format to reset to.
     */
    void reset(CachedFormat format);

    /**
     * Sets the parent consumer.
     * This consumer may choose to not pipe any data,
     * that's fine, but if it does, it MUST pipe the data to the one provided here.
     *
     * @param parent The parent.
     */
    void setParent(IVertexConsumer parent);
}

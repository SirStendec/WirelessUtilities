package com.lordmau5.repack.codechicken.lib.bakedmodel;

import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * Marks a standard IVertexConsumer as compatible with {@link Quad}.
 *
 * @author covers1624
 */
public interface ISmartVertexConsumer extends IVertexConsumer {

    /**
     * Assumes the data is already completely unpacked.
     * You must always copy the data from the quad provided to an internal cache.
     * basically:
     * this.quad.put(quad);
     *
     * @param quad The quad to copy data from.
     */
    void put(Quad quad);
}

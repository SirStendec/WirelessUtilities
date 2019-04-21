package com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.transformers;

import com.lordmau5.repack.codechicken.lib.bakedmodel.Quad;
import com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.IPipelineElementFactory;
import com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.QuadTransformer;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * This transformer tints quads..
 * Feed it the output of BlockColors.colorMultiplier.
 *
 * @author covers1624
 */
public class QuadTinter extends QuadTransformer {

    public static final IPipelineElementFactory<QuadTinter> FACTORY = QuadTinter::new;

    private int tint;

    QuadTinter() {
        super();
    }

    public QuadTinter(IVertexConsumer consumer, int tint) {
        super(consumer);
        this.tint = tint;
    }

    public QuadTinter setTint(int tint) {
        this.tint = tint;
        return this;
    }

    @Override
    public boolean transform() {
        // Nuke tintIndex.
        quad.tintIndex = -1;
        if ( format.hasColor ) {
            float r = (tint >> 0x10 & 0xFF) / 255F;
            float g = (tint >> 0x08 & 0xFF) / 255F;
            float b = (tint & 0xFF) / 255F;
            for (Quad.Vertex v : quad.vertices) {
                v.color[0] *= r;
                v.color[1] *= g;
                v.color[2] *= b;
            }
        }
        return true;
    }
}

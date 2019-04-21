package com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.transformers;

import com.lordmau5.repack.codechicken.lib.bakedmodel.Quad;
import com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.IPipelineElementFactory;
import com.lordmau5.repack.codechicken.lib.bakedmodel.pipeline.QuadTransformer;

/**
 * This transformer simply kicks the quad's face by the specified amount.
 * The direction of the kick it determined by the vertices normal data.
 * Created by covers1624 on 21/01/19.
 */
public class QuadFaceKicker extends QuadTransformer {

    public static final IPipelineElementFactory<QuadFaceKicker> FACTORY = QuadFaceKicker::new;

    private float kick;

    QuadFaceKicker() {
        super();
    }

    /**
     * Set the amount to kick by.
     *
     * @param kick The kick amount.
     */
    public void setKick(float kick) {
        this.kick = kick;
    }

    @Override
    public boolean transform() {
        for (Quad.Vertex vertex : quad.vertices) {
            vertex.vec[0] += vertex.normal[0] * kick;
            vertex.vec[1] += vertex.normal[1] * kick;
            vertex.vec[2] += vertex.normal[2] * kick;
        }
        return true;
    }
}

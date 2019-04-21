package com.lordmau5.repack.codechicken.lib.bakedmodel.properties;

import net.minecraftforge.common.model.IModelState;

/**
 * Created by covers1624 on 19/01/19.
 */
public interface IPerspectiveProperties extends IModelProperties {

    /**
     * @return Perspective based item transformations.
     */
    IModelState getState();

}

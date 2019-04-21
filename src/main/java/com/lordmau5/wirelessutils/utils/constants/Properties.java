package com.lordmau5.wirelessutils.utils.constants;

import com.lordmau5.repack.net.covers1624.model.UnlistedMapProperty;
import com.lordmau5.wirelessutils.utils.EnumFacingRotation;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;

public class Properties {
    public final static PropertyDirection FACING = PropertyDirection.create("facing");
    public final static PropertyEnum<EnumFacingRotation> FACING_ROTATION = PropertyEnum.create("facing", EnumFacingRotation.class);
    public final static PropertyBool ACTIVE = PropertyBool.create("active");
    public final static PropertyInteger LEVEL = PropertyInteger.create("level", 0, 9);

    public static final UnlistedMapProperty<String, String> MODEL_PROPERTIES = new UnlistedMapProperty<>("model_properties");

}

package com.lordmau5.repack.codechicken.lib.bakedmodel;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.*;

/**
 * This is mostly just extracted from the ForgeBlockStateV1.
 * Credits to Fry(Rain Warrior).
 * <p>
 * Suggestions for more defaults are welcome.
 * <p>
 * Created by covers1624 on 19/01/19.
 */
public class TransformUtils {

    private static final TRSRTransformation flipX = new TRSRTransformation(null, null, new Vector3f(-1, 1, 1), null);

    public static final IModelState DEFAULT_BLOCK;
    public static final IModelState DEFAULT_ITEM;
    public static final IModelState DEFAULT_TOOL;
    public static final IModelState DEFAULT_BOW;
    public static final IModelState DEFAULT_HANDHELD_ROD;

    static {
        Map<ItemCameraTransforms.TransformType, TRSRTransformation> map;
        TRSRTransformation thirdPerson;
        TRSRTransformation firstPerson;

        //@formatter:off
        map = new HashMap<>();
        thirdPerson = create(0F, 2.5F, 0F, 75F, 45F, 0F, 0.375F);
        map.put(GUI, create(0F, 0F, 0F, 30F, 225F, 0F, 0.625F));
        map.put(GROUND, create(0F, 3F, 0F, 0F, 0F, 0F, 0.25F));
        map.put(FIXED, create(0F, 0F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(THIRD_PERSON_RIGHT_HAND, thirdPerson);
        map.put(THIRD_PERSON_LEFT_HAND, flipLeft(thirdPerson));
        map.put(FIRST_PERSON_RIGHT_HAND, create(0F, 0F, 0F, 0F, 45F, 0F, 0.4F));
        map.put(FIRST_PERSON_LEFT_HAND, create(0F, 0F, 0F, 0F, 225F, 0F, 0.4F));
        DEFAULT_BLOCK = new SimpleModelState(ImmutableMap.copyOf(map));

        map = new HashMap<>();
        thirdPerson = create(0F, 3F, 1F, 0F, 0F, 0F, 0.55F);
        firstPerson = create(1.13F, 3.2F, 1.13F, 0F, -90F, 25F, 0.68F);
        map.put(GROUND, create(0F, 2F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(HEAD, create(0F, 13F, 7F, 0F, 180F, 0F, 1F));
        map.put(THIRD_PERSON_RIGHT_HAND, thirdPerson);
        map.put(THIRD_PERSON_LEFT_HAND, flipLeft(thirdPerson));
        map.put(FIRST_PERSON_RIGHT_HAND, firstPerson);
        map.put(FIRST_PERSON_LEFT_HAND, flipLeft(firstPerson));
        DEFAULT_ITEM = new SimpleModelState(ImmutableMap.copyOf(map));

        map = new HashMap<>();
        map.put(GROUND, create(0F, 2F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(THIRD_PERSON_RIGHT_HAND, create(0F, 4F, 0.5F, 0F, -90F, 55, 0.85F));
        map.put(THIRD_PERSON_LEFT_HAND, create(0F, 4F, 0.5F, 0F, 90F, -55, 0.85F));
        map.put(FIRST_PERSON_RIGHT_HAND, create(1.13F, 3.2F, 1.13F, 0F, -90F, 25, 0.68F));
        map.put(FIRST_PERSON_LEFT_HAND, create(1.13F, 3.2F, 1.13F, 0F, 90F, -25, 0.68F));
        DEFAULT_TOOL = new SimpleModelState(ImmutableMap.copyOf(map));

        map = new HashMap<>();
        map.put(GROUND, create(0F, 2F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(THIRD_PERSON_RIGHT_HAND, create(-1F, -2F, 2.5F, -80F, 260F, -40F, 0.9F));
        map.put(THIRD_PERSON_LEFT_HAND, create(-1F, -2F, 2.5F, -80F, -280F, 40F, 0.9F));
        map.put(FIRST_PERSON_RIGHT_HAND, create(1.13F, 3.2F, 1.13F, 0F, -90F, 25F, 0.68F));
        map.put(FIRST_PERSON_LEFT_HAND, create(1.13F, 3.2F, 1.13F, 0F, 90F, -25F, 0.68F));
        DEFAULT_BOW = new SimpleModelState(ImmutableMap.copyOf(map));

        map = new HashMap<>();
        map.put(GROUND, create(0F, 2F, 0F, 0F, 0F, 0F, 0.5F));
        map.put(THIRD_PERSON_RIGHT_HAND, create(0F, 4F, 2.5F, 0F, 90F, 55F, 0.85F));
        map.put(THIRD_PERSON_LEFT_HAND, create(0F, 4F, 2.5F, 0F, -90F, -55F, 0.85F));
        map.put(FIRST_PERSON_RIGHT_HAND, create(0F, 1.6F, 0.8F, 0F, 90F, 25F, 0.68F));
        map.put(FIRST_PERSON_LEFT_HAND, create(0F, 1.6F, 0.8F, 0F, -90F, -25F, 0.68F));
        DEFAULT_HANDHELD_ROD = new SimpleModelState(ImmutableMap.copyOf(map));
        //@formatter:on
    }

    /**
     * Creates a new TRSRTransformation.
     *
     * @param tx The x transform.
     * @param ty The y transform.
     * @param tz The z transform.
     * @param rx The x Axis rotation.
     * @param ry The y Axis rotation.
     * @param rz The z Axis rotation.
     * @param s  The scale.
     * @return The new TRSRTransformation.
     */
    public static TRSRTransformation create(float tx, float ty, float tz, float rx, float ry, float rz, float s) {
        return create(new Vector3f(tx / 16, ty / 16, tz / 16), new Vector3f(rx, ry, rz), new Vector3f(s, s, s));
    }

//    /**
//     * Creates a new TRSRTransformation.
//     *
//     * @param transform The transform.
//     * @param rotation  The rotation.
//     * @param scale     The scale.
//     * @return The new TRSRTransformation.
//     */
//    public static TRSRTransformation create(Vector3 transform, Vector3 rotation, Vector3 scale) {
//        return create(transform.vector3f(), rotation.vector3f(), scale.vector3f());
//    }

    /**
     * Creates a new TRSRTransformation.
     *
     * @param transform The transform.
     * @param rotation  The rotation.
     * @param scale     The scale.
     * @return The new TRSRTransformation.
     */
    public static TRSRTransformation create(Vector3f transform, Vector3f rotation, Vector3f scale) {
        return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(transform, TRSRTransformation.quatFromXYZDegrees(rotation), scale, null));
    }

    /**
     * Flips the transform for the left hand.
     *
     * @param transform The right hand transform.
     * @return The new left hand transform.
     */
    public static TRSRTransformation flipLeft(TRSRTransformation transform) {
        return TRSRTransformation.blockCenterToCorner(flipX.compose(TRSRTransformation.blockCornerToCenter(transform)).compose(flipX));
    }

}

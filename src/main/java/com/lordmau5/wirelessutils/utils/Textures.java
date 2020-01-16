package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Textures {

    public static TextureAtlasSprite ROBIN_0;
    public static TextureAtlasSprite ROBIN_1;
    public static TextureAtlasSprite ROBIN_2;

    public static TextureAtlasSprite LOCK;
    public static TextureAtlasSprite UNLOCK;

    public static TextureAtlasSprite ERROR;

    public static TextureAtlasSprite SIZE;
    public static TextureAtlasSprite OFFSET;

    public static TextureAtlasSprite INPUT_OUTPUT;

    public static TextureAtlasSprite ROUND_ROBIN;
    public static TextureAtlasSprite NEAREST_FIRST;
    public static TextureAtlasSprite FURTHEST_FIRST;
    public static TextureAtlasSprite RANDOM;

    public static TextureAtlasSprite COLOR;

    private static TextureMap textureMap;

    @SideOnly(Side.CLIENT)
    public static void registerIcons(TextureMap map) {
        textureMap = map;

        ROBIN_0 = register("robin_0");
        ROBIN_1 = register("robin_1");
        ROBIN_2 = register("robin_2");

        LOCK = register("lock");
        UNLOCK = register("unlock");

        ERROR = register("error");

        SIZE = register("size");
        OFFSET = register("offset");

        ROUND_ROBIN = register("round_robin");
        NEAREST_FIRST = register("nearest_first");
        FURTHEST_FIRST = register("furthest_first");
        RANDOM = register("random");

        INPUT_OUTPUT = register("input_output");
        COLOR = register("color");
    }

    private static TextureAtlasSprite register(String icon) {
        return textureMap.registerSprite(new ResourceLocation(WirelessUtils.MODID, "gui/icons/" + icon));
    }

}

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

    public static TextureAtlasSprite SIZE;
    public static TextureAtlasSprite OFFSET;

    private static TextureMap textureMap;

    @SideOnly(Side.CLIENT)
    public static void registerIcons(TextureMap map) {
        textureMap = map;

        ROBIN_0 = register("robin_0");
        ROBIN_1 = register("robin_1");
        ROBIN_2 = register("robin_2");

        LOCK = register("lock");
        UNLOCK = register("unlock");

        SIZE = register("size");
        OFFSET = register("offset");
    }

    private static TextureAtlasSprite register(String icon) {
        return textureMap.registerSprite(new ResourceLocation(WirelessUtils.MODID, "gui/icons/" + icon));
    }

}

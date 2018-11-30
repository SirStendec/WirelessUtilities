package com.lordmau5.wirelessutils.block.base;

import cofh.core.block.BlockCore;
import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBase extends BlockCore {

    public BlockBase(Material material) {
        super(material, WirelessUtils.MODID);
        setCreativeTab(WirelessUtils.creativeTabCU);
    }

    protected BlockBase(Material material, MapColor blockMapColor) {
        super(material, blockMapColor, WirelessUtils.MODID);
        setCreativeTab(WirelessUtils.creativeTabCU);
    }

    protected void setName(String name) {
        setTranslationKey(name);
        setRegistryName(name);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        Item item = Item.getItemFromBlock(this);
        ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }
}

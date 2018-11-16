package com.lordmau5.wirelessutils.item;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.IExplainableItem;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGlasses extends ItemArmor implements IExplainableItem {

    public ItemGlasses() {
        super(ModItems.GLASSES_MATERIAL, 1, EntityEquipmentSlot.HEAD);

        setTranslationKey(WirelessUtils.MODID + ".glasses");
        setRegistryName("glasses");
        setCreativeTab(WirelessUtils.creativeTabCU);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        addExplanation(tooltip, getTranslationKey() + ".info");
    }
}
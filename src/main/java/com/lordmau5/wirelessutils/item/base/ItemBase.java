package com.lordmau5.wirelessutils.item.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBase extends Item implements IExplainableItem {
    public ItemBase() {
        super();

        setCreativeTab(WirelessUtils.creativeTabCU);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public Item setName(String name) {
        setTranslationKey(name);
        setRegistryName(name);

        return this;
    }

    @Override
    @Nonnull
    public Item setTranslationKey(@Nonnull String translationKey) {
        return super.setTranslationKey(WirelessUtils.MODID + "." + translationKey);
    }

    public boolean isLocked(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean("Locked");
    }

    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        String out = super.getItemStackDisplayName(stack);

        if ( isLocked(stack) )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".locked",
                    out
            ).getUnformattedText();

        return out;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if ( isLocked(stack) )
            tooltip.add(StringHelper.localize("info." + WirelessUtils.MODID + ".locked.tip"));

        addExplanation(tooltip, stack.getTranslationKey() + ".info");
    }
}

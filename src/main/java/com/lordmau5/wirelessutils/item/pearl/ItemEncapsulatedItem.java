package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.pearl.EntityEncapsulatedItem;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemEncapsulatedItem extends ItemBasePearl {

    public ItemEncapsulatedItem() {
        super();
        setName("encapsulated_item");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        ItemStack stack = new ItemStack(this, 1, 0);
        ItemStack contained = new ItemStack(ModItems.itemFluxedPearl);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("Held", contained.serializeNBT());
        stack.setTagCompound(tag);

        items.add(stack);
    }

    @Nonnull
    public ItemStack getContainedItem(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("Held", Constants.NBT.TAG_COMPOUND) )
            return ItemStack.EMPTY;

        return new ItemStack(tag.getCompoundTag("Held"));
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".encapsulated_item.name",
                getContainedItem(stack).getDisplayName()
        ).getUnformattedText();
    }

    @Override
    public float getProjectileInaccuracy(@Nonnull ItemStack stack) {
        return 0F;
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityEncapsulatedItem(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityEncapsulatedItem(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityEncapsulatedItem(worldIn, stack);
    }
}

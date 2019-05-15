package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.pearl.EntityStabilizedEnderPearl;
import com.lordmau5.wirelessutils.item.base.IDimensionallyStableItem;
import com.lordmau5.wirelessutils.item.base.IJEIInformationItem;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import mezz.jei.api.IModRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemStabilizedEnderPearl extends ItemBasePearl implements IDimensionallyStableItem {

    public ItemStabilizedEnderPearl() {
        super();
        setName("stabilized_ender_pearl");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void registerJEI(IModRegistry registry) {
        IJEIInformationItem.addJEIInformation(registry, new ItemStack(this, 1, 0));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        items.add(new ItemStack(this, 1, 0));
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        return new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".accurate_pearl.name",
                new TextComponentTranslation("item.enderPearl.name")
        ).getUnformattedText();
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        addLocalizedLines(tooltip, "item." + WirelessUtils.MODID + ".accurate_pearl.info", TextHelpers.GREEN);
    }

    @Override
    public boolean allowDimensionalTravel() {
        return !ModConfig.items.voidPearl.enableVoiding;
    }

    @Override
    public void onPortalImpact(@Nonnull ItemStack stack, @Nonnull EntityItem entity, @Nonnull IBlockState state) {
        if ( !entity.world.isRemote && ModConfig.items.voidPearl.enableVoiding ) {
            stack = new ItemStack(ModItems.itemVoidPearl, 1);
            entity.setItem(stack);

            if ( entity.world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) entity.world;
                ws.spawnParticle(EnumParticleTypes.PORTAL, false, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
            }
        }

        ModItems.itemVoidPearl.onPortalImpact(stack, entity, state);
    }

    @Override
    public float getProjectileInaccuracy(@Nonnull ItemStack stack) {
        return 0F;
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityStabilizedEnderPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityStabilizedEnderPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityStabilizedEnderPearl(worldIn, stack);
    }
}

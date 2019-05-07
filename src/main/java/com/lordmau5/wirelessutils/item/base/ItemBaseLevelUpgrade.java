package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IUpgradeable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ItemBaseLevelUpgrade extends ItemBaseUpgrade {

    public ItemBaseLevelUpgrade() {
        super();

        setMaxStackSize(16);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    public Level getLevel(ItemStack stack) {
        return Level.fromInt(stack.getMetadata());
    }

    @Override
    public void onUpgradeInstalled(@Nullable EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IUpgradeable tile, @Nullable EnumFacing side, @Nonnull ItemStack stack) {
        super.onUpgradeInstalled(player, world, pos, tile, side, stack);
        if ( !world.isRemote && player instanceof EntityPlayerMP )
            ModAdvancements.UPGRADED.trigger((EntityPlayerMP) player);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if ( !ModConfig.upgrades.allowInWorld )
            return EnumActionResult.PASS;

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    @Nonnull
    public EnumRarity getRarity(@Nonnull ItemStack stack) {
        return getLevel(stack).rarity;
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        Level level = getLevel(stack);

        return new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.name",
                new TextComponentTranslation(getTranslationKey(stack) + ".name"),
                level.getName()
        ).getUnformattedText();
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        Level[] levels = Level.values();
        for (int i = 0; i < levels.length; i++) {
            Level level = levels[i];
            if ( level == Level.getMinLevel() )
                continue;

            items.add(new ItemStack(this, 1, i));
        }
    }
}

package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ILockExplanation;
import com.lordmau5.wirelessutils.item.base.ItemBaseUpgrade;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.tile.base.IUpgradeable;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class ItemAugment extends ItemBaseUpgrade implements ILockExplanation {

    public final static List<ItemAugment> AUGMENT_TYPES = new ArrayList<>();

    public ItemAugment() {
        super();

        AUGMENT_TYPES.add(this);

        setMaxStackSize(16);
        setMaxDamage(0);

        if ( getTiers() > 1 )
            setHasSubtypes(true);
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    @Nonnull
    public Level getRequiredLevel(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return Level.getMinLevel();

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("RequiredLevel") )
                return Level.fromInt(itemTag.getByte("RequiredLevel"));
        }

        Level out = getRequiredLevelDelegate(stack);
        if ( out == null )
            return Level.getMinLevel();

        return out;
    }

    @Nullable
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        if ( ModConfig.augments.requireMachineLevel )
            return getLevel(stack);

        return null;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        Level minLevel = getRequiredLevel(stack);
        if ( !minLevel.equals(Level.getMinLevel()) ) {
            tooltip.add(new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".augment.min_level",
                    minLevel.getTextComponent()
            ).getFormattedText());
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);

        if ( StringHelper.isControlKeyDown() ) {
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment_ctrl.active"));
            boolean had_machines = false;

            for (Class<? extends TileEntity> klass : CommonProxy.MACHINES) {
                if ( !IAugmentable.class.isAssignableFrom(klass) )
                    continue;

                @SuppressWarnings("unchecked")
                Class<? extends IAugmentable> augClass = (Class<? extends IAugmentable>) klass;

                Machine machine = klass.getAnnotation(Machine.class);
                if ( machine != null && canApplyTo(stack, augClass) ) {
                    had_machines = true;
                    tooltip.add(new TextComponentTranslation(
                            "item." + WirelessUtils.MODID + ".augment_ctrl.entry",
                            new TextComponentTranslation("tile." + WirelessUtils.MODID + "." + machine.name() + ".name").setStyle(TextHelpers.WHITE)
                    ).setStyle(TextHelpers.GRAY).getFormattedText());
                }
            }

            if ( !had_machines )
                tooltip.add(new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".augment_ctrl.entry",
                        new TextComponentTranslation("item." + WirelessUtils.MODID + ".augment_ctrl.none").setStyle(TextHelpers.WHITE)
                ).setStyle(TextHelpers.GRAY).getFormattedText());

        } else
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment_ctrl.inactive"));
    }

    @Nonnull
    public Level getLevel(@Nonnull ItemStack stack) {
        return Level.fromInt(stack.getMetadata());
    }

    public boolean isUpgrade(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        if ( newStack.getItem() != this )
            return false;

        if ( oldStack.getItem() != this )
            return true;

        return newStack.getMetadata() > oldStack.getMetadata();
    }

    public abstract void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable);

    public abstract boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass);

    public abstract boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable);

    public boolean shouldRequireLowerTier(@Nonnull ItemStack stack) {
        return true;
    }

    public boolean canInstallMultiple() {
        return false;
    }

    public int getTiers() {
        return 1;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        int tiers = getTiers();
        for (int i = 0; i < tiers; i++)
            items.add(new ItemStack(this, 1, i));
    }

    @Override
    public void onUpgradeInstalled(@Nullable EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IUpgradeable tile, @Nullable EnumFacing side, @Nonnull ItemStack stack) {
        if ( !world.isRemote && player instanceof EntityPlayerMP )
            ModAdvancements.AUGMENTED.trigger((EntityPlayerMP) player);
    }

    public void addSlotLockExplanation(@Nonnull List<String> tooltip, @Nonnull TileEntity tile, @Nonnull Slot slot, @Nonnull ItemStack stack) {
        if ( !ModConfig.augments.requirePreviousTiers || !(tile instanceof TileEntityBaseMachine) )
            return;

        TileEntityBaseMachine te = (TileEntityBaseMachine) tile;
        if ( te.hasHigherTierAugment(stack) )
            addLocalizedLines(tooltip, "info." + WirelessUtils.MODID + ".tiered.required.higher", TextHelpers.YELLOW);
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        if ( getTiers() > 1 )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".tiered.name",
                    name,
                    Level.fromAugment(stack).getName()
            ).getUnformattedText();

        return name;
    }
}

package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiAdminAugment;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.items.ContainerAdminAugment;
import com.lordmau5.wirelessutils.item.base.IAdminEditableItem;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.item.base.ILockExplanation;
import com.lordmau5.wirelessutils.item.base.ItemBaseUpgrade;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class ItemAugment extends ItemBaseUpgrade implements IAdminEditableItem, ILockExplanation {

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

    @Override
    public boolean hasEffect(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null && tag.hasKey("ForceEffect", Constants.NBT.TAG_BYTE) )
            return tag.getBoolean("ForceEffect");

        return super.hasEffect(stack);
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

    public double getEnergyMultiplier(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 1;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("EnergyMult") )
                return itemTag.getDouble("EnergyMult");
        }

        return getEnergyMultiplierDelegate(stack, augmentable);
    }

    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 1;
    }

    public int getEnergyAddition(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("EnergyAdd") )
                return itemTag.getInteger("EnergyAdd");
        }

        return getEnergyAdditionDelegate(stack, augmentable);
    }

    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 0;
    }

    public int getEneryDrain(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("EnergyDrain") )
                return itemTag.getInteger("EnergyDrain");
        }

        return getEnergyDrainDelegate(stack, augmentable);
    }

    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 0;
    }

    public int getBudgetAddition(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("BudgetAdd") )
                return tag.getInteger("BudgetAdd");
        }

        return getBudgetAdditionDelegate(stack, augmentable);
    }

    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 0;
    }

    public double getBudgetMultiplier(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 1D;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("BudgetMult") )
                return tag.getDouble("BudgetMult");
        }

        return getBudgetMultiplierDelegate(stack, augmentable);
    }

    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 1D;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if ( StringHelper.isControlKeyDown() ) {
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment_ctrl.active"));
            boolean had_machines = false;

            for (Class<? extends TileEntity> klass : CommonProxy.MACHINES) {
                if ( !IAugmentable.class.isAssignableFrom(klass) )
                    continue;

                Class<? extends IAugmentable> augClass = klass.asSubclass(IAugmentable.class);

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

        Level minLevel = getRequiredLevel(stack);
        if ( !minLevel.equals(Level.getMinLevel()) ) {
            tooltip.add(new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".augment.min_level",
                    minLevel.getTextComponent()
            ).getFormattedText());
        }

        double multiplier = getEnergyMultiplier(stack, null);
        int addition = getEnergyAddition(stack, null);
        int drain = getEneryDrain(stack, null);

        if ( multiplier != 1 || addition != 0 || drain != 0 ) {
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment.energy"));

            if ( drain != 0 )
                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".modifier.entry",
                        !StringHelper.isShiftKeyDown() && drain >= 1000 ? TextHelpers.getScaledNumber(drain, "RF/t", true) : StringHelper.formatNumber(drain) + " RF/t"
                ).getFormattedText());

            ITextComponent text = TextHelpers.getModifier(multiplier, addition);
            if ( text != null )
                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".modifier.entry",
                        new TextComponentTranslation(
                                "info." + WirelessUtils.MODID + ".modifier.combiner",
                                text,
                                StringHelper.localize("info." + WirelessUtils.MODID + ".modifier.energy")
                        )
                ).getFormattedText());
        }

        multiplier = getBudgetMultiplier(stack, null);
        addition = getBudgetAddition(stack, null);

        ITextComponent text = TextHelpers.getModifier(multiplier, addition);
        if ( text != null ) {
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment.budget"));

            tooltip.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".modifier.entry",
                    new TextComponentTranslation(
                            "info." + WirelessUtils.MODID + ".modifier.combiner",
                            text,
                            StringHelper.localize("info." + WirelessUtils.MODID + ".modifier.budget")
                    )
            ).getFormattedText());
        }

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

    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("AllowedMachines", Constants.NBT.TAG_LIST) ) {
                Machine machine = klass.getAnnotation(Machine.class);
                if ( machine == null )
                    return false;

                String name = machine.name();
                NBTTagList list = tag.getTagList("AllowedMachines", Constants.NBT.TAG_STRING);
                boolean allowed = false;
                for (int i = 0; i < list.tagCount(); i++) {
                    if ( name.equals(list.getStringTagAt(i)) ) {
                        allowed = true;
                        break;
                    }
                }

                if ( !allowed )
                    return false;
            }
        }

        return canApplyToDelegate(stack, klass);
    }

    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("AllowedMachines", Constants.NBT.TAG_LIST) ) {
                Machine machine = augmentable.getClass().getAnnotation(Machine.class);
                if ( machine == null )
                    return false;

                String name = machine.name();
                NBTTagList list = tag.getTagList("AllowedMachines", Constants.NBT.TAG_STRING);
                boolean allowed = false;
                for (int i = 0; i < list.tagCount(); i++) {
                    if ( name.equals(list.getStringTagAt(i)) ) {
                        allowed = true;
                        break;
                    }
                }

                if ( !allowed )
                    return false;
            }
        }

        return canApplyToDelegate(stack, augmentable);
    }

    public abstract boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass);

    public abstract boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable);

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
        if ( !(tile instanceof TileEntityBaseMachine) )
            return;

        TileEntityBaseMachine te = (TileEntityBaseMachine) tile;

        if ( ModConfig.augments.requirePreviousTiers && te.hasHigherTierAugment(stack) )
            addLocalizedLines(tooltip, "info." + WirelessUtils.MODID + ".tiered.required.higher", TextHelpers.YELLOW);
    }

    @Nullable
    public String getTierNameDelegate(@Nonnull ItemStack stack) {
        return null;
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        String tier = null;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("TierName", Constants.NBT.TAG_STRING) )
                tier = tag.getString("TierName");
        }

        if ( tier == null )
            tier = getTierNameDelegate(stack);

        if ( tier == null && getTiers() > 1 )
            tier = Level.fromAugment(stack).getName();

        if ( tier != null )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".tiered.name",
                    name,
                    tier
            ).getUnformattedText();

        return name;
    }

    @Override
    public void handleAdminPacket(@Nonnull ItemStack stack, EntityPlayer player, int slot, ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        if ( stack.isEmpty() || newStack.isEmpty() || stack.getItem() != newStack.getItem() || stack.getItem() != this )
            return;

        player.inventory.setInventorySlotContents(slot, newStack);

        boolean wantGui = packet.getBool();
        if ( wantGui && this instanceof IGuiItem )
            ((IGuiItem) this).openGui(player, slot);
    }

    @Nullable
    public PageBase getAdminGuiPage(GuiAdminAugment gui) {
        return null;
    }

    @Override
    public Object getClientAdminGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new GuiAdminAugment(new ContainerAdminAugment(stack, slot, player.inventory));
    }

    @Override
    public Object getServerAdminGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new ContainerAdminAugment(stack, slot, player.inventory);
    }
}

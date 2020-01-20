package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.augment.ITickableAugment;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseAE2;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBusAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.crafting.INBTPreservingIngredient;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemAEBusAugment extends ItemAugment implements IUpdateableItem, IGuiItem, INBTPreservingIngredient {

    public ItemAEBusAugment() {
        super();
        setName("ae_bus_augment");
    }

    /* Crafting */

    private final static String[] VALID_KEYS = {
            "TickRate",
            "Energy",
            "Items",
            "Fluid"
    };

    @Override
    public boolean isValidForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        return tag == null || !tag.getBoolean("Locked");
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTTagForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return null;

        NBTTagCompound out = new NBTTagCompound();
        for (String key : VALID_KEYS) {
            if ( tag.hasKey(key) )
                out.setTag(key, tag.getTag(key));
        }

        if ( out.getSize() == 0 )
            return null;

        return out;
    }


    /* Display */

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        final String name = "item." + WirelessUtils.MODID + ".ae_bus_augment";

        final byte rate = getTickRate(stack);
        final byte minRate = getMinTickRate(stack);

        if ( rate == minRate )
            tooltip.add(new TextComponentTranslation(
                    name + ".tick_rate",
                    TextHelpers.getComponent(rate).setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
        else
            tooltip.add(new TextComponentTranslation(
                    name + ".tick_rate.slow",
                    TextHelpers.getComponent(rate).setStyle(TextHelpers.WHITE),
                    TextHelpers.getComponent(minRate).setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());

        tooltip.add(StringHelper.localize(name + ".transfers"));

        if ( getEnergyRate(stack) > 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".transfer_line",
                    StringHelper.localize(name + ".energy_mode"),
                    getEnergyMode(stack).getComponent().setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());

        if ( getItemsRate(stack) > 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".transfer_line",
                    StringHelper.localize(name + ".items_mode"),
                    getItemsMode(stack).getComponent().setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());

        if ( getFluidRate(stack) > 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".transfer_line",
                    StringHelper.localize(name + ".fluid_mode"),
                    getFluidMode(stack).getComponent().setStyle(TextHelpers.WHITE)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
    }


    /* Transfers Enabled */

    @Nonnull
    public ItemStack setEnergyMode(@Nonnull ItemStack stack, @Nullable AEBusTickable.TransferMode mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode == null || mode == AEBusTickable.TransferMode.BOTH )
            tag.removeTag("Energy");
        else
            tag.setByte("Energy", (byte) mode.ordinal());

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nonnull
    public AEBusTickable.TransferMode getEnergyMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Energy", Constants.NBT.TAG_BYTE) )
                return AEBusTickable.TransferMode.byIndex(tag.getByte("Energy"));
        }

        return AEBusTickable.TransferMode.BOTH;
    }


    @Nonnull
    public ItemStack setFluidMode(@Nonnull ItemStack stack, @Nullable AEBusTickable.TransferMode mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode == null || mode == AEBusTickable.TransferMode.BOTH )
            tag.removeTag("Fluid");
        else
            tag.setByte("Fluid", (byte) mode.ordinal());

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nonnull
    public AEBusTickable.TransferMode getFluidMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Fluid", Constants.NBT.TAG_BYTE) )
                return AEBusTickable.TransferMode.byIndex(tag.getByte("Fluid"));
        }

        return AEBusTickable.TransferMode.BOTH;
    }


    @Nonnull
    public ItemStack setItemsMode(@Nonnull ItemStack stack, @Nullable AEBusTickable.TransferMode mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode == null || mode == AEBusTickable.TransferMode.BOTH )
            tag.removeTag("Items");
        else
            tag.setByte("Items", (byte) mode.ordinal());

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nonnull
    public AEBusTickable.TransferMode getItemsMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Items", Constants.NBT.TAG_BYTE) )
                return AEBusTickable.TransferMode.byIndex(tag.getByte("Items"));
        }

        return AEBusTickable.TransferMode.BOTH;
    }


    /* Transfer Rates */

    private int pickRate(int l, int[] list) {
        if ( l < 0 )
            l = 0;
        else if ( l >= list.length )
            l = list.length - 1;

        return list[l];
    }

    @Nonnull
    public ItemStack setTickRate(@Nonnull ItemStack stack, byte rate) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( rate <= getMinTickRate(stack) )
            tag.removeTag("TickRate");
        else
            tag.setByte("TickRate", rate);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public byte getTickRate(@Nonnull ItemStack stack) {
        final byte min = getMinTickRate(stack);
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("TickRate", Constants.NBT.TAG_BYTE) ) {
                final byte val = tag.getByte("TickRate");
                if ( val < min )
                    return min;
                return val;
            }
        }

        return min;
    }

    public byte getMinTickRate(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("M:TickRate", Constants.NBT.TAG_BYTE) )
                return tag.getByte("M:TickRate");
        }

        return (byte) pickRate(
                Level.fromItemStack(stack).toInt(),
                ModConfig.plugins.appliedEnergistics.aeBusAugment.tickRate
        );
    }


    public int getItemsRate(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("M:ItemRate", Constants.NBT.TAG_INT) )
                return tag.getInteger("M:ItemRate");
        }

        return pickRate(
                Level.fromItemStack(stack).toInt(),
                ModConfig.plugins.appliedEnergistics.aeBusAugment.itemsPerTick
        );
    }

    public int getFluidRate(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("M:FluidRate", Constants.NBT.TAG_INT) )
                return tag.getInteger("M:FluidRate");
        }

        return pickRate(
                Level.fromItemStack(stack).toInt(),
                ModConfig.plugins.appliedEnergistics.aeBusAugment.fluidPerTick
        );
    }

    public int getEnergyRate(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("M:EnergyRate", Constants.NBT.TAG_INT) )
                return tag.getInteger("M:EnergyRate");
        }

        return pickRate(
                Level.fromItemStack(stack).toInt(),
                ModConfig.plugins.appliedEnergistics.aeBusAugment.energyPerTick
        );
    }


    /* Stuff */

    @Override
    public int getTiers() {
        return Math.min(ModConfig.plugins.appliedEnergistics.aeBusAugment.tiers, Level.values().length);
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.plugins.appliedEnergistics.aeBusAugment.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        double[] multipliers = ModConfig.plugins.appliedEnergistics.aeBusAugment.energyMultiplier;
        if ( multipliers.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= multipliers.length )
            idx = multipliers.length - 1;

        return multipliers[idx];
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] additions = ModConfig.plugins.appliedEnergistics.aeBusAugment.energyAddition;
        if ( additions.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= additions.length )
            idx = additions.length - 1;

        return additions[idx];
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] drain = ModConfig.plugins.appliedEnergistics.aeBusAugment.energyDrain;
        if ( drain.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= drain.length )
            idx = drain.length - 1;

        return drain[idx];
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int[] additions = ModConfig.plugins.appliedEnergistics.aeBusAugment.budgetAddition;
        if ( additions.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= additions.length )
            idx = additions.length - 1;

        return additions[idx];
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        double[] multipliers = ModConfig.plugins.appliedEnergistics.aeBusAugment.budgetMultiplier;
        if ( multipliers.length == 0 )
            return 0;

        int idx = getLevel(stack).toInt();
        if ( idx >= multipliers.length )
            idx = multipliers.length - 1;

        return multipliers[idx];
    }

    /* Installation */

    @Override
    public ITickableAugment getTickableAugment(@Nonnull ItemStack stack, @Nonnull IAugmentable tile) {
        if ( canApplyTo(stack, tile) )
            return new AEBusTickable(stack, (TileEntityBaseAE2) tile);

        return null;
    }

    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        // Nothing. We use an ITickableAugment.
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IBusAugmentable.class.isAssignableFrom(klass) && TileEntityBaseAE2.class.isAssignableFrom(klass);
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IBusAugmentable && augmentable instanceof TileEntityBaseAE2;
    }

    /* Updating */

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if ( world.isRemote || player.isSneaking() )
            return super.onItemRightClick(world, player, hand);

        openGui(player, hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public Object getClientGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new GuiAEBusAugment(new ContainerAEBusAugment(stack, slot, player.inventory));
    }

    @Override
    public Object getServerGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new ContainerAEBusAugment(stack, slot, player.inventory);
    }

    @Override
    public void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        if ( stack.isEmpty() || newStack.isEmpty() || stack.getItem() != newStack.getItem() )
            return;

        setTickRate(stack, getTickRate(stack));
        setEnergyMode(stack, getEnergyMode(stack));
        setItemsMode(stack, getItemsMode(stack));
        setFluidMode(stack, getFluidMode(stack));

        player.inventory.setInventorySlotContents(slot, stack);
    }
}

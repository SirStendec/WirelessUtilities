package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiFacingAugment;
import com.lordmau5.wirelessutils.gui.container.items.ContainerFacingAugment;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFacingAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFacingAugment extends ItemAugment implements IUpdateableItem, IGuiItem {

    public ItemFacingAugment() {
        super();
        setName("facing_augment");
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.augments.facing.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.facing.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.facing.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.facing.energyDrain;
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.facing.budgetAddition;
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.facing.budgetMultiplier;
    }

    @Nullable
    public EnumFacing getFacing(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null && tag.hasKey("Facing") )
            return EnumFacing.byIndex(tag.getByte("Facing"));

        return null;
    }

    @Nonnull
    public ItemStack setFacing(@Nonnull ItemStack stack, @Nullable EnumFacing facing) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( facing == null )
            tag.removeTag("Facing");
        else
            tag.setByte("Facing", (byte) facing.ordinal());

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IFacingAugmentable )
            ((IFacingAugmentable) augmentable).setFacingAugmented(
                    !stack.isEmpty() && stack.getItem() == this,
                    getFacing(stack)
            );
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IFacingAugmentable.class.isAssignableFrom(klass);
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IFacingAugmentable;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if ( world.isRemote || player.isSneaking() )
            return super.onItemRightClick(world, player, hand);

        openGui(player, hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public Object getClientGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new GuiFacingAugment(new ContainerFacingAugment(stack, slot, player.inventory));
    }

    @Override
    public Object getServerGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new ContainerFacingAugment(stack, slot, player.inventory);
    }

    public void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        if ( stack.isEmpty() || newStack.isEmpty() || stack.getItem() != newStack.getItem() )
            return;

        setFacing(stack, getFacing(newStack));

        player.inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        EnumFacing facing = getFacing(stack);

        tooltip.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".blockpos.side",
                new TextComponentString(String.valueOf(facing)).setStyle(TextHelpers.WHITE)
        ).getFormattedText());
    }
}

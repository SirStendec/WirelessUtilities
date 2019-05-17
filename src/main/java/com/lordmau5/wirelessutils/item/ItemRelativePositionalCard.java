package com.lordmau5.wirelessutils.item;

import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemRelativePositionalCard extends ItemBasePositionalCard {

    public ItemRelativePositionalCard() {
        setName("relative_positional_card");
    }

    @Override
    public boolean isCardConfigured(@Nonnull ItemStack stack) {
        if ( !stack.hasTagCompound() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("X") && tag.hasKey("Y") && tag.hasKey("Z");
    }

    @Override
    public BlockPosDimension getTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("X") )
            return null;

        return new BlockPosDimension(
                origin.getX() + tag.getInteger("X"),
                origin.getY() + tag.getInteger("Y"),
                origin.getZ() + tag.getInteger("Z"),
                origin.getDimension(),
                tag.hasKey("Facing") ? EnumFacing.byIndex(tag.getByte("Facing")) : null
        );
    }

    public Vec3d getVector(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return null;

        return new Vec3d(
                -tag.getInteger("X"),
                -tag.getInteger("Y"),
                -tag.getInteger("Z")
        );
    }

    @Override
    public boolean updateCard(@Nonnull ItemStack stack, TileEntity container) {
        if ( stack.getItem() != this || container == null )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        BlockPos pos = container.getPos();
        World world = container.getWorld();
        if ( world == null || pos == null )
            return false;

        boolean updated = false;

        int dimension = world.provider.getDimension();
        if ( dimension != tag.getInteger("Dimension") ) {
            tag.setInteger("Dimension", dimension);
            updated = true;
        }

        long origin = pos.toLong();
        if ( origin != tag.getLong("Origin") ) {
            tag.setLong("Origin", origin);
            updated = true;
        }

        return updated;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        if ( isCardConfigured(stack) ) {
            NBTTagCompound tag = stack.getTagCompound();

            tooltip.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".blockpos.basic",
                    TextHelpers.formatRelative(tag.getInteger("X")),
                    TextHelpers.formatRelative(tag.getInteger("Y")),
                    TextHelpers.formatRelative(tag.getInteger("Z"))
            ).setStyle(TextHelpers.GRAY).getFormattedText());

            if ( tag.hasKey("Facing") )
                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".blockpos.side",
                        TextHelpers.getComponent(EnumFacing.byIndex(tag.getByte("Facing")).getName())
                ).setStyle(TextHelpers.GRAY).getFormattedText());
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getHighlightTip(ItemStack item, String displayName) {
        displayName = super.getHighlightTip(item, displayName);

        if ( isCardConfigured(item) ) {
            NBTTagCompound tag = item.getTagCompound();
            displayName = new TextComponentTranslation(
                    getTranslationKey() + ".highlight",
                    displayName,
                    new TextComponentTranslation(
                            "info." + WirelessUtils.MODID + ".blockpos.basic",
                            TextHelpers.formatRelative(tag.getInteger("X")),
                            TextHelpers.formatRelative(tag.getInteger("Y")),
                            TextHelpers.formatRelative(tag.getInteger("Z"))
                    ).setStyle(new Style().setColor(TextFormatting.AQUA).setItalic(true))
            ).getFormattedText();
        }

        return displayName;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if ( !worldIn.isRemote && playerIn.isSneaking() && handIn == EnumHand.MAIN_HAND ) {
            ItemStack stack = playerIn.getHeldItemMainhand();
            RayTraceResult ray = rayTrace(worldIn, playerIn, false);
            if ( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK )
                return new ActionResult<>(EnumActionResult.PASS, stack);

            NBTTagCompound tag = stack.getTagCompound();
            if ( tag == null || tag.getBoolean("Locked") )
                return new ActionResult<>(EnumActionResult.PASS, stack);
            else if ( stack.getCount() > 1 )
                tag = tag.copy();

            tag.removeTag("X");
            tag.removeTag("Y");
            tag.removeTag("Z");
            tag.removeTag("Stage");
            tag.removeTag("Origin");
            tag.removeTag("Dimension");
            tag.removeTag("Range");
            tag.removeTag("Facing");

            if ( tag.isEmpty() )
                tag = null;

            if ( stack.getCount() == 1 ) {
                stack.setTagCompound(tag);
            } else {
                ItemStack newStack = new ItemStack(this, 1);
                newStack.setTagCompound(tag);
                stack.shrink(1);
                if ( !playerIn.addItemStackToInventory(newStack) )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(newStack, worldIn, playerIn.getPositionVector());
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if ( !world.isRemote && player.isSneaking() && hand == EnumHand.MAIN_HAND ) {
            ItemStack stack = player.getHeldItemMainhand();
            BlockPosDimension target = new BlockPosDimension(pos, world.provider.getDimension(), side);
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag == null )
                tag = new NBTTagCompound();
            else if ( tag.getBoolean("Locked") )
                return EnumActionResult.PASS;
            else if ( stack.getCount() > 1 )
                tag = tag.copy();

            byte stage = tag.getByte("Stage");
            if ( stage == 0 ) {
                tag.setLong("Origin", target.toLong());
                tag.setInteger("Dimension", target.getDimension());

                TileEntity tile = world.getTileEntity(pos);
                if ( tile instanceof IPositionalMachine ) {
                    IPositionalMachine machine = (IPositionalMachine) tile;
                    if ( !machine.isInterdimensional() )
                        tag.setInteger("Range", machine.getRange());
                }

                tag.setByte("Stage", (byte) 1);
                tag.removeTag("X");
                tag.removeTag("Y");
                tag.removeTag("Z");
                tag.removeTag("Facing");

            } else {
                BlockPosDimension origin = new BlockPosDimension(
                        BlockPos.fromLong(tag.getLong("Origin")),
                        tag.getInteger("Dimension"),
                        null);

                if ( origin.getDimension() != target.getDimension() )
                    return EnumActionResult.SUCCESS;

                tag.setInteger("X", target.getX() - origin.getX());
                tag.setInteger("Y", target.getY() - origin.getY());
                tag.setInteger("Z", target.getZ() - origin.getZ());
                tag.setByte("Facing", (byte) target.getFacing().ordinal());
                tag.setByte("Stage", (byte) 2);

                if ( player instanceof EntityPlayerMP )
                    ModAdvancements.SET_POSITIONAL_CARD.trigger((EntityPlayerMP) player);
            }

            if ( stack.getCount() == 1 ) {
                stack.setTagCompound(tag);
            } else {
                ItemStack newStack = new ItemStack(this, 1);
                newStack.setTagCompound(tag);
                stack.shrink(1);
                if ( !player.addItemStackToInventory(newStack) )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(newStack, world, player.getPositionVector());
            }

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
}

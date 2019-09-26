package com.lordmau5.wirelessutils.item.cards;

import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiRelativeAreaCard;
import com.lordmau5.wirelessutils.gui.container.items.ContainerRelativeAreaCard;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import com.lordmau5.wirelessutils.item.base.ItemBaseAreaCard;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemRelativeAreaCard extends ItemBaseAreaCard implements IGuiItem {

    public ItemRelativeAreaCard() {
        super();
        setName("relative_area_card");
    }

    @Nonnull
    @Override
    public ItemStack handleUpdatePacketDelegate(@Nonnull final ItemStack stack, @Nonnull final EntityPlayer player, final int slot, @Nonnull final ItemStack newStack, @Nonnull final PacketUpdateItem packet) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        final NBTTagCompound newTag = newStack.getTagCompound();
        IUpdateableItem.copyOrRemoveNBTKeys(
                newTag, tag, false,
                "Origin", "Dimension",
                "X", "Y", "Z", "Facing",
                "Stage"
        );

        if ( tag.getSize() == 0 )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean isCardConfigured(@Nonnull ItemStack stack) {
        final NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.getInteger("X") != 0 || tag.getInteger("Y") != 0 || tag.getInteger("Z") != 0;
    }

    @Nullable
    public EnumFacing getFacing(@Nonnull ItemStack stack) {
        EnumFacing facing = null;
        if ( stack.getItem() == this ) {
            final NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Facing", Constants.NBT.TAG_BYTE) )
                facing = EnumFacing.byIndex(tag.getByte("Facing"));
        }

        if ( facing == null && !ModConfig.items.relativeCards.allowNullFacing )
            return EnumFacing.NORTH;

        return facing;
    }

    @Nullable
    public BlockPosDimension getTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin) {
        if ( !isCardConfigured(stack) )
            return null;

        final NBTTagCompound tag = stack.getTagCompound();
        final BlockPosDimension out = new BlockPosDimension(
                origin.getX() + tag.getInteger("X"),
                origin.getY() + tag.getInteger("Y"),
                origin.getZ() + tag.getInteger("Z"),
                origin.getDimension(),
                getFacing(stack)
        );

        if ( origin.isInsideWorld() && !out.isInsideWorld() )
            return null;

        return out;
    }

    @Nullable
    public Vec3d getVector(@Nonnull ItemStack stack) {
        if ( !isCardConfigured(stack) )
            return null;

        final NBTTagCompound tag = stack.getTagCompound();
        return new Vec3d(
                -tag.getInteger("X"),
                -tag.getInteger("Y"),
                -tag.getInteger("Z")
        );
    }

    @Nullable
    public BlockArea getHandRenderArea(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nullable EnumHand hand, int color) {
        final NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("Origin", Constants.NBT.TAG_LONG) )
            return null;

        final BlockPosDimension origin = new BlockPosDimension(
                BlockPos.fromLong(tag.getLong("Origin")),
                tag.getInteger("Dimension")
        );

        final byte stage = tag.getByte("Stage");
        if ( stage == 1 )
            return new BlockArea(origin, color, getCustomDisplayName(stack), null);

        if ( stage != 2 )
            return null;

        final Vec3d vector = getVector(stack);
        final BlockPosDimension target = getTarget(stack, origin);
        if ( target == null )
            return null;

        Tuple<BlockPosDimension, BlockPosDimension> corners = getCorners(stack, target);
        if ( corners == null )
            return null;

        return new BlockArea(
                corners.getFirst(), corners.getSecond(),
                color,
                getCustomDisplayName(stack),
                vector
        );
    }

    @Override
    public boolean updateCard(@Nonnull ItemStack stack, TileEntity container) {
        if ( stack.getItem() != this || container == null )
            return false;

        final NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        final BlockPos pos = container.getPos();
        final World world = container.getWorld();
        if ( world == null || pos == null )
            return false;

        boolean updated = false;
        final int dimension = world.provider.getDimension();
        final long origin = pos.toLong();

        if ( dimension != tag.getInteger("Dimension") ) {
            tag.setInteger("Dimension", dimension);
            updated = true;
        }

        if ( origin != tag.getLong("Origin") ) {
            tag.setLong("Origin", origin);
            updated = true;
        }

        return super.updateCard(stack, container) || updated;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if ( isCardConfigured(stack) ) {
            final NBTTagCompound tag = stack.getTagCompound();
            tooltip.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".blockpos.basic",
                    TextHelpers.formatRelative(tag.getInteger("X")),
                    TextHelpers.formatRelative(tag.getInteger("Y")),
                    TextHelpers.formatRelative(tag.getInteger("Z"))
            ).setStyle(TextHelpers.GRAY).getFormattedText());

            EnumFacing facing = getFacing(stack);
            tooltip.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".blockpos.side",
                    TextHelpers.getComponent(facing == null ? null : facing.getName())
            ).setStyle(TextHelpers.GRAY).getFormattedText());
        }

        addTooltipRangeInformation(stack, worldIn, tooltip, flagIn);
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
        if ( !playerIn.isSneaking() && handIn == EnumHand.MAIN_HAND ) {
            if ( !worldIn.isRemote )
                openGui(playerIn, handIn);

            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItemMainhand());
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public Object getClientGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new GuiRelativeAreaCard(new ContainerRelativeAreaCard(stack, slot, player.inventory));
    }

    public Object getServerGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new ContainerRelativeAreaCard(stack, slot, player.inventory);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if ( player.isSneaking() && hand == EnumHand.MAIN_HAND ) {
            ItemStack stack = player.getHeldItemMainhand();
            if ( !isLocked(stack) ) {
                if ( !world.isRemote ) {
                    BlockPosDimension target = new BlockPosDimension(pos, world.provider.getDimension(), side);
                    NBTTagCompound tag = stack.getTagCompound();
                    if ( tag == null )
                        tag = new NBTTagCompound();
                    else if ( stack.getCount() > 1 )
                        tag = tag.copy();

                    byte stage = tag.getByte("Stage");
                    if ( stage == 0 ) {
                        tag.setLong("Origin", target.toLong());
                        tag.setInteger("Dimension", target.getDimension());

                        tag.setByte("Stage", (byte) 1);
                        tag.removeTag("X");
                        tag.removeTag("Y");
                        tag.removeTag("Z");
                        tag.setByte("Facing", (byte) target.getFacing().ordinal());

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
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Nonnull
    @Override
    public ItemStack clearItem(@Nonnull ItemStack stack, @Nullable EntityPlayer player) {
        // The super method already creates a clone for us, so we don't have to worry
        // about that.
        stack = super.clearItem(stack, player);
        if ( stack.isEmpty() )
            return stack;

        final NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return stack;

        tag.removeTag("Origin");
        tag.removeTag("Dimension");
        tag.removeTag("X");
        tag.removeTag("Y");
        tag.removeTag("Z");
        tag.removeTag("Stage");
        tag.removeTag("Facing");
        tag.removeTag("Range");

        if ( tag.isEmpty() )
            stack.setTagCompound(null);

        return stack;
    }
}

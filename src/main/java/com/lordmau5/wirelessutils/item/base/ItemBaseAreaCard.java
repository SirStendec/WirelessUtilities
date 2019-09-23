package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBaseAreaCard extends ItemBasePositionalCard implements IClearableItem, IUpdateableItem {

    public ItemBaseAreaCard() {
        super();

        setMaxDamage(0);
        if ( getTiers() > 1 )
            setHasSubtypes(true);
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    /* Tiers */

    public int getTiers() {
        return Math.min(ModConfig.items.positionalAreaCards.availableTiers, Level.getMaxLevel().toInt());
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Nonnull
    public Level getLevel(@Nonnull ItemStack stack) {
        return Level.fromInt(stack.getMetadata());
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
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        String tier = null;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("TierName", Constants.NBT.TAG_STRING) )
                tier = tag.getString("TierName");
        }

        if ( tier == null && getTiers() > 1 )
            tier = Level.fromItemStack(stack).getName();

        if ( tier != null )
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".tiered.name",
                    name,
                    tier
            ).getUnformattedText();

        return name;
    }

    /* Target Stuff */

    @Nullable
    public Iterable<Tuple<BlockPosDimension, ItemStack>> getTargetArea(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return null;

        BlockPosDimension target = getTarget(stack, origin);
        if ( target == null )
            return null;

        Tuple<BlockPosDimension, BlockPosDimension> corners = getCorners(stack, target);
        if ( corners == null )
            return null;

        return IDirectionalMachine.iterateTargets(corners.getFirst(), corners.getSecond(), target.getFacing(), stack);
    }

    @Nullable
    public Tuple<BlockPosDimension, BlockPosDimension> getCorners(@Nonnull ItemStack stack, @Nonnull BlockPosDimension target) {
        return IDirectionalMachine.calculateCorners(
                target,
                target.getFacing(),
                getRangeWidth(stack), getRangeLength(stack), getRangeHeight(stack),
                getOffsetHorizontal(stack), getOffsetVertical(stack),
                false, false
        );
    }

    public void addTooltipRangeInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        final int height = getRangeHeight(stack);
        final int width = getRangeWidth(stack);
        final int length = getRangeLength(stack);

        if ( height > 0 || width > 0 || length > 0 ) {
            tooltip.add(new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".area_card.area",
                    TextHelpers.getComponent(1 + height * 2),
                    TextHelpers.getComponent(1 + width * 2),
                    TextHelpers.getComponent(1 + length * 2)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
        }
    }

    @Override
    public void addTooltipContext(@Nonnull List<String> tooltip, @Nonnull TileEntity tile, @Nonnull Slot slot, @Nonnull ItemStack stack) {

    }

    private ItemStack setByte(@Nonnull ItemStack stack, String key, byte value) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && value == 0 )
            return ItemStack.EMPTY;
        else if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( value == 0 )
            tag.removeTag(key);
        else
            tag.setByte(key, value);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    /* Offset */

    @Nonnull
    public ItemStack setOffsetHorizontal(@Nonnull ItemStack stack, byte offset) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && offset == 0 )
            return ItemStack.EMPTY;
        else if ( tag == null )
            tag = new NBTTagCompound();

        if ( offset == 0 )
            tag.removeTag("OffsetHorizontal");
        else
            tag.setByte("OffsetHorizontal", offset);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public int getOffsetHorizontal(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return 0;

        int offset = tag.getByte("OffsetHorizontal");
        int width = getRangeWidth(stack);

        if ( offset > width )
            return width;
        else if ( offset < -width )
            return -width;

        return offset;
    }

    @Nonnull
    public ItemStack setOffsetVertical(@Nonnull ItemStack stack, byte offset) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && offset == 0 )
            return ItemStack.EMPTY;
        else if ( tag == null )
            tag = new NBTTagCompound();

        if ( offset == 0 )
            tag.removeTag("OffsetVertical");
        else
            tag.setByte("OffsetVertical", offset);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public int getOffsetVertical(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return 0;

        int offset = tag.getByte("OffsetVertical");

        EnumFacing facing = getFacing(stack);
        boolean facingY = facing != null && facing.getAxis() == EnumFacing.Axis.Y;
        int maxVertical = facingY ? getRangeLength(stack) : getRangeHeight(stack);

        if ( offset > maxVertical )
            return maxVertical;
        else if ( offset < -maxVertical )
            return -maxVertical;

        return offset;
    }

    @Nonnull
    public ItemStack setFacing(@Nonnull ItemStack stack, @Nullable EnumFacing facing) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && facing == null )
            return ItemStack.EMPTY;
        else if ( tag == null )
            tag = new NBTTagCompound();

        if ( facing == null )
            tag.removeTag("facing");
        else
            tag.setByte("facing", (byte) facing.ordinal());

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public EnumFacing getFacing(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("facing", Constants.NBT.TAG_BYTE) )
            return null;

        return EnumFacing.byIndex(tag.getByte("facing"));
    }

    /* Range */

    public int getRange(@Nonnull ItemStack stack) {
        if ( stack.getItem() != this )
            return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null && tag.hasKey("Range", Constants.NBT.TAG_BYTE) )
            return tag.getByte("Range");

        final int[] blocks = ModConfig.items.positionalAreaCards.blocks;
        int index = stack.getMetadata();

        if ( index < 0 )
            index = 0;
        else if ( index >= blocks.length )
            index = blocks.length - 1;

        return blocks[index];
    }

    @Nonnull
    public ItemStack setRangeHeight(@Nonnull ItemStack stack, byte range) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        if ( range < 0 )
            range = 0;

        final int rangeHeight = getRangeHeight(stack);
        if ( range == rangeHeight )
            return ItemStack.EMPTY;

        if ( range > rangeHeight ) {
            final int totalRange = getRange(stack);
            final int rangeWidth = getRangeWidth(stack);
            final int rangeLength = getRangeLength(stack);

            while ( range > 0 && !IDirectionalMachine.isRangeValid(totalRange, range, rangeLength, rangeWidth) )
                range--;
        }

        return setByte(stack, "RangeHeight", range);
    }

    public int getRangeHeight(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return 0;

        return tag.getByte("RangeHeight");
    }

    @Nonnull
    public ItemStack setRangeWidth(@Nonnull ItemStack stack, byte range) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        if ( range < 0 )
            range = 0;

        final int rangeWidth = getRangeWidth(stack);
        if ( range == rangeWidth )
            return ItemStack.EMPTY;

        if ( range > rangeWidth ) {
            final int totalRange = getRange(stack);
            final int rangeHeight = getRangeHeight(stack);
            final int rangeLength = getRangeLength(stack);

            while ( range > 0 && !IDirectionalMachine.isRangeValid(totalRange, rangeHeight, rangeLength, range) )
                range--;
        }

        return setByte(stack, "RangeWidth", range);
    }

    public int getRangeWidth(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return 0;

        return tag.getByte("RangeWidth");
    }

    @Nonnull
    public ItemStack setRangeLength(@Nonnull ItemStack stack, byte range) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        if ( range < 0 )
            range = 0;

        final int rangeLength = getRangeLength(stack);
        if ( range == rangeLength )
            return ItemStack.EMPTY;

        if ( range > rangeLength ) {
            final int totalRange = getRange(stack);
            final int rangeHeight = getRangeHeight(stack);
            final int rangeWidth = getRangeWidth(stack);

            while ( range > 0 && !IDirectionalMachine.isRangeValid(totalRange, rangeHeight, range, rangeWidth) )
                range--;
        }

        return setByte(stack, "RangeLength", range);
    }

    public int getRangeLength(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return 0;

        return tag.getByte("RangeLength");
    }

    @Nonnull
    public ItemStack setRanges(@Nonnull ItemStack stack, int height, int length, int width) {
        if ( stack.getItem() != this )
            return ItemStack.EMPTY;

        height = Math.max(0, height);
        length = Math.max(0, length);
        width = Math.max(0, width);

        final int range = getRange(stack);

        while ( !IDirectionalMachine.isRangeValid(range, height, length, width) ) {
            if ( height > 0 )
                height--;
            if ( width > 0 )
                width--;
            if ( length > 0 )
                length--;

            if ( height == 0 && width == 0 && length == 0 )
                break;
        }

        stack = setByte(stack, "RangeHeight", (byte) height);
        if ( stack.isEmpty() )
            return stack;

        stack = setByte(stack, "RangeLength", (byte) length);
        if ( stack.isEmpty() )
            return stack;

        return setByte(stack, "RangeWidth", (byte) width);
    }

    @Nonnull
    public ItemStack handleUpdatePacketDelegate(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        return stack;
    }

    /* IUpdateableItem */

    public void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        if ( stack.isEmpty() || newStack.isEmpty() || stack.getItem() != newStack.getItem() )
            return;

        ItemStack out = setRanges(
                stack,
                getRangeHeight(newStack),
                getRangeLength(newStack),
                getRangeWidth(newStack)
        );

        if ( out.isEmpty() )
            return;

        out = handleUpdatePacketDelegate(out, player, slot, newStack, packet);
        if ( out.isEmpty() )
            return;

        NBTTagCompound tag = out.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return;

        NBTTagCompound newTag = newStack.getTagCompound();
        if ( newTag != null )
            IUpdateableItem.copyOrRemoveNBTKeys(
                    newTag, tag, false,
                    "OffsetVertical", "OffsetHorizontal"
            );

        if ( tag.getSize() == 0 )
            tag = null;

        out.setTagCompound(tag);

        if ( isCardConfigured(out) && player instanceof EntityPlayerMP )
            ModAdvancements.SET_POSITIONAL_CARD.trigger((EntityPlayerMP) player);

        player.inventory.setInventorySlotContents(slot, out);
    }

    /* IClearableItem */

    public boolean canClearItem(@Nonnull ItemStack stack, @Nullable EntityPlayer player) {
        return stack.getItem() == this && !isLocked(stack);
    }

    @Nonnull
    public ItemStack clearItem(@Nonnull ItemStack stack, @Nullable EntityPlayer player) {
        if ( stack.getItem() != this || !stack.hasTagCompound() || isLocked(stack) )
            return ItemStack.EMPTY;

        stack = stack.copy();
        final NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return ItemStack.EMPTY;

        tag.removeTag("RangeHeight");
        tag.removeTag("RangeLength");
        tag.removeTag("RangeWidth");
        tag.removeTag("OffsetVertical");
        tag.removeTag("OffsetHorizontal");

        if ( tag.isEmpty() )
            stack.setTagCompound(null);

        return stack;
    }
}

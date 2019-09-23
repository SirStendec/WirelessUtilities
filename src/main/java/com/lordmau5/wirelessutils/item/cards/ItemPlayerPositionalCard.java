package com.lordmau5.wirelessutils.item.cards;

import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiPlayerCard;
import com.lordmau5.wirelessutils.gui.container.items.ContainerPlayerCard;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import com.lordmau5.wirelessutils.item.base.ItemBaseEntityPositionalCard;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import joptsimple.internal.Strings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemPlayerPositionalCard extends ItemBaseEntityPositionalCard implements IUpdateableItem, IGuiItem {

    public ItemPlayerPositionalCard() {
        setName("player_positional_card");
    }

    @Override
    public boolean isCardConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("PlayerUUID");
    }

    public EntityPlayer getEntityTarget(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("PlayerUUID") )
            return null;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if ( server == null )
            return null;

        PlayerList players = server.getPlayerList();
        if ( players == null )
            return null;

        String s_uuid = tag.getString("PlayerUUID");
        if ( Strings.isNullOrEmpty(s_uuid) )
            return null;

        try {
            return players.getPlayerByUUID(UUID.fromString(s_uuid));
        } catch (IllegalArgumentException err) {
            return null;
        }
    }

    public Entity getEntityTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin) {
        return getEntityTarget(stack);
    }

    public String getPlayerName(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this || !stack.hasTagCompound() )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? null : tag.getString("Player");
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        String name = getPlayerName(stack);
        if ( name != null && !name.isEmpty() )
            tooltip.add(new TextComponentTranslation(
                    getTranslationKey() + ".player",
                    name
            ).getFormattedText());

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getHighlightTip(@Nonnull ItemStack stack, @Nonnull String displayName) {
        displayName = super.getHighlightTip(stack, displayName);

        if ( isCardConfigured(stack) ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag.hasKey("Player") )
                displayName = new TextComponentTranslation(
                        getTranslationKey() + ".highlight",
                        displayName,
                        new TextComponentString(tag.getString("Player")).setStyle(new Style().setColor(TextFormatting.AQUA).setItalic(true))
                ).getFormattedText();
        }

        return displayName;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if ( !world.isRemote && player.isSneaking() && hand == EnumHand.MAIN_HAND ) {
            ItemStack stack = player.getHeldItem(hand);
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag == null )
                tag = new NBTTagCompound();

            if ( tag.getBoolean("Locked") )
                return new ActionResult<>(EnumActionResult.PASS, stack);
            else if ( stack.getCount() > 1 )
                tag = tag.copy();

            tag.setString("Player", player.getName());
            tag.setString("PlayerUUID", player.getUniqueID().toString());

            if ( stack.getCount() == 1 ) {
                stack.setTagCompound(tag);
                // The GUI is not ready yet.
                // player.openGui(WirelessUtils.instance, WirelessUtils.GUI_PLAYER_CARD, player.getEntityWorld(), hand.ordinal(), 0, 0);

            } else {
                ItemStack newStack = new ItemStack(this, 1);
                newStack.setTagCompound(tag);
                stack.shrink(1);
                if ( !player.addItemStackToInventory(newStack) )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(newStack, world, player.getPositionVector());
            }

            if ( player instanceof EntityPlayerMP )
                ModAdvancements.SET_POSITIONAL_CARD.trigger((EntityPlayerMP) player);

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    public Object getClientGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new GuiPlayerCard(new ContainerPlayerCard(stack, slot, player.inventory));
    }

    public Object getServerGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new ContainerPlayerCard(stack, slot, player.inventory);
    }

    public void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        if ( tag.getBoolean("Locked") )
            return;

        EntityPlayer target = getEntityTarget(stack);
        if ( target != null && target != player && !player.capabilities.isCreativeMode )
            return;

        stack.setTagCompound(tag);
        player.inventory.setInventorySlotContents(slot, stack);
    }
}

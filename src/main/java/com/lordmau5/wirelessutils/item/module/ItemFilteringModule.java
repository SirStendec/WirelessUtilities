package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemFilteringModule extends ItemModule {

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return super.isConfigured(stack);

        return tag.hasKey("Whitelist") || tag.hasKey("Blacklist") || tag.hasKey("ChildMode") || tag.hasKey("NameMode") || tag.hasKey("SneakMode") || super.isConfigured(stack);
    }

    protected int getDefaultPlayerMode() {
        return 1;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = "item." + WirelessUtils.MODID + ".filtered_module";

        int mode = getChildMode(stack);
        if ( mode != 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".age",
                    StringHelper.localize("btn." + WirelessUtils.MODID + ".age_mode." + mode)
            ).getFormattedText());

        mode = getNamedMode(stack);
        if ( mode != 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".named",
                    StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + mode)
            ).getFormattedText());

        mode = getPlayerMode(stack);
        if ( mode != 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".player",
                    StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + mode)
            ).getFormattedText());

        mode = getSneakMode(stack);
        if ( mode != 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".sneak",
                    StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + mode)
            ).getFormattedText());

        String[] blacklist = getBlacklist(stack);
        if ( blacklist != null ) {
            String bl = StringHelper.localize("btn." + WirelessUtils.MODID + "." + (isWhitelist(stack) ? "whitelist" : "blacklist"));
            if ( blacklist.length == 1 || StringHelper.isControlKeyDown() ) {
                tooltip.add(bl);
                for (String key : blacklist)
                    tooltip.add(new TextComponentTranslation(
                            name + ".entry",
                            key
                    ).getFormattedText());
            } else
                tooltip.add(new TextComponentTranslation(
                        name + ".info_list",
                        bl,
                        blacklist.length
                ).getFormattedText());
        }
    }

    @Nullable
    public String[] getBlacklist(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Blacklist", Constants.NBT.TAG_LIST) ) {
                NBTTagList list = tag.getTagList("Blacklist", Constants.NBT.TAG_STRING);
                if ( list.tagCount() == 0 )
                    return null;

                String[] out = new String[list.tagCount()];
                for (int i = 0; i < out.length; i++)
                    out[i] = list.getStringTagAt(i);

                return out;
            }
        }

        return null;
    }

    @Nonnull
    public ItemStack setBlacklist(@Nonnull ItemStack stack, String blacklist) {
        if ( blacklist == null )
            return setBlacklist(stack, (String[]) null);

        return setBlacklist(stack, blacklist.split("[ \t\r]*\n[ \t\r]*"));
    }

    @Nonnull
    public ItemStack setBlacklist(@Nonnull ItemStack stack, String[] blacklist) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( blacklist == null || blacklist.length == 0 )
            tag.removeTag("Blacklist");
        else {
            NBTTagList list = new NBTTagList();
            for (String word : blacklist)
                if ( word != null && !word.isEmpty() )
                    list.appendTag(new NBTTagString(word));

            if ( list.tagCount() == 0 )
                tag.removeTag("Blacklist");
            else
                tag.setTag("Blacklist", list);
        }

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if ( entity.world.isRemote )
            return false;

        ResourceLocation key = EntityList.getKey(entity);
        if ( key == null )
            return false;

        String name = key.toString();
        String[] blacklist = getBlacklist(stack);

        if ( blacklist == null )
            setBlacklist(stack, new String[]{name});
        else {
            boolean found = false;
            for (int i = 0; i < blacklist.length; i++) {
                if ( name.equals(blacklist[i]) ) {
                    found = true;
                    blacklist[i] = null;
                    break;
                }
            }

            if ( !found ) {
                String[] newList = new String[blacklist.length + 1];
                System.arraycopy(blacklist, 0, newList, 0, blacklist.length);
                newList[blacklist.length] = name;
                setBlacklist(stack, newList);

            } else
                setBlacklist(stack, blacklist);
        }

        player.setHeldItem(hand, stack);
        player.getCooldownTracker().setCooldown(this, 5);

        return true;
    }

    public int getPlayerMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("PlayerMode") )
                return tag.getByte("PlayerMode");
        }

        return getDefaultPlayerMode();
    }

    @Nonnull
    public ItemStack setPlayerMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < 0 )
            mode = 2;
        else if ( mode > 2 )
            mode = 0;

        if ( mode == getDefaultPlayerMode() )
            tag.removeTag("PlayerMode");
        else
            tag.setByte("PlayerMode", (byte) mode);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }


    public int getSneakMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null )
                return tag.getByte("SneakMode");
        }

        return 0;
    }

    @Nonnull
    public ItemStack setSneakMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < 0 )
            mode = 2;
        else if ( mode > 2 )
            mode = 0;

        if ( mode == 0 )
            tag.removeTag("SneakMode");
        else
            tag.setByte("SneakMode", (byte) mode);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean isWhitelist(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("Whitelist");
        }

        return false;
    }

    @Nonnull
    public ItemStack setWhitelist(@Nonnull ItemStack stack, boolean enabled) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( enabled )
            tag.setBoolean("Whitelist", true);
        else
            tag.removeTag("Whitelist");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public int getNamedMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null )
                return tag.getByte("NamedMode");
        }

        return 0;
    }

    @Nonnull
    public ItemStack setNamedMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < 0 )
            mode = 2;
        else if ( mode > 2 )
            mode = 0;

        if ( mode == 0 )
            tag.removeTag("NamedMode");
        else
            tag.setByte("NamedMode", (byte) mode);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public int getChildMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null )
                return tag.getByte("ChildMode");
        }

        return 0;
    }

    @Nonnull
    public ItemStack setChildMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < 0 )
            mode = 2;
        else if ( mode > 2 )
            mode = 0;

        if ( mode == 0 )
            tag.removeTag("ChildMode");
        else
            tag.setByte("ChildMode", (byte) mode);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public abstract static class FilteredBehavior implements TileBaseVaporizer.IVaporizerBehavior {

        public final TileBaseVaporizer vaporizer;

        // Player Modes
        protected boolean allowPlayers = false;
        protected boolean allowCreative = false;

        // Living Modes
        protected boolean allowBosses = false;
        protected boolean requireAttackable = true;
        protected boolean requireAlive = true;

        // Items
        protected boolean obeyItemTags = false;

        // Configurable Modes
        private int playerMode = 0;
        private int childMode = 0;
        private int namedMode = 0;
        private int sneakMode = 0;

        private boolean whitelist = false;
        private String[] blacklist = null;

        private Predicate<? super Entity> predicate = null;

        public FilteredBehavior(@Nonnull TileBaseVaporizer vaporizer) {
            this.vaporizer = vaporizer;
        }

        public boolean allowPlayers() {
            return allowPlayers;
        }

        public int getPlayerMode() {
            return playerMode;
        }

        public int getChildMode() {
            return childMode;
        }

        public int getNamedMode() {
            return namedMode;
        }

        public int getSneakMode() {
            return sneakMode;
        }

        public boolean isWhitelist() {
            return whitelist;
        }

        @Nullable
        public String[] getBlacklist() {
            return blacklist;
        }

        public void updateModule(@Nonnull ItemStack stack) {
            ItemFilteringModule item = (ItemFilteringModule) stack.getItem();

            playerMode = item.getPlayerMode(stack);
            childMode = item.getChildMode(stack);
            namedMode = item.getNamedMode(stack);
            sneakMode = item.getSneakMode(stack);

            blacklist = item.getBlacklist(stack);
            whitelist = item.isWhitelist(stack);

            // In theory, this might do something in the future.
            updatePredicate();
        }

        public void updatePredicate() {
            // TODO: Optimize this to output a more streamlined method?
            predicate = entity -> {
                // We never want dead entities.
                if ( entity == null || entity.isDead )
                    return false;

                // We *sometimes* want players.
                if ( entity instanceof EntityPlayer ) {
                    if ( !allowPlayers || playerMode == 1 )
                        return false;

                    EntityPlayer player = (EntityPlayer) entity;
                    if ( player.isSpectator() )
                        return false;

                    if ( !allowCreative && player.capabilities.isCreativeMode )
                        return false;

                } else if ( playerMode == 2 )
                    return false;

                if ( entity instanceof EntityLivingBase ) {
                    EntityLivingBase living = (EntityLivingBase) entity;

                    if ( requireAttackable && !living.attackable() )
                        return false;

                    if ( requireAlive && !living.isEntityAlive() )
                        return false;

                    if ( !allowBosses && !living.isNonBoss() )
                        return false;

                    if ( sneakMode != 0 && living.isSneaking() == (sneakMode == 1) )
                        return false;

                    if ( childMode != 0 && living.isChild() != (childMode == 1) )
                        return false;

                } else if ( obeyItemTags && entity instanceof EntityItem ) {
                    NBTTagCompound tag = entity.getEntityData();
                    if ( tag != null && tag.getBoolean("PreventRemoteMovement") && !tag.getBoolean("AllowMachineRemoteMovement") )
                        return false;
                }

                if ( namedMode != 0 && entity.getCustomNameTag().isEmpty() != (namedMode == 1) )
                    return false;

                if ( blacklist != null ) {
                    ResourceLocation name = EntityList.getKey(entity);
                    if ( name != null ) {
                        String ns = name.getNamespace() + ":*";
                        String key = name.toString();
                        for (String word : blacklist)
                            if ( key.equals(word) || ns.equals(word) ) {
                                if ( whitelist )
                                    break;
                                else
                                    return false;
                            }
                    } else if ( whitelist )
                        return false;

                } else if ( whitelist )
                    return false;

                return true;
            };
        }

        public void updateModePacket(@Nonnull PacketBase packet) {
            packet.addByte(playerMode);
            packet.addByte(childMode);
            packet.addByte(namedMode);
            packet.addByte(sneakMode);
            packet.addBool(whitelist);

            if ( blacklist == null )
                packet.addInt(0);
            else {
                packet.addInt(blacklist.length);
                for (String word : blacklist)
                    packet.addString(word);
            }
        }

        public void handleModePacket(@Nonnull PacketBase packet) {
            ItemStack stack = vaporizer.getModule();
            ItemFilteringModule item = (ItemFilteringModule) stack.getItem();

            item.setPlayerMode(stack, packet.getByte());
            item.setChildMode(stack, packet.getByte());
            item.setNamedMode(stack, packet.getByte());
            item.setSneakMode(stack, packet.getByte());
            item.setWhitelist(stack, packet.getBool());

            int count = packet.getInt();
            if ( count == 0 )
                blacklist = null;
            else {
                blacklist = new String[count];
                for (int i = 0; i < count; i++)
                    blacklist[i] = packet.getString();
            }

            item.setBlacklist(stack, blacklist);

            stack = handleModeDelegate(stack, packet);
            vaporizer.setModule(stack);
        }

        @Nonnull
        public ItemStack handleModeDelegate(@Nonnull ItemStack stack, @Nonnull PacketBase packet) {
            return stack;
        }

        @Nullable
        public Class<? extends Entity> getEntityClass() {
            return EntityLivingBase.class;
        }

        @Nullable
        public Predicate<? super Entity> getEntityFilter() {
            return predicate;
        }

        @Override
        public boolean canRun(boolean ignorePower) {
            if ( whitelist && (blacklist == null || blacklist.length == 0) )
                return false;

            return true;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( whitelist && (blacklist == null || blacklist.length == 0) )
                return "info." + WirelessUtils.MODID + ".vaporizer.empty_whitelist";

            return null;
        }
    }

}

package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.elements.ElementSlaughterModule;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSlaughterModule extends ItemModule {

    public ItemSlaughterModule() {
        super();
        setName("slaughter_module");
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null ) {
            if ( tag.hasKey("Whitelist") || tag.hasKey("Blacklist") || tag.hasKey("AdultsOnly") || tag.hasKey("CollectDrops") || tag.hasKey("CollectExp") )
                name = new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".configured",
                        name
                ).getFormattedText();
        }

        return name;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String[] blacklist = getBlacklist(stack);
        String name = "item." + WirelessUtils.MODID + ".slaughter_module";

        tooltip.add(new TextComponentTranslation(
                name + ".drops",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getDropMode(stack))
        ).getFormattedText());

        tooltip.add(new TextComponentTranslation(
                name + ".exp",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getExperienceMode(stack))
        ).getFormattedText());

        if ( getAdultsOnly(stack) )
            tooltip.add(StringHelper.localize("btn." + WirelessUtils.MODID + ".child_mode.1"));

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
                if ( word != null )
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

    public boolean isWhitelist(@Nullable ItemStack stack) {
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

    public boolean getAdultsOnly(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("AdultsOnly");
        }

        return false;
    }

    @Nonnull
    public ItemStack setAdultsOnly(@Nonnull ItemStack stack, boolean enabled) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( enabled )
            tag.setBoolean("AdultsOnly", true);
        else
            tag.removeTag("AdultsOnly");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public int getDropMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("CollectDrops", Constants.NBT.TAG_BYTE) )
                return tag.getByte("CollectDrops");
        }

        return ModConfig.vaporizers.modules.slaughter.collectDrops;
    }

    @Nonnull
    public ItemStack setDropMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < ModConfig.vaporizers.modules.slaughter.collectDropsMinimum )
            mode = 3;
        else if ( mode > 3 )
            mode = ModConfig.vaporizers.modules.slaughter.collectDropsMinimum;

        tag.setByte("CollectDrops", (byte) mode);
        stack.setTagCompound(tag);
        return stack;
    }

    public int getExperienceMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("CollectExp", Constants.NBT.TAG_BYTE) )
                return tag.getByte("CollectExp");
        }

        return ModConfig.vaporizers.modules.slaughter.collectExperience;
    }

    @Nonnull
    public ItemStack setExperienceMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum )
            mode = 3;
        else if ( mode > 3 )
            mode = ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum;

        tag.setByte("CollectExp", (byte) mode);
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer
            vaporizer) {
        return new SlaughterBehavior(vaporizer, stack);
    }

    public static class VaporizerDamage extends EntityDamageSource {
        private final TileBaseVaporizer vaporizer;

        public VaporizerDamage(EntityPlayer player, TileBaseVaporizer vaporizer) {
            super("player", player);
            this.vaporizer = vaporizer;
        }

        public TileBaseVaporizer getVaporizer() {
            return vaporizer;
        }

        @Override
        public boolean isDifficultyScaled() {
            return false;
        }

        @Override
        public boolean isUnblockable() {
            return ModConfig.vaporizers.modules.slaughter.unblockable;
        }

        @Override
        public boolean isDamageAbsolute() {
            return ModConfig.vaporizers.modules.slaughter.absolute;
        }

        @Override
        public ITextComponent getDeathMessage(EntityLivingBase entity) {
            ItemStack weapon = damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase) damageSourceEntity).getHeldItemMainhand() : ItemStack.EMPTY;
            if ( !weapon.isEmpty() && weapon.hasDisplayName() )
                return new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".slaughter_module.kill.item",
                        entity.getDisplayName(),
                        weapon.getTextComponent()
                );

            return new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".slaughter_module.kill",
                    entity.getDisplayName()
            );
        }
    }

    public static class SlaughterBehavior implements TileBaseVaporizer.IVaporizerBehavior {
        public final TileBaseVaporizer vaporizer;

        public boolean adultsOnly = false;
        int dropMode = 0;
        int experienceMode = 0;

        public boolean whitelist = false;
        public String[] blacklist = null;

        public SlaughterBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            this.vaporizer = vaporizer;
            updateModule(module);
        }

        public void updateModule(@Nonnull ItemStack stack) {
            dropMode = ModItems.itemSlaughterModule.getDropMode(stack);
            experienceMode = ModItems.itemSlaughterModule.getExperienceMode(stack);
            adultsOnly = ModItems.itemSlaughterModule.getAdultsOnly(stack);
            blacklist = ModItems.itemSlaughterModule.getBlacklist(stack);
            whitelist = ModItems.itemSlaughterModule.isWhitelist(stack);
        }

        public void updateModifier(@Nonnull ItemStack stack) {

        }

        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementSlaughterModule(gui, this);
        }

        public boolean wantsFluid() {
            return false;
        }

        public boolean canInvert() {
            return false;
        }

        public void updateModePacket(@Nonnull PacketBase packet) {
            packet.addBool(adultsOnly);
            packet.addByte(dropMode);
            packet.addByte(experienceMode);
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
            ItemStack module = vaporizer.getModule();
            ModItems.itemSlaughterModule.setAdultsOnly(module, packet.getBool());
            ModItems.itemSlaughterModule.setDropMode(module, packet.getByte());
            ModItems.itemSlaughterModule.setExperienceMode(module, packet.getByte());
            ModItems.itemSlaughterModule.setWhitelist(module, packet.getBool());

            int count = packet.getInt();
            if ( count == 0 )
                blacklist = null;
            else {
                blacklist = new String[count];
                for (int i = 0; i < count; i++)
                    blacklist[i] = packet.getString();
            }

            ModItems.itemSlaughterModule.setBlacklist(module, blacklist);

            vaporizer.setModule(module);
        }

        public int getExperienceMode() {
            return Math.max(experienceMode, ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum);
        }

        @Override
        public int getDropMode() {
            return Math.max(dropMode, ModConfig.vaporizers.modules.slaughter.collectDropsMinimum);
        }

        public Class<? extends Entity> getEntityClass() {
            return EntityLivingBase.class;
        }

        @Nullable
        public Predicate<? super Entity> getEntityFilter() {
            if ( !adultsOnly && blacklist == null && !whitelist )
                return null;

            return entity -> {
                if ( entity == null || (adultsOnly && entity instanceof EntityLiving && ((EntityLiving) entity).isChild()) )
                    return false;

                if ( entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator() )
                    return false;

                if ( blacklist != null ) {
                    ResourceLocation name = EntityList.getKey(entity);
                    if ( name != null ) {
                        String key = name.toString();
                        for (String word : blacklist)
                            if ( key.equals(word) != whitelist )
                                return false;
                    }
                } else if ( whitelist )
                    return false;

                return true;
            };
        }

        public boolean isInputUnlocked(int slot) {
            return ModConfig.vaporizers.modules.slaughter.enableWeapon && slot == 0;
        }

        public boolean isModifierUnlocked() {
            return false;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return ItemStack.EMPTY;
        }

        public boolean isValidInput(@Nonnull ItemStack stack) {
            return ModConfig.vaporizers.modules.slaughter.enableWeapon;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return false;
        }

        public boolean canRun() {
            return true;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            if ( !(entity instanceof EntityLivingBase) )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            EntityLivingBase living = (EntityLivingBase) entity;
            if ( !living.attackable() || !living.isEntityAlive() )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            if ( entity instanceof EntityPlayer && (((EntityPlayer) entity).capabilities.isCreativeMode || !ModConfig.vaporizers.modules.slaughter.targetPlayers || (entity.isSneaking() && ModConfig.vaporizers.modules.slaughter.ignoreSneaking)) )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            if ( !ModConfig.vaporizers.modules.slaughter.attackBosses && !living.isNonBoss() )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            TileBaseVaporizer.WUVaporizerPlayer player = vaporizer.getFakePlayer(living.world);
            if ( player == null )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            ItemStack weapon = ModConfig.vaporizers.modules.slaughter.enableWeapon ? vaporizer.getInput().getStackInSlot(0) : ItemStack.EMPTY;
            player.setHeldItem(EnumHand.MAIN_HAND, weapon);

            float damage = living.getHealth();
            if ( ModConfig.vaporizers.modules.slaughter.maxDamage != 0 && damage > ModConfig.vaporizers.modules.slaughter.maxDamage )
                damage = (float) ModConfig.vaporizers.modules.slaughter.maxDamage;

            boolean success = living.attackEntityFrom(new VaporizerDamage(player, vaporizer), damage);
            int weaponDamage = ModConfig.vaporizers.modules.slaughter.damageWeapon;
            if ( success && weaponDamage > 0 && !vaporizer.isCreative() ) {
                weapon.damageItem(weaponDamage, player);
                if ( weapon.isEmpty() )
                    vaporizer.getInput().setStackInSlot(0, weapon);
                vaporizer.markChunkDirty();
            }

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}

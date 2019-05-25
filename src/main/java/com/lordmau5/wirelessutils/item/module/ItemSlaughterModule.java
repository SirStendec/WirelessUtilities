package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSlaughterModule extends ItemFilteringModule {

    public ItemSlaughterModule() {
        super();
        setName("slaughter_module");
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("CollectDrops") || tag.hasKey("CollectExp") || super.isConfigured(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = "item." + WirelessUtils.MODID + ".slaughter_module";

        tooltip.add(new TextComponentTranslation(
                name + ".drops",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getDropMode(stack))
        ).getFormattedText());

        tooltip.add(new TextComponentTranslation(
                name + ".exp",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getExperienceMode(stack))
        ).getFormattedText());
    }

    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
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

    public static class SlaughterBehavior extends FilteredBehavior {

        private int dropMode = 0;
        private int experienceMode = 0;

        public SlaughterBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            super(vaporizer);

            allowPlayers = ModConfig.vaporizers.modules.slaughter.targetPlayers;
            allowCreative = false;

            requireAttackable = true;
            requireAlive = true;

            updateModule(module);
        }

        @Override
        public void updateModule(@Nonnull ItemStack stack) {
            super.updateModule(stack);

            dropMode = ModItems.itemSlaughterModule.getDropMode(stack);
            experienceMode = ModItems.itemSlaughterModule.getExperienceMode(stack);
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

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            super.updateModePacket(packet);
            packet.addByte(dropMode);
            packet.addByte(experienceMode);
        }

        @Nonnull
        @Override
        public ItemStack handleModeDelegate(@Nonnull ItemStack stack, @Nonnull PacketBase packet) {
            ModItems.itemSlaughterModule.setDropMode(stack, packet.getByte());
            ModItems.itemSlaughterModule.setExperienceMode(stack, packet.getByte());
            return stack;
        }

        public int getExperienceMode() {
            return Math.max(experienceMode, ModConfig.vaporizers.modules.slaughter.collectExperienceMinimum);
        }

        @Override
        public int getDropMode() {
            return Math.max(dropMode, ModConfig.vaporizers.modules.slaughter.collectDropsMinimum);
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
            EntityLivingBase living = (EntityLivingBase) entity;

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

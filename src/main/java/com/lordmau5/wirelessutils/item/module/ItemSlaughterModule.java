package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemSlaughterModule extends ItemModule {

    public ItemSlaughterModule() {
        super();
        setName("slaughter_module");
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new SlaughterBehavior(vaporizer);
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
    }

    public static class SlaughterBehavior implements TileBaseVaporizer.IVaporizerBehavior {
        public final TileBaseVaporizer vaporizer;

        public SlaughterBehavior(TileBaseVaporizer vaporizer) {
            this.vaporizer = vaporizer;
        }

        public void updateModifier(@Nonnull ItemStack stack) {

        }

        public boolean canInvert() {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            return EntityLivingBase.class;
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

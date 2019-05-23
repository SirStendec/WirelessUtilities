package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
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
            return true;
        }

        @Override
        public boolean isDamageAbsolute() {
            return true;
        }
    }

    public static class SlaughterBehavior implements TileBaseVaporizer.IVaporizerBehavior {
        public final TileBaseVaporizer vaporizer;

        public SlaughterBehavior(TileBaseVaporizer vaporizer) {
            this.vaporizer = vaporizer;
        }

        public void updateModifier(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {

        }

        public boolean canInvert(@Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            return EntityLivingBase.class;
        }

        public boolean isInputUnlocked(int slot, @Nonnull TileBaseVaporizer vaporizer) {
            return slot == 0;
        }

        public boolean isModifierUnlocked(@Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
            return true;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        public boolean process(@Nonnull Entity entity, @Nonnull TileBaseVaporizer vaporizer) {
            if ( !(entity instanceof EntityLivingBase) )
                return false;

            EntityLivingBase living = (EntityLivingBase) entity;
            if ( !living.attackable() || !living.isEntityAlive() )
                return false;

            TileBaseVaporizer.WUVaporizerPlayer player = vaporizer.getFakePlayer(living.world);
            if ( player == null )
                return false;

            // Race condition? It crashed stuff, anyways.
            ItemStack weapon = vaporizer.getInput().getStackInSlot(0);
            player.setHeldItem(EnumHand.MAIN_HAND, weapon);

            boolean success = living.attackEntityFrom(new VaporizerDamage(player, vaporizer), living.getHealth());
            if ( success && !vaporizer.isCreative() ) {
                weapon.damageItem(1, player);
                vaporizer.markChunkDirty();
            }

            return success;
        }

        public void postProcess(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull AxisAlignedBB box, @Nonnull World world, @Nonnull TileBaseVaporizer vaporizer) {

        }
    }
}

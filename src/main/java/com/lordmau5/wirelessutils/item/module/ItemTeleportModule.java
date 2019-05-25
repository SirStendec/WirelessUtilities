package com.lordmau5.wirelessutils.item.module;

import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.TeleportUtils;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTeleportModule extends ItemModule {

    public ItemTeleportModule() {
        super();
        setName("teleport_module");
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new TeleportBehavior(vaporizer);
    }

    public static class TeleportBehavior implements TileBaseVaporizer.IVaporizerBehavior {

        public final TileBaseVaporizer vaporizer;
        public final ItemStack GHOST;
        public BlockPosDimension target;

        public TeleportBehavior(@Nonnull TileBaseVaporizer vaporizer) {
            GHOST = new ItemStack(ModItems.itemAbsolutePositionalCard);

            this.vaporizer = vaporizer;
            updateModifier(vaporizer.getModifier());
        }

        public void updateModule(@Nonnull ItemStack stack) {

        }

        public void updateModifier(@Nonnull ItemStack stack) {
            if ( isValidModifier(stack) ) {
                ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
                target = card.getTarget(stack, vaporizer.getPosition());

            } else {
                BlockPosDimension pos = vaporizer.getPosition();
                if ( pos == null )
                    target = null;
                else
                    target = pos.offset(vaporizer.getEnumFacing().getOpposite(), 1);
            }
        }

        @Nullable
        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return null;
        }

        public boolean canInvert() {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            if ( ModConfig.vaporizers.modules.teleport.livingOnly )
                return EntityLivingBase.class;

            return Entity.class;
        }

        @Nullable
        public Predicate<? super Entity> getEntityFilter() {
            return null;
        }

        public boolean isInputUnlocked(int slot) {
            return false;
        }

        public boolean isValidInput(@Nonnull ItemStack stack) {
            return false;
        }

        public boolean isModifierUnlocked() {
            return true;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return GHOST;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            Item item = stack.getItem();
            if ( !(item instanceof ItemBasePositionalCard) )
                return false;

            return ((ItemBasePositionalCard) item).isCardConfigured(stack);
        }

        public boolean wantsFluid() {
            return false;
        }

        public int getExperienceMode() {
            return 0;
        }

        public int getDropMode() {
            return 0;
        }

        public boolean canRun() {
            return true;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget vaporizerTarget) {
            World world = entity.world;
            if ( world == null || entity.isDead || entity.timeUntilPortal > 0 )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            if ( entity instanceof EntityPlayer && (!ModConfig.vaporizers.modules.teleport.targetPlayers || (entity.isSneaking() && ModConfig.vaporizers.modules.teleport.ignoreSneaking)) )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            Entity newEntity = TeleportUtils.teleportEntity(entity, target.getDimension(), target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            if ( newEntity == null )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            newEntity.timeUntilPortal = newEntity.getPortalCooldown();
            newEntity.fallDistance = 0;
            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}

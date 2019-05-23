package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.TeleportUtils;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
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

        public boolean canInvert() {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            return Entity.class;
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

        public void updateModifier(@Nonnull ItemStack stack) {
            if ( isValidModifier(stack) ) {
                ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
                target = card.getTarget(stack, vaporizer.getPosition());

            } else
                target = vaporizer.getPosition().offset(vaporizer.getEnumFacing().getOpposite(), 1);
        }

        public boolean canRun() {
            return true;
        }

        @Nonnull
        public IWorkProvider.WorkResult process(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget vaporizerTarget) {
            World world = entity.world;
            if ( world == null || entity.isDead )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            // TODO: Cooldown for teleportation.

            Entity newEntity = TeleportUtils.teleportEntity(entity, target.getDimension(), target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            if ( newEntity == null )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            newEntity.fallDistance = 0;
            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        public void postProcess(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull AxisAlignedBB box, @Nonnull World world) {

        }
    }
}

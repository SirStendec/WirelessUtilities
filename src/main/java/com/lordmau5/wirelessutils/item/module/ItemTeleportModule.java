package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
        public BlockPosDimension target;

        public TeleportBehavior(@Nonnull TileBaseVaporizer vaporizer) {
            this.vaporizer = vaporizer;
            updateModifier(vaporizer.getModifier(), vaporizer);
        }

        public boolean canInvert(@Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            return Entity.class;
        }

        public boolean isInputUnlocked(int slot, @Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
            return false;
        }

        public boolean isModifierUnlocked(@Nonnull TileBaseVaporizer vaporizer) {
            return true;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
            Item item = stack.getItem();
            if ( !(item instanceof ItemBasePositionalCard) )
                return false;

            return ((ItemBasePositionalCard) item).isCardConfigured(stack);
        }

        public void updateModifier(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
            if ( isValidModifier(stack, vaporizer) ) {
                ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
                target = card.getTarget(stack, vaporizer.getPosition());

            } else
                target = vaporizer.getPosition().offset(vaporizer.getEnumFacing().getOpposite(), 1);
        }

        public boolean process(@Nonnull Entity entity, @Nonnull TileBaseVaporizer vaporizer) {
            World world = entity.world;
            if ( world == null || entity.isDead )
                return false;

            NBTTagCompound tag = entity.getEntityData();
            int now = entity.ticksExisted;
            int then = tag.getInteger("WUTeleport");
            if ( now - then < 20 )
                return false;

            // TODO: Add interdimensional teleportation.
            if ( target.getDimension() != world.provider.getDimension() )
                return false;

            // TODO: Facing direction?
            entity.setPositionAndUpdate(target.getX(), target.getY(), target.getZ());
            entity.fallDistance = 0;

            tag.setInteger("WUTeleport", now);
            return true;
        }

        public void postProcess(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull AxisAlignedBB box, @Nonnull World world, @Nonnull TileBaseVaporizer vaporizer) {

        }
    }
}

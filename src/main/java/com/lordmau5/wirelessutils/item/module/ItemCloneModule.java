package com.lordmau5.wirelessutils.item.module;

import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCloneModule extends ItemModule {

    public ItemCloneModule() {
        super();
        setName("clone_module");
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new CloneBehavior(vaporizer, stack);
    }

    public static class CloneBehavior implements TileBaseVaporizer.IVaporizerBehavior {

        public final TileBaseVaporizer vaporizer;
        public final ItemStack ghost;

        public CloneBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            this.vaporizer = vaporizer;
            ghost = new ItemStack(ModItems.itemVoidPearl);
            updateModule(module);
        }

        public boolean canInvert() {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            return null;
        }

        public Predicate<? super Entity> getEntityFilter() {
            return null;
        }

        public void updateModule(@Nonnull ItemStack stack) {

        }

        @Nullable
        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return null;
        }

        public boolean isInputUnlocked(int slot) {
            return true;
        }

        public boolean isValidInput(@Nonnull ItemStack stack) {
            return EntityUtilities.isFilledEntityBall(stack);
        }

        public boolean isModifierUnlocked() {
            return true;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return EntityUtilities.isFilledEntityBall(stack);
        }

        public void updateModifier(@Nonnull ItemStack stack) {

        }

        public int getExperienceMode() {
            return 0;
        }

        public int getDropMode() {
            return 0;
        }

        public boolean wantsFluid() {
            return true;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return ghost;
        }

        public boolean canRun() {
            return isValidModifier(vaporizer.getModifier());
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            ItemStack modifier = vaporizer.getModifier();
            int cost = EntityUtilities.getSpawnCost(modifier, world);
            if ( cost == -1 )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            if ( !vaporizer.removeFuel(cost) )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            Entity entity = EntityUtilities.getEntity(modifier, world, true);
            if ( entity == null ) {
                vaporizer.addFuel(cost);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            entity.setPosition(target.pos.getX() + 0.5, target.pos.getY() + 0.5, target.pos.getZ() + 0.5);
            world.spawnEntity(entity);

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }
    }
}

package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.item.pearl.ItemVoidPearl;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.ItemHandlerProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCaptureModule extends ItemModule {

    public ItemCaptureModule() {
        super();
        setName("capture_module");
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new CaptureBehavior(vaporizer);
    }

    public static boolean isEntityBall(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return true;

        return false;
    }

    public static boolean isFilledEntityBall(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl ) {
            ItemVoidPearl pearl = (ItemVoidPearl) item;
            return pearl.containsEntity(stack);
        }

        return false;
    }

    @Nonnull
    public static ItemStack captureEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
        Item item = stack.getItem();
        if ( item instanceof ItemVoidPearl )
            return ((ItemVoidPearl) item).captureEntity(stack, entity);

        return ItemStack.EMPTY;
    }

    public static class CaptureBehavior implements TileBaseVaporizer.IVaporizerBehavior {

        public final TileBaseVaporizer vaporizer;

        public CaptureBehavior(TileBaseVaporizer vaporizer) {
            this.vaporizer = vaporizer;
        }

        public boolean canInvert() {
            return false;
        }

        public Class<? extends Entity> getEntityClass() {
            return EntityLiving.class;
        }

        public boolean isInputUnlocked(int slot) {
            return true;
        }

        public boolean isValidInput(@Nonnull ItemStack stack) {
            return isEntityBall(stack) && !isFilledEntityBall(stack);
        }

        public boolean isModifierUnlocked() {
            return false;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return false;
        }

        public void updateModifier(@Nonnull ItemStack stack) {

        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return ItemStack.EMPTY;
        }

        public boolean canRun() {
            return vaporizer.hasInput() && vaporizer.hasEmptyOutput();
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            if ( !vaporizer.hasInput() || !vaporizer.hasEmptyOutput() )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            ItemStack stack = null;
            ItemHandlerProxy input = vaporizer.getInput();
            int slot = 0;
            for (int i = 0; i < input.getSlots(); i++) {
                ItemStack slotted = input.extractItem(i, 1, true);
                if ( isValidInput(slotted) ) {
                    stack = slotted;
                    slot = i;
                    break;
                }
            }

            ItemStack result = captureEntity(stack, entity);
            if ( result.isEmpty() )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            input.extractItem(slot, 1, false);
            vaporizer.insertOutputStack(result);

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}

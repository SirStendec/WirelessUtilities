package com.lordmau5.wirelessutils.item.module;

import com.lordmau5.wirelessutils.gui.client.modules.ElementCaptureModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.ItemHandlerProxy;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCaptureModule extends ItemFilteringModule {

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
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.capture.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.capture.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.capture.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.capture.energyDrain;
    }

    @Nullable
    @Override
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new CaptureBehavior(vaporizer, stack);
    }

    public static class CaptureBehavior extends FilteredBehavior {

        private ItemStack ghost;

        public CaptureBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            super(vaporizer);

            ghost = new ItemStack(ModItems.itemVoidPearl);

            allowPlayers = false;
            allowCreative = false;

            requireAttackable = true;
            requireAlive = true;

            updateModule(module);
        }

        public double getEnergyMultiplier() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 1;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyMultiplier(stack, vaporizer);
        }

        public int getEnergyAddition() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 0;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyAddition(stack, vaporizer);
        }

        public int getEnergyDrain() {
            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return 0;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEneryDrain(stack, vaporizer);
        }

        public boolean canInvert() {
            return false;
        }

        @Override
        public Class<? extends Entity> getEntityClass() {
            return EntityLiving.class;
        }

        @Nullable
        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementCaptureModule(gui, this);
        }

        public boolean isInputUnlocked(int slot) {
            return true;
        }

        public boolean isValidInput(@Nonnull ItemStack stack) {
            return EntityUtilities.isEntityBall(stack) && !EntityUtilities.isFilledEntityBall(stack);
        }

        @Nonnull
        @Override
        public ItemStack getInputGhost(int slot) {
            return ghost;
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
            return vaporizer.hasInput() && vaporizer.hasEmptyOutput();
        }

        public int getEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return 0;
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

            ItemStack result = EntityUtilities.saveEntity(stack, entity);
            if ( result.isEmpty() )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            entity.setDead();
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

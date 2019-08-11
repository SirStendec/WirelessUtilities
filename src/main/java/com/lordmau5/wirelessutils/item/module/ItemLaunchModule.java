package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.ElementLaunchModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemLaunchModule extends ItemFilteringModule {

    public ItemLaunchModule() {
        super();
        setName("launch_module");
    }

    protected int getDefaultPlayerMode() {
        return 0;
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    private float getSpeed(@Nonnull ItemStack stack, String key) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey(key, Constants.NBT.TAG_FLOAT) )
                return tag.getFloat(key);
        }

        return 0;
    }

    @Nonnull
    private ItemStack setSpeed(@Nonnull ItemStack stack, String key, float value) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( value == 0 )
            tag.removeTag(key);
        else
            tag.setFloat(key, value);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public float getXSpeed(@Nonnull ItemStack stack) {
        return getSpeed(stack, "SpeedX");
    }

    @Nonnull
    public ItemStack setXSpeed(@Nonnull ItemStack stack, float speed) {
        return setSpeed(stack, "SpeedX", speed);
    }

    public float getYSpeed(@Nonnull ItemStack stack) {
        return getSpeed(stack, "SpeedY");
    }

    @Nonnull
    public ItemStack setYSpeed(@Nonnull ItemStack stack, float speed) {
        return setSpeed(stack, "SpeedY", speed);
    }

    public float getZSpeed(@Nonnull ItemStack stack) {
        return getSpeed(stack, "SpeedZ");
    }

    @Nonnull
    public ItemStack setZSpeed(@Nonnull ItemStack stack, float speed) {
        return setSpeed(stack, "SpeedZ", speed);
    }


    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new LaunchBehavior(vaporizer, stack);
    }

    public static class LaunchBehavior extends FilteredBehavior {

        private float speedX;
        private float speedY;
        private float speedZ;

        private boolean canRun;

        public LaunchBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack stack) {
            super(vaporizer);

            allowPlayers = true;
            allowCreative = true;
            allowBosses = true;

            updateModule(stack);
        }

        public float getSpeedX() {
            return speedX;
        }

        public float getSpeedY() {
            return speedY;
        }

        public float getSpeedZ() {
            return speedZ;
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            super.updateModePacket(packet);
            packet.addFloat(speedX);
            packet.addFloat(speedY);
            packet.addFloat(speedZ);
        }

        @Nonnull
        @Override
        public ItemStack handleModeDelegate(@Nonnull ItemStack stack, @Nonnull PacketBase packet) {
            super.handleModeDelegate(stack, packet);

            ModItems.itemLaunchModule.setXSpeed(stack, packet.getFloat());
            ModItems.itemLaunchModule.setYSpeed(stack, packet.getFloat());
            ModItems.itemLaunchModule.setZSpeed(stack, packet.getFloat());

            return stack;
        }

        @Nullable
        @Override
        public Class<? extends Entity> getEntityClass() {
            return Entity.class;
        }

        @Override
        public void updateModule(@Nonnull ItemStack stack) {
            super.updateModule(stack);

            speedX = ModItems.itemLaunchModule.getXSpeed(stack);
            speedY = ModItems.itemLaunchModule.getYSpeed(stack);
            speedZ = ModItems.itemLaunchModule.getZSpeed(stack);

            canRun = speedX != 0 || speedY != 0 || speedZ != 0;
        }

        public boolean isModifierUnlocked() {
            return false;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return false;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            return ItemStack.EMPTY;
        }

        public void updateModifier(@Nonnull ItemStack stack) {

        }

        public boolean isInputUnlocked(int slot) {
            return false;
        }

        public boolean isValidInput(@Nonnull ItemStack stack) {
            return false;
        }

        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementLaunchModule(gui, this);
        }

        public boolean wantsFluid() {
            return false;
        }

        @Override
        public boolean canRun(boolean ignorePower) {
            if ( !super.canRun(ignorePower) )
                return false;

            return canRun;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( !canRun )
                return "info." + WirelessUtils.MODID + ".vaporizer.bad_config";

            return super.getUnconfiguredExplanation();
        }

        public boolean canInvert() {
            return false;
        }

        public int getExperienceMode() {
            return 0;
        }

        public int getDropMode() {
            return 0;
        }

        public int getBlockEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return 0;
        }

        public int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getActionCost() {
            return 0;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {

            double targetX = entity.motionX;
            double targetY = entity.motionY;
            double targetZ = entity.motionZ;

            if ( speedX > 0 ? targetX < speedX : targetX > speedX )
                targetX += speedX;

            if ( speedY > 0 ? targetY < speedY : targetY > speedY )
                targetY += speedY;

            if ( speedZ > 0 ? targetZ < speedZ : targetZ > speedZ )
                targetZ += speedZ;

            entity.motionX = targetX;
            entity.motionY = targetY;
            entity.motionZ = targetZ;
            entity.velocityChanged = true;

            if ( entity.world != null ) {
                NBTTagCompound data = entity.getEntityData();
                data.setBoolean("WUFallProtect", true);

                entity.world.updateEntityWithOptionalForce(entity, false);
            }

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }
    }
}

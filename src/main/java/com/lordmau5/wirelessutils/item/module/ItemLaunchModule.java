package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.ElementLaunchModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemLaunchModule extends ItemFilteringModule {

    public ItemLaunchModule() {
        super();
        setName("launch_module");
    }

    protected int getDefaultPlayerMode() {
        return 0;
    }

    @Override
    public boolean allowPlayerMode() {
        return ModConfig.vaporizers.modules.launch.targetPlayers;
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.launch.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.launch.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.launch.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.launch.energyDrain;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        float speedX = getXSpeed(stack);
        float speedY = getYSpeed(stack);
        float speedZ = getZSpeed(stack);

        tooltip.add(new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".launch_module.speed",
                new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".blockpos.basic",
                        String.format("%.2f", speedX),
                        String.format("%.2f", speedY),
                        String.format("%.2f", speedZ)
                )
        ).getFormattedText());

        if ( ModConfig.vaporizers.modules.launch.allowFallProtect && getFallProtect(stack) )
            tooltip.add(new TextComponentTranslation("item." + WirelessUtils.MODID + ".launch_module.fall_protect").setStyle(TextHelpers.GREEN).getFormattedText());
    }

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("SpeedX") || tag.hasKey("SpeedY") || tag.hasKey("SpeedZ") || tag.hasKey("FallProtect") || super.isConfigured(stack);
    }


    public boolean getFallProtect(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("FallProtect");
        }

        return false;
    }

    @Nonnull
    public ItemStack setFallProtect(@Nonnull ItemStack stack, boolean enabled) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( enabled )
            tag.setBoolean("FallProtect", true);
        else
            tag.removeTag("FallProtect");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
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

        private float speedXsq;
        private float speedYsq;
        private float speedZsq;

        private boolean canRun;
        private boolean fallProtect = false;

        public LaunchBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack stack) {
            super(vaporizer);

            allowPlayers = ModConfig.vaporizers.modules.launch.targetPlayers;
            allowCreative = true;
            allowBosses = ModConfig.vaporizers.modules.launch.targetBosses;

            requireAttackable = false;
            obeyItemTags = true;

            updateModule(stack);
        }

        public boolean getFallProtect() {
            return fallProtect;
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
            packet.addBool(fallProtect);
        }

        @Nonnull
        @Override
        public ItemStack handleModeDelegate(@Nonnull ItemStack stack, @Nonnull PacketBase packet) {
            super.handleModeDelegate(stack, packet);

            ModItems.itemLaunchModule.setXSpeed(stack, packet.getFloat());
            ModItems.itemLaunchModule.setYSpeed(stack, packet.getFloat());
            ModItems.itemLaunchModule.setZSpeed(stack, packet.getFloat());
            ModItems.itemLaunchModule.setFallProtect(stack, packet.getBool());

            return stack;
        }

        @Nullable
        @Override
        public Class<? extends Entity> getEntityClass() {
            if ( ModConfig.vaporizers.modules.launch.livingOnly )
                return EntityLivingBase.class;

            return Entity.class;
        }

        @Override
        public void updateModule(@Nonnull ItemStack stack) {
            super.updateModule(stack);

            speedX = ModItems.itemLaunchModule.getXSpeed(stack);
            speedY = ModItems.itemLaunchModule.getYSpeed(stack);
            speedZ = ModItems.itemLaunchModule.getZSpeed(stack);

            speedXsq = speedX * speedX;
            speedYsq = speedY * speedY;
            speedZsq = speedZ * speedZ;

            fallProtect = ModItems.itemLaunchModule.getFallProtect(stack);

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

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
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

            if ( !canRun )
                return false;

            if ( ignorePower )
                return true;

            // Rudimentary cost calculation since we don't know what entities we'll be
            // working on.
            int cost = ModConfig.vaporizers.modules.launch.energy;
            if ( fallProtect && ModConfig.vaporizers.modules.launch.allowFallProtect )
                cost += ModConfig.vaporizers.modules.launch.fallProtectEnergy;

            return vaporizer.getEnergyStored() >= cost;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( !canRun )
                return "info." + WirelessUtils.MODID + ".vaporizer.bad_config";

            return super.getUnconfiguredExplanation();
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
            int cost = ModConfig.vaporizers.modules.launch.energy;
            if ( fallProtect && ModConfig.vaporizers.modules.launch.allowFallProtect )
                cost += ModConfig.vaporizers.modules.launch.fallProtectEnergy;

            if ( ModConfig.vaporizers.modules.launch.energyPerUnit == 0 && ModConfig.vaporizers.modules.launch.energyPerUnitSquared == 0 )
                return cost;

            float velocity = 0;
            double targetX = entity.motionX;
            double targetY = entity.motionY;
            double targetZ = entity.motionZ;

            if ( speedX > 0 ? targetX < speedX : targetX > speedX )
                velocity += speedXsq;

            if ( speedY > 0 ? targetY < speedY : targetY > speedY )
                velocity += speedYsq;

            if ( speedZ > 0 ? targetZ < speedZ : targetZ > speedZ )
                velocity += speedZsq;

            return cost +
                    (int) Math.ceil(ModConfig.vaporizers.modules.launch.energyPerUnit * Math.sqrt(velocity)) +
                    (int) Math.ceil(ModConfig.vaporizers.modules.launch.energyPerUnitSquared * velocity);
        }

        public int getMaxEntityEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target) {
            int cost = ModConfig.vaporizers.modules.launch.energy;
            if ( fallProtect && ModConfig.vaporizers.modules.launch.allowFallProtect )
                cost += ModConfig.vaporizers.modules.launch.fallProtectEnergy;

            if ( ModConfig.vaporizers.modules.launch.energyPerUnit == 0 && ModConfig.vaporizers.modules.launch.energyPerUnitSquared == 0 )
                return cost;

            return cost +
                    (int) Math.ceil(ModConfig.vaporizers.modules.launch.energyPerUnit * Math.sqrt(speedXsq + speedYsq + speedZsq)) +
                    (int) Math.ceil(ModConfig.vaporizers.modules.launch.energyPerUnitSquared * (speedXsq + speedYsq + speedZsq));
        }

        public int getActionCost() {
            int budget = ModConfig.vaporizers.modules.launch.budget;
            if ( fallProtect && ModConfig.vaporizers.modules.launch.allowFallProtect )
                budget += ModConfig.vaporizers.modules.launch.fallProtectBudget;

            return budget;
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

            boolean changed = false;

            if ( speedX > 0 ? targetX < speedX : targetX > speedX ) {
                targetX += speedX;
                changed = true;
            }

            if ( speedY > 0 ? targetY < speedY : targetY > speedY ) {
                targetY += speedY;
                changed = true;
            }

            if ( speedZ > 0 ? targetZ < speedZ : targetZ > speedZ ) {
                targetZ += speedZ;
                changed = true;
            }

            if ( !changed )
                return IWorkProvider.WorkResult.FAILURE_CONTINUE;

            entity.motionX = targetX;
            entity.motionY = targetY;
            entity.motionZ = targetZ;
            entity.velocityChanged = true;

            if ( fallProtect && ModConfig.vaporizers.modules.launch.allowFallProtect ) {
                NBTTagCompound data = entity.getEntityData();
                data.setBoolean("WUFallProtect", true);
            }

            if ( entity.world != null )
                entity.world.updateEntityWithOptionalForce(entity, false);

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }
    }
}

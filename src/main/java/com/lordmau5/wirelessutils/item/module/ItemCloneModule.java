package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.ElementCloneModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.clone.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.clone.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.clone.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.clone.energyDrain;
    }

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return super.isConfigured(stack);

        return tag.hasKey("ExactCopies") || super.isConfigured(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if ( getExactCopies(stack) )
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".clone_module.exact"));
    }

    public boolean getExactCopies(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("ExactCopies");
        }

        return false;
    }

    @Nonnull
    public ItemStack setExactCopies(@Nonnull ItemStack stack, boolean enabled) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( enabled )
            tag.setBoolean("ExactCopies", true);
        else
            tag.removeTag("ExactCopies");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    @Override
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new CloneBehavior(vaporizer, stack);
    }

    public static class CloneBehavior implements TileBaseVaporizer.IVaporizerBehavior {

        public final TileBaseVaporizer vaporizer;
        private final ItemStack ghost;

        private boolean exactCopies = false;

        private ItemStack entityBall = ItemStack.EMPTY;
        private boolean entityLoaded = false;
        private float entityHealth = 0;
        private int entityCost = 0;
        private int finalCost = 0;

        public CloneBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            this.vaporizer = vaporizer;
            ghost = new ItemStack(ModItems.itemVoidPearl);
            updateModule(module);
            updateModifier(vaporizer.getModifier());
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

        public boolean isExact() {
            return exactCopies;
        }

        public boolean canExact() {
            return ModConfig.vaporizers.modules.clone.exactFactor > 0;
        }

        public Class<? extends Entity> getEntityClass() {
            return null;
        }

        public Predicate<? super Entity> getEntityFilter() {
            return null;
        }

        public void updateModule(@Nonnull ItemStack stack) {
            exactCopies = canExact() && ModItems.itemCloneModule.getExactCopies(stack);
            recalculateCost();
        }

        @Nullable
        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementCloneModule(gui, this);
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            packet.addBool(exactCopies);
        }

        @Override
        public void handleModePacket(@Nonnull PacketBase packet) {
            ItemStack module = vaporizer.getModule();
            ModItems.itemCloneModule.setExactCopies(module, packet.getBool());
            vaporizer.setModule(module);
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
            entityBall = stack;
            entityLoaded = false;

            World world = vaporizer.getWorld();
            // No, world isn't always non-null you idiot.
            if ( world != null ) {
                Entity entity = EntityUtilities.getEntity(stack, world, exactCopies);
                if ( entity != null ) {
                    entityLoaded = true;
                    entityCost = EntityUtilities.getBaseExperience(entity);
                    if ( entity instanceof EntityLivingBase )
                        entityHealth = ((EntityLivingBase) entity).getMaxHealth();
                    else
                        entityHealth = 0;
                }
            }

            recalculateCost();
        }

        public void recalculateCost() {
            if ( !entityLoaded ) {
                finalCost = 0;
                return;
            }

            double rawCost = (ModConfig.vaporizers.modules.clone.expFactor * entityCost) + (ModConfig.vaporizers.modules.clone.healthFactor * entityHealth);
            if ( exactCopies )
                rawCost *= ModConfig.vaporizers.modules.clone.exactFactor;

            finalCost = (int) Math.ceil(rawCost);
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
            if ( !entityLoaded && !entityBall.isEmpty() )
                updateModifier(entityBall);

            return finalCost > 0;
        }

        public int getEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            // TODO: Energy cost to clone mobs.
            return 0;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            ItemStack modifier = vaporizer.getModifier();
            if ( finalCost == 0 )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            int removed = vaporizer.removeFuel(finalCost);
            if ( removed < finalCost ) {
                vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            Entity entity = EntityUtilities.getEntity(modifier, world, exactCopies);
            if ( entity == null ) {
                vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            entity.setPosition(target.pos.getX() + 0.5, target.pos.getY() + 0.5, target.pos.getZ() + 0.5);
            world.spawnEntity(entity);

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }
    }
}

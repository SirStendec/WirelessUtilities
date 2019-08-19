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
import net.minecraft.util.math.AxisAlignedBB;
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

        return tag.hasKey("ExactCopies") || tag.hasKey("EntityLimit") || super.isConfigured(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int count = getEntityLimit(stack);
        if ( count < ModConfig.vaporizers.modules.clone.maxCount )
            tooltip.add(StringHelper.localizeFormat("item." + WirelessUtils.MODID + ".clone_module.limit", count));

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

    public int getEntityLimit(@Nonnull ItemStack stack) {
        int out = 0;
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("EntityLimit") )
                out = tag.getInteger("EntityLimit");
        }

        if ( out == 0 || out > ModConfig.vaporizers.modules.clone.maxCount )
            return ModConfig.vaporizers.modules.clone.maxCount;

        return out;
    }

    @Nonnull
    public ItemStack setEntityLimit(@Nonnull ItemStack stack, int count) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( count == 0 || count >= ModConfig.vaporizers.modules.clone.maxCount )
            tag.removeTag("EntityLimit");
        else
            tag.setInteger("EntityLimit", count);

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
        private boolean entityBaby = false;
        private float entityHealth = 0;
        private int entityCost = 0;
        private int finalCost = 0;
        private int entityLimit = 0;

        private int entities = 0;

        public CloneBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            this.vaporizer = vaporizer;
            ghost = new ItemStack(ModItems.itemVoidPearl);
            updateModule(module);
            updateModifier(vaporizer.getModifier());
        }

        public boolean hasEntity() {
            return entityLoaded;
        }

        public int getCost() {
            return finalCost;
        }

        public double getEnergyMultiplier() {
            double exactMultiplier = exactCopies ? ModConfig.vaporizers.modules.clone.exactEnergyMultiplier : 1D;

            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return exactMultiplier;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyMultiplier(stack, vaporizer) * exactMultiplier;
        }

        public int getEnergyAddition() {
            int energyAddition = exactCopies ? ModConfig.vaporizers.modules.clone.exactEnergyAddition : 0;

            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return energyAddition;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyAddition(stack, vaporizer) + energyAddition;
        }

        public int getEnergyDrain() {
            int exactDrain = exactCopies ? ModConfig.vaporizers.modules.clone.exactEnergyDrain : 0;

            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return exactDrain;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEneryDrain(stack, vaporizer) + exactDrain;
        }

        public boolean canInvert() {
            return false;
        }

        public int getEntityLimit() {
            return entityLimit;
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
            entityLimit = ModItems.itemCloneModule.getEntityLimit(stack);
            recalculateCost();
        }

        @Nullable
        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementCloneModule(gui, this);
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            packet.addBool(exactCopies);
            packet.addInt(entityLimit);
        }

        @Override
        public void handleModePacket(@Nonnull PacketBase packet) {
            ItemStack module = vaporizer.getModule();
            ModItems.itemCloneModule.setExactCopies(module, packet.getBool());
            ModItems.itemCloneModule.setEntityLimit(module, packet.getInt());
            vaporizer.setModule(module);
        }

        public boolean isInputUnlocked(int slot) {
            return ModConfig.vaporizers.useEntitiesFuel;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
            if ( !EntityUtilities.isFilledEntityBall(stack) )
                return false;

            int value = EntityUtilities.getBaseExperience(stack, vaporizer.getWorld());
            if ( EntityUtilities.containsBabyEntity(stack) )
                value = (int) Math.floor(value * ModConfig.vaporizers.babyMultiplier);

            return value > 0;
        }

        public boolean isModifierUnlocked() {
            return true;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            return isValidInput(stack, 0);
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
                    entityBaby = EntityUtilities.containsBabyEntity(stack);

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
            if ( exactCopies ) {
                rawCost *= ModConfig.vaporizers.modules.clone.exactFactor;
                if ( entityBaby && ModConfig.vaporizers.modules.clone.exactBaby )
                    rawCost *= ModConfig.vaporizers.babyMultiplier;
            }

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

        public boolean canRun(boolean ignorePower) {
            this.entities = 0;
            if ( !entityLoaded && !entityBall.isEmpty() )
                updateModifier(entityBall);

            if ( finalCost == 0 )
                return false;

            // If we operate on entities in a single continuous
            // block, then do our entity check here.
            if ( !ignorePower && vaporizer.hasWorld() && vaporizer.canGetFullEntities() ) {
                Class<? extends Entity> klass = EntityUtilities.getEntityClass(vaporizer.getModifier());
                if ( klass == null )
                    klass = EntityLivingBase.class;

                List<Entity> entities = vaporizer.getWorld().getEntitiesWithinAABB(klass, vaporizer.getFullEntitiesAABB().grow(ModConfig.vaporizers.modules.clone.maxRange));
                this.entities = entities.size();

                if ( this.entities >= entityLimit )
                    return false;
            }

            return true;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( entityBall.isEmpty() )
                return "info." + WirelessUtils.MODID + ".vaporizer.missing_entity";

            if ( finalCost == 0 )
                return "info." + WirelessUtils.MODID + ".vaporizer.cannot_spawn";

            return null;
        }

        public int getBlockEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            if ( exactCopies )
                return ModConfig.vaporizers.modules.clone.entityExactEnergy;

            return ModConfig.vaporizers.modules.clone.entityEnergy;
        }

        public int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getMaxEntityEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getActionCost() {
            if ( exactCopies )
                return ModConfig.vaporizers.modules.clone.budgetExact;

            return ModConfig.vaporizers.modules.clone.budget;
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
                if ( removed > 0 )
                    vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            // Check for too many entities.
            if ( !vaporizer.canGetFullEntities() ) {
                Class<? extends Entity> klass = EntityUtilities.getEntityClass(modifier);
                if ( klass == null ) {
                    vaporizer.addFuel(removed);
                    return IWorkProvider.WorkResult.FAILURE_STOP;
                }

                List<Entity> existing = world.getEntitiesWithinAABB(klass, new AxisAlignedBB(target.pos).grow(ModConfig.vaporizers.modules.clone.maxRange));
                if ( existing.size() >= entityLimit ) {
                    vaporizer.addFuel(removed);
                    return IWorkProvider.WorkResult.FAILURE_STOP;
                }
            }

            // Now get the entity...
            Entity entity = EntityUtilities.getEntity(modifier, world, exactCopies);
            if ( entity == null ) {
                vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            // ... and spawn it!
            double offsetX = 0.5D;
            double offsetY = 0.5D;
            double offsetZ = 0.5D;

            if ( ModConfig.vaporizers.modules.clone.randomSpawn ) {
                offsetX = world.rand.nextDouble();
                offsetY = world.rand.nextDouble();
                offsetZ = world.rand.nextDouble();
            }

            entity.setPositionAndRotation(
                    target.pos.getX() + offsetX,
                    target.pos.getY() + offsetY,
                    target.pos.getZ() + offsetZ,
                    360.0F * world.rand.nextFloat(),
                    entity.rotationPitch
            );

            world.spawnEntity(entity);

            if ( removed > finalCost )
                vaporizer.addFuel(removed - finalCost);

            if ( vaporizer.canGetFullEntities() ) {
                entities++;
                if ( entities >= entityLimit )
                    return IWorkProvider.WorkResult.SUCCESS_STOP;
            }

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }
    }
}

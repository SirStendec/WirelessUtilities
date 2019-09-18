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
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        private final static ItemStack CRYSTAL_GHOST = new ItemStack(ModItems.itemCrystallizedVoidPearl);

        public final TileBaseVaporizer vaporizer;
        private final ItemStack[] ghosts;

        private boolean exactCopies = false;

        private ItemStack entityBall = ItemStack.EMPTY;
        private boolean canSpawn = false;
        private boolean entityLoaded = false;
        private boolean entityBaby = false;
        private boolean entityBoss = false;
        private float entityHealth = 0;
        private int entityCost = 0;

        private int budgetCost = 0;
        private int fuelCost = 0;
        private int energyCost = 0;
        private int entityLimit = 0;

        private int entities = 0;
        private IEntityLivingData entityLivingData;

        public CloneBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            this.vaporizer = vaporizer;

            Set<Item> ghostItems = EntityUtilities.getValidItems();
            ghosts = new ItemStack[ghostItems.size()];
            int i = 0;

            for (Item item : ghostItems) {
                ghosts[i] = new ItemStack(item);
                i++;
            }

            updateModule(module);
            updateModifier(vaporizer.getModifier());
        }

        public boolean hasEntity() {
            return entityLoaded;
        }

        public int getCost() {
            return fuelCost;
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
            return ModConfig.vaporizers.modules.clone.allowExact;
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

        public boolean isValidBall(@Nonnull ItemStack stack) {
            if ( !EntityUtilities.isFilledEntityBall(stack) )
                return false;

            int value = EntityUtilities.getBaseExperience(stack, vaporizer.getWorld());
            if ( EntityUtilities.containsBabyEntity(stack) )
                value = (int) Math.floor(value * ModConfig.vaporizers.babyMultiplier);

            return value > 0;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
            if ( !EntityUtilities.canEmptyBall(stack) )
                return false;

            return isValidBall(stack);
        }

        public boolean isModifierUnlocked() {
            return true;
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            if ( !EntityUtilities.isFilledEntityBall(stack) )
                return false;

            if ( ModConfig.vaporizers.modules.clone.requireCrystallizedVoidPearls && stack.getItem() != ModItems.itemCrystallizedVoidPearl )
                return false;

            World world = vaporizer.getWorld();
            if ( world == null )
                return false;

            Entity entity = EntityUtilities.getEntity(stack, world, true, null);
            if ( entity == null || !(entity instanceof EntityLivingBase) )
                return false;

            final EntityLivingBase living = (EntityLivingBase) entity;
            final boolean isBoss = !entity.isNonBoss();
            final ModConfig.BossMode bossMode = ModConfig.vaporizers.modules.clone.bossMode;
            if ( isBoss && (bossMode == ModConfig.BossMode.DISABLED || (bossMode == ModConfig.BossMode.CREATIVE_ONLY && !vaporizer.isCreative())) )
                return false;

            final boolean isBaby = EntityUtilities.isBaby(entity);
            if ( isBaby && ModConfig.vaporizers.modules.clone.babyMode == ModConfig.BabyCloningMode.DISALLOW )
                return false;

            int expCost = EntityUtilities.getBaseFromEntity(entity);
            if ( isBaby && ModConfig.vaporizers.modules.clone.exactBaby )
                expCost *= ModConfig.vaporizers.babyMultiplier;

            final float health = living.getMaxHealth();
            double rawFuel = (ModConfig.vaporizers.modules.clone.fuelPerExp * expCost) + (ModConfig.vaporizers.modules.clone.fuelPerHealth * health);
            double rawEnergy = (ModConfig.vaporizers.modules.clone.energyPerExp * expCost) + (ModConfig.vaporizers.modules.clone.energyPerHealth * health);
            if ( exactCopies ) {
                if ( ModConfig.vaporizers.modules.clone.fuelExactFactor != 0 )
                    rawFuel *= ModConfig.vaporizers.modules.clone.fuelExactFactor;

                if ( ModConfig.vaporizers.modules.clone.energyExactFactor != 0 )
                    rawEnergy *= ModConfig.vaporizers.modules.clone.energyExactFactor;

                if ( isBaby && ModConfig.vaporizers.modules.clone.exactBaby )
                    rawFuel *= ModConfig.vaporizers.babyMultiplier;
            }

            return rawFuel > 0 || (!ModConfig.vaporizers.modules.clone.requireFuel && rawEnergy > 0);
        }

        public void updateModifier(@Nonnull ItemStack stack) {
            if ( !isValidModifier(stack) ) {
                entityBall = ItemStack.EMPTY;
                entityLoaded = false;
                canSpawn = false;
                return;
            }

            entityBall = stack;
            entityLoaded = false;
            canSpawn = false;

            World world = vaporizer.getWorld();
            // No, world isn't always non-null you idiot.
            if ( world != null ) {
                Entity entity = EntityUtilities.getEntity(stack, world, true, null);
                if ( entity != null ) {
                    entityLoaded = true;
                    if ( entity instanceof EntityLivingBase ) {
                        entityCost = EntityUtilities.getBaseFromEntity(entity);
                        entityBaby = EntityUtilities.isBaby(entity);
                        entityBoss = !entity.isNonBoss();
                        entityHealth = ((EntityLivingBase) entity).getMaxHealth();
                    } else {
                        entityCost = 0;
                        entityHealth = 0;
                    }
                }
            }

            recalculateCost();
        }

        public void recalculateCost() {
            if ( !entityLoaded ) {
                fuelCost = 0;
                energyCost = 0;
                budgetCost = 0;
                canSpawn = false;
                return;
            }

            ModConfig.BossMode bossMode = ModConfig.vaporizers.modules.clone.bossMode;
            if ( entityBoss && (bossMode == ModConfig.BossMode.DISABLED || (bossMode == ModConfig.BossMode.CREATIVE_ONLY && !vaporizer.isCreative())) ) {
                fuelCost = 0;
                energyCost = 0;
                budgetCost = 0;
                canSpawn = false;
                return;
            }

            int cost = entityCost;
            if ( entityBaby && ModConfig.vaporizers.modules.clone.exactBaby )
                cost *= ModConfig.vaporizers.babyMultiplier;

            double rawFuel = (ModConfig.vaporizers.modules.clone.fuelPerExp * cost) + (ModConfig.vaporizers.modules.clone.fuelPerHealth * entityHealth);
            double rawEnergy = (ModConfig.vaporizers.modules.clone.energyPerExp * cost) + (ModConfig.vaporizers.modules.clone.energyPerHealth * entityHealth);
            double rawBudget = (ModConfig.vaporizers.modules.clone.budgetPerExp * cost) + (ModConfig.vaporizers.modules.clone.budgetPerHealth * entityHealth);

            if ( exactCopies ) {
                if ( ModConfig.vaporizers.modules.clone.fuelExactFactor != 0 )
                    rawFuel *= ModConfig.vaporizers.modules.clone.fuelExactFactor;

                if ( ModConfig.vaporizers.modules.clone.energyExactFactor != 0 )
                    rawEnergy *= ModConfig.vaporizers.modules.clone.energyExactFactor;

                if ( ModConfig.vaporizers.modules.clone.budgetExactFactor != 0 )
                    rawBudget *= ModConfig.vaporizers.modules.clone.budgetExactFactor;
            }

            fuelCost = (int) Math.ceil(rawFuel);
            energyCost = (int) Math.ceil(rawEnergy);
            budgetCost = (int) Math.ceil(rawBudget);

            canSpawn = fuelCost > 0 || (!ModConfig.vaporizers.modules.clone.requireFuel && energyCost > 0);

            // Now that we've determined if we can spawn, add the base values.
            fuelCost += ModConfig.vaporizers.modules.clone.fuelBase;
            energyCost += ModConfig.vaporizers.modules.clone.energyBase;
            budgetCost += ModConfig.vaporizers.modules.clone.budgetBase;

            if ( exactCopies ) {
                fuelCost += ModConfig.vaporizers.modules.clone.fuelExact;
                energyCost += ModConfig.vaporizers.modules.clone.energyExact;
                budgetCost += ModConfig.vaporizers.modules.clone.budgetExact;
            }
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
            if ( ModConfig.vaporizers.modules.clone.requireCrystallizedVoidPearls )
                return CRYSTAL_GHOST;

            return pickGhost(ghosts);
        }

        public boolean canRun(boolean ignorePower) {
            this.entities = 0;
            if ( !entityLoaded && !entityBall.isEmpty() )
                updateModifier(entityBall);

            if ( !canSpawn )
                return false;

            // If we operate on entities in a single continuous
            // block, then do our entity check here.
            if ( vaporizer.hasWorld() && vaporizer.canGetFullEntities() ) {
                Class<? extends Entity> klass = EntityUtilities.getEntityClass(vaporizer.getModifier());
                if ( klass == null )
                    klass = EntityLivingBase.class;

                List<Entity> entities = vaporizer.getWorld().getEntitiesWithinAABB(klass, vaporizer.getFullEntitiesAABB().grow(ModConfig.vaporizers.modules.clone.maxRange));
                this.entities = entities.size();

                if ( this.entities >= entityLimit )
                    return false;
            }

            return ignorePower || vaporizer.getEnergyStored() >= energyCost;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( entityBall.isEmpty() )
                return "info." + WirelessUtils.MODID + ".vaporizer.missing_entity";

            if ( !canSpawn )
                return "info." + WirelessUtils.MODID + ".vaporizer.cannot_spawn";

            if ( entities >= entityLimit )
                return "info." + WirelessUtils.MODID + ".vaporizer.too_many_entities";

            return null;
        }

        public int getBlockEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return energyCost;
        }

        public int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getMaxEntityEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return 0;
        }

        public int getActionCost() {
            return budgetCost;
        }

        @Override
        public void preWork() {
            entityLivingData = null;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }

        @Nonnull
        @SuppressWarnings("deprecation")
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            ItemStack modifier = vaporizer.getModifier();
            if ( fuelCost == 0 )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            int removed = vaporizer.removeFuel(fuelCost);
            if ( removed < fuelCost ) {
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
            Entity entity = EntityUtilities.getEntity(modifier, world, exactCopies, null);
            if ( entity == null ) {
                vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            if ( exactCopies )
                entity.setUniqueId(UUID.randomUUID());

            else if ( entityBaby && ModConfig.vaporizers.modules.clone.babyMode == ModConfig.BabyCloningMode.BABY_CLONES ) {
                if ( entity instanceof EntityAgeable )
                    ((EntityAgeable) entity).setGrowingAge(-24000);
                else if ( entity instanceof EntityZombie )
                    ((EntityZombie) entity).setChild(true);
            }

            // ... and spawn it!
            double offsetX = 0.5D;
            double offsetY = 0D;
            double offsetZ = 0.5D;

            if ( ModConfig.vaporizers.modules.clone.randomSpawn ) {
                offsetX = world.rand.nextDouble();
                offsetY = world.rand.nextDouble();
                offsetZ = world.rand.nextDouble();
            }

            double posX = target.pos.getX() + offsetX;
            double posY = target.pos.getY() + offsetY;
            double posZ = target.pos.getZ() + offsetZ;

            if ( ModConfig.vaporizers.modules.clone.useCheckSpawn && (entity instanceof EntityLiving) && ForgeEventFactory.canEntitySpawn((EntityLiving) entity, world, (float) posX, (float) posY, (float) posZ, true) == Event.Result.DENY ) {
                vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_CONTINUE;
            }

            entity.setPositionAndRotation(
                    posX, posY, posZ,
                    360.0F * world.rand.nextFloat(),
                    entity.rotationPitch
            );

            world.spawnEntity(entity);

            if ( entity instanceof EntityLiving ) {
                EntityLiving living = (EntityLiving) entity;
                if ( !exactCopies )
                    entityLivingData = living.onInitialSpawn(world.getDifficultyForLocation(target.pos), entityLivingData);

                if ( ModConfig.vaporizers.modules.clone.useSpawnParticles )
                    living.spawnExplosionParticle();
            }

            if ( removed > fuelCost )
                vaporizer.addFuel(removed - fuelCost);

            if ( vaporizer.canGetFullEntities() ) {
                entities++;
                if ( entities >= entityLimit )
                    return IWorkProvider.WorkResult.SUCCESS_STOP;
            }

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }
    }
}

package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.ElementTheoreticalSlaughterModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ItemTheoreticalSlaughterModule extends ItemModule {

    public ItemTheoreticalSlaughterModule() {
        super();
        setName("theoretical_slaughter_module");
    }

    @Override
    public int getItemEnchantability() {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( (tag == null || !tag.hasKey("Looting", Constants.NBT.TAG_BYTE)) && ModConfig.vaporizers.modules.theoreticalSlaughter.allowLooting && enchantment == Enchantments.LOOTING )
            return true;

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public int getLooting(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null && tag.hasKey("Looting", Constants.NBT.TAG_BYTE) )
            return tag.getByte("Looting");

        return EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.theoreticalSlaughter.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.theoreticalSlaughter.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.theoreticalSlaughter.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.theoreticalSlaughter.energyDrain;
    }

    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("ExactCopies") || super.isConfigured(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = "item." + WirelessUtils.MODID + ".theoretical_slaughter_module";

        if ( getExactCopies(stack) )
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".theoretical_slaughter_module.exact"));
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
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new TheoreticalSlaughterBehavior(vaporizer, stack);
    }

    public static class TheoreticalDamage extends EntityDamageSource {
        private final TileBaseVaporizer vaporizer;

        public TheoreticalDamage(EntityPlayer player, TileBaseVaporizer vaporizer) {
            super("player", player);
            this.vaporizer = vaporizer;
        }

        public int getLooting() {
            return ModItems.itemTheoreticalSlaughterModule.getLooting(vaporizer.getModule());
        }

        public TileBaseVaporizer getVaporizer() {
            return vaporizer;
        }

        @Override
        public boolean isDifficultyScaled() {
            return false;
        }

        @Override
        public boolean isUnblockable() {
            return true;
        }

        @Override
        public boolean isDamageAbsolute() {
            return true;
        }

        @Override
        public boolean canHarmInCreative() {
            return true;
        }

        @Override
        public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
            return null;
        }
    }

    public static class TheoreticalSlaughterBehavior implements TileBaseVaporizer.IVaporizerBehavior {
        private static final ItemStack DIAMOND_GHOST = new ItemStack(Items.DIAMOND_SWORD);
        private final static ItemStack CRYSTAL_GHOST = new ItemStack(ModItems.itemCrystallizedVoidPearl);

        public final TileBaseVaporizer vaporizer;
        private final ItemStack[] ghosts;

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

        private IEntityLivingData entityLivingData;

        private boolean exactCopies = false;

        public TheoreticalSlaughterBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
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

        /* GUI Stuff */

        public int getEntityCost() {
            return fuelCost;
        }

        public boolean hasEntity() {
            return entityLoaded;
        }

        public boolean canExact() {
            return ModConfig.vaporizers.modules.theoreticalSlaughter.allowExact;
        }

        public boolean isExact() {
            return exactCopies;
        }

        public int getExperienceMode() {
            return 3;
        }

        public int getDropMode() {
            return 2;
        }

        /* Other Stuff */

        public void updateCost() {
            if ( !entityLoaded ) {
                fuelCost = 0;
                energyCost = 0;
                budgetCost = 0;
                canSpawn = false;
                return;
            }

            ModConfig.BossMode bossMode = ModConfig.vaporizers.modules.theoreticalSlaughter.bossMode;
            if ( entityBoss && (bossMode == ModConfig.BossMode.DISABLED || (bossMode == ModConfig.BossMode.CREATIVE_ONLY && !vaporizer.isCreative())) ) {
                fuelCost = 0;
                energyCost = 0;
                budgetCost = 0;
                canSpawn = false;
                return;
            }

            int cost = entityCost;
            if ( entityBaby && ModConfig.vaporizers.modules.theoreticalSlaughter.exactBaby )
                cost *= ModConfig.vaporizers.babyMultiplier;

            double rawFuel = (ModConfig.vaporizers.modules.theoreticalSlaughter.fuelPerExp * cost) + (ModConfig.vaporizers.modules.theoreticalSlaughter.fuelPerHealth * entityHealth);
            double rawEnergy = (ModConfig.vaporizers.modules.theoreticalSlaughter.energyPerExp * cost) + (ModConfig.vaporizers.modules.theoreticalSlaughter.energyPerHealth * entityHealth);
            double rawBudget = (ModConfig.vaporizers.modules.theoreticalSlaughter.budgetPerExp * cost) + (ModConfig.vaporizers.modules.theoreticalSlaughter.budgetPerHealth * entityHealth);

            if ( exactCopies ) {
                if ( ModConfig.vaporizers.modules.theoreticalSlaughter.fuelExactFactor != 0 )
                    rawFuel *= ModConfig.vaporizers.modules.theoreticalSlaughter.fuelExactFactor;

                if ( ModConfig.vaporizers.modules.theoreticalSlaughter.energyExactFactor != 0 )
                    rawEnergy *= ModConfig.vaporizers.modules.theoreticalSlaughter.energyExactFactor;

                if ( ModConfig.vaporizers.modules.theoreticalSlaughter.budgetExactFactor != 0 )
                    rawBudget *= ModConfig.vaporizers.modules.theoreticalSlaughter.budgetExactFactor;
            }

            fuelCost = (int) Math.ceil(rawFuel);
            energyCost = (int) Math.ceil(rawEnergy);
            budgetCost = (int) Math.ceil(rawBudget);

            canSpawn = fuelCost > 0 || (!ModConfig.vaporizers.modules.theoreticalSlaughter.requireFuel && energyCost > 0);

            // Now that we've determined if we can spawn, add the base values.
            fuelCost += ModConfig.vaporizers.modules.theoreticalSlaughter.fuelBase;
            energyCost += ModConfig.vaporizers.modules.theoreticalSlaughter.energyBase;
            budgetCost += ModConfig.vaporizers.modules.theoreticalSlaughter.budgetBase;

            if ( exactCopies ) {
                fuelCost += ModConfig.vaporizers.modules.theoreticalSlaughter.fuelExact;
                energyCost += ModConfig.vaporizers.modules.theoreticalSlaughter.energyExact;
                budgetCost += ModConfig.vaporizers.modules.theoreticalSlaughter.budgetExact;
            }
        }

        public boolean isValidBall(@Nonnull ItemStack stack) {
            if ( !EntityUtilities.isFilledEntityBall(stack) )
                return false;

            int value = EntityUtilities.getBaseExperience(stack, vaporizer.getWorld());
            if ( EntityUtilities.containsBabyEntity(stack) )
                value = (int) Math.floor(value * ModConfig.vaporizers.babyMultiplier);

            return value > 0;
        }

        public double getEnergyMultiplier() {
            double exactMultiplier = exactCopies ? ModConfig.vaporizers.modules.theoreticalSlaughter.exactEnergyMultiplier : 1D;

            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return exactMultiplier;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyMultiplier(stack, vaporizer) * exactMultiplier;
        }

        public int getEnergyAddition() {
            int energyAddition = exactCopies ? ModConfig.vaporizers.modules.theoreticalSlaughter.exactEnergyAddition : 0;

            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return energyAddition;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEnergyAddition(stack, vaporizer) + energyAddition;
        }

        public int getEnergyDrain() {
            int exactDrain = exactCopies ? ModConfig.vaporizers.modules.theoreticalSlaughter.exactEnergyDrain : 0;

            ItemStack stack = vaporizer.getModule();
            if ( stack.isEmpty() || !(stack.getItem() instanceof ItemModule) )
                return exactDrain;

            ItemModule item = (ItemModule) stack.getItem();
            return item.getEneryDrain(stack, vaporizer) + exactDrain;
        }

        @Override
        public void updateModule(@Nonnull ItemStack stack) {
            exactCopies = ModItems.itemTheoreticalSlaughterModule.getExactCopies(stack);

            updateCost();
        }

        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementTheoreticalSlaughterModule(gui, this);
        }

        public boolean wantsFluid() {
            return true;
        }

        public boolean canInvert() {
            return false;
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            packet.addBool(exactCopies);
        }

        @Override
        public void handleModePacket(@Nonnull PacketBase packet) {
            ItemStack stack = vaporizer.getModule();
            ModItems.itemTheoreticalSlaughterModule.setExactCopies(stack, packet.getBool());
            vaporizer.setModule(stack);
        }

        public boolean isInputUnlocked(int slot) {
            return ModConfig.vaporizers.useEntitiesFuel;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
            if ( !ModConfig.vaporizers.useEntitiesFuel )
                return false;

            if ( !EntityUtilities.canEmptyBall(stack) )
                return false;

            return isValidBall(stack);
        }

        public void updateInput(int slot) {

        }

        @Nonnull
        @Override
        public ItemStack getInputGhost(int slot) {
            if ( ModConfig.vaporizers.useEntitiesFuel )
                return pickGhost(ghosts, slot);

            return ItemStack.EMPTY;
        }

        public boolean isModifierUnlocked() {
            return true;
        }

        @Nonnull
        public ItemStack getModifierGhost() {
            if ( ModConfig.vaporizers.modules.theoreticalSlaughter.requireCrystallizedVoidPearls )
                return CRYSTAL_GHOST;

            return pickGhost(ghosts);
        }

        public boolean isValidModifier(@Nonnull ItemStack stack) {
            if ( !EntityUtilities.isFilledEntityBall(stack) )
                return false;

            if ( ModConfig.vaporizers.modules.theoreticalSlaughter.requireCrystallizedVoidPearls && stack.getItem() != ModItems.itemCrystallizedVoidPearl )
                return false;

            World world = vaporizer.getWorld();
            if ( world == null )
                return false;

            Entity entity = EntityUtilities.getEntity(stack, world, true, null);
            if ( entity == null || !(entity instanceof EntityLivingBase) )
                return false;

            final EntityLivingBase living = (EntityLivingBase) entity;
            final boolean isBoss = !entity.isNonBoss();
            final ModConfig.BossMode bossMode = ModConfig.vaporizers.modules.theoreticalSlaughter.bossMode;
            if ( isBoss && (bossMode == ModConfig.BossMode.DISABLED || (bossMode == ModConfig.BossMode.CREATIVE_ONLY && !vaporizer.isCreative())) )
                return false;

            final boolean isBaby = EntityUtilities.isBaby(entity);
            if ( isBaby && ModConfig.vaporizers.modules.theoreticalSlaughter.babyMode == ModConfig.BabyCloningMode.DISALLOW )
                return false;

            int expCost = EntityUtilities.getBaseFromEntity(entity);
            if ( isBaby && ModConfig.vaporizers.modules.theoreticalSlaughter.exactBaby )
                expCost *= ModConfig.vaporizers.babyMultiplier;

            final float health = living.getMaxHealth();
            double rawFuel = (ModConfig.vaporizers.modules.theoreticalSlaughter.fuelPerExp * expCost) + (ModConfig.vaporizers.modules.theoreticalSlaughter.fuelPerHealth * health);
            double rawEnergy = (ModConfig.vaporizers.modules.theoreticalSlaughter.energyPerExp * expCost) + (ModConfig.vaporizers.modules.theoreticalSlaughter.energyPerHealth * health);

            if ( exactCopies ) {
                if ( ModConfig.vaporizers.modules.theoreticalSlaughter.fuelExactFactor != 0 )
                    rawFuel *= ModConfig.vaporizers.modules.theoreticalSlaughter.fuelExactFactor;

                if ( ModConfig.vaporizers.modules.theoreticalSlaughter.energyExactFactor != 0 )
                    rawEnergy *= ModConfig.vaporizers.modules.theoreticalSlaughter.energyExactFactor;
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

            updateCost();
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

        public Class<? extends Entity> getEntityClass() {
            return null;
        }

        public Predicate<? super Entity> getEntityFilter() {
            return null;
        }

        @Override
        public boolean canRun(boolean ignorePower) {
            if ( !entityLoaded )
                updateModifier(vaporizer.getModifier());

            if ( !canSpawn )
                return false;

            if ( !vaporizer.hasEmptyOutput() )
                return false;

            return ignorePower || vaporizer.getEnergyStored() >= energyCost;
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( entityBall.isEmpty() )
                return "info." + WirelessUtils.MODID + ".vaporizer.missing_entity";

            if ( !canSpawn )
                return "info." + WirelessUtils.MODID + ".vaporizer.cannot_spawn";

            if ( !vaporizer.hasEmptyOutput() )
                return "info." + WirelessUtils.MODID + ".vaporizer.no_empty_output";

            return null;
        }

        @Override
        public void preWork() {
            entityLivingData = null;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            if ( fuelCost == 0 || entityBall.isEmpty() )
                return IWorkProvider.WorkResult.FAILURE_STOP_IN_PLACE;

            TileBaseVaporizer.WUVaporizerPlayer player = vaporizer.getFakePlayer(world);
            if ( player == null )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            int removed = vaporizer.removeFuel(fuelCost);
            if ( removed < fuelCost ) {
                if ( removed > 0 )
                    vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            // Clone an entity.
            Entity entity = EntityUtilities.getEntity(entityBall, world, exactCopies, null);
            if ( !(entity instanceof EntityLivingBase) ) {
                vaporizer.addFuel(removed);
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            EntityLivingBase base = (EntityLivingBase) entity;

            if ( exactCopies )
                entity.setUniqueId(UUID.randomUUID());
            else {
                if ( entity instanceof EntityLiving )
                    entityLivingData = ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(target.pos), entityLivingData);

                if ( entityBaby && ModConfig.vaporizers.modules.theoreticalSlaughter.babyMode == ModConfig.BabyCloningMode.BABY_CLONES ) {
                    if ( entity instanceof EntityAgeable )
                        ((EntityAgeable) entity).setGrowingAge(-24000);
                    else if ( entity instanceof EntityZombie )
                        ((EntityZombie) entity).setChild(true);
                }
            }

            // We kill it! Theoretically.
            base.onDeath(new TheoreticalDamage(player, vaporizer));

            // Did we kill it? Did we not? Either way, it's dead.
            // Then again, it was never alive to start.
            // It's theoretically dead.
            entity.setDead();

            if ( removed > fuelCost )
                vaporizer.addFuel(removed - fuelCost);

            return IWorkProvider.WorkResult.SUCCESS_REPEAT;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}

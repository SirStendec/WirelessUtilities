package com.lordmau5.wirelessutils.item.module;

import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.ElementFishingModule;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.tile.vaporizer.TileDirectionalVaporizer;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemFishingModule extends ItemModule {

    public ItemFishingModule() {
        super();
        setName("fishing_module");
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return vaporizer instanceof TileDirectionalVaporizer;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.vaporizers.modules.fishing.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.fishing.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.fishing.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.vaporizers.modules.fishing.energyDrain;
    }

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("CollectDrops") || tag.hasKey("CollectExp") || super.isConfigured(stack);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = "item." + WirelessUtils.MODID + ".fishing_module";

        tooltip.add(new TextComponentTranslation(
                name + ".drops",
                StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getDropMode(stack))
        ).getFormattedText());

        if ( ModConfig.vaporizers.modules.fishing.maxExp > 0 )
            tooltip.add(new TextComponentTranslation(
                    name + ".exp",
                    StringHelper.localize("btn." + WirelessUtils.MODID + ".drop_mode." + getExperienceMode(stack))
            ).getFormattedText());
    }

    public int getDropMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("CollectDrops", Constants.NBT.TAG_BYTE) )
                return tag.getByte("CollectDrops");
        }

        return ModConfig.vaporizers.modules.fishing.collectDrops;
    }

    @Nonnull
    public ItemStack setDropMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < ModConfig.vaporizers.modules.fishing.collectDropsMinimum )
            mode = 3;
        else if ( mode > 3 )
            mode = ModConfig.vaporizers.modules.fishing.collectDropsMinimum;

        tag.setByte("CollectDrops", (byte) mode);
        stack.setTagCompound(tag);
        return stack;
    }

    public int getExperienceMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("CollectExp", Constants.NBT.TAG_BYTE) )
                return tag.getByte("CollectExp");
        }

        return ModConfig.vaporizers.modules.fishing.collectExperience;
    }

    @Nonnull
    public ItemStack setExperienceMode(@Nonnull ItemStack stack, int mode) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( mode < ModConfig.vaporizers.modules.fishing.collectExperienceMinimum )
            mode = 3;
        else if ( mode > 3 )
            mode = ModConfig.vaporizers.modules.fishing.collectExperienceMinimum;

        tag.setByte("CollectExp", (byte) mode);
        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new FishingBehavior(vaporizer, stack);
    }

    public static class FishingBehavior implements TileBaseVaporizer.IVaporizerBehavior {

        public final static ItemStack GHOST = new ItemStack(Items.FISHING_ROD);

        public final TileBaseVaporizer vaporizer;
        private final Map<BlockPosDimension, EntityFishHook> hooks = new Object2ObjectArrayMap<>();

        private int dropMode = 0;
        private int experienceMode = 0;

        private int energyCost = 0;
        private int budgetCost = 0;

        private boolean hasRod = false;

        public FishingBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            this.vaporizer = vaporizer;
            updateModule(module);
            updateRod();
        }

        public void updateCost() {
            energyCost = ModConfig.vaporizers.modules.fishing.energy;
            budgetCost = ModConfig.vaporizers.modules.fishing.budget;

            if ( hasRod ) {
                energyCost += ModConfig.vaporizers.modules.fishing.energyRod;
                budgetCost += ModConfig.vaporizers.modules.fishing.budgetRod;
            }
        }

        public void updateRod() {
            hasRod = isValidRod(vaporizer.getInput().getStackInSlot(0));
            updateCost();

            if ( !canRun() )
                destroyHooks();
        }

        public boolean isValidRod(@Nonnull ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ItemFishingRod;
        }

        public void destroyHooks() {
            if ( !hooks.isEmpty() ) {
                for (Map.Entry<BlockPosDimension, EntityFishHook> entry : hooks.entrySet()) {
                    final EntityFishHook hook = entry.getValue();
                    if ( hook != null && !hook.isDead )
                        hook.setDead();
                }

                hooks.clear();
            }
        }

        @Override
        public void onRemove() {
            destroyHooks();
        }

        @Override
        public void onDestroy() {
            destroyHooks();
        }

        @Override
        public void onInactive() {
            destroyHooks();
        }

        public void updateModule(@Nonnull ItemStack stack) {
            dropMode = ModItems.itemFishingModule.getDropMode(stack);
            experienceMode = ModItems.itemFishingModule.getExperienceMode(stack);

            updateCost();
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            packet.addByte(dropMode);
            packet.addByte(experienceMode);
        }

        @Override
        public void handleModePacket(@Nonnull PacketBase packet) {
            ItemStack stack = vaporizer.getModule();

            ModItems.itemFishingModule.setDropMode(stack, packet.getByte());
            ModItems.itemFishingModule.setExperienceMode(stack, packet.getByte());

            if ( !stack.isEmpty() )
                vaporizer.setModule(stack);
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
            if ( slot == 0 && ModConfig.vaporizers.modules.fishing.allowRod )
                return true;

            return false;
        }

        public boolean isValidInput(@Nonnull ItemStack stack, int slot) {
            if ( slot == 0 && ModConfig.vaporizers.modules.fishing.allowRod )
                return isValidRod(stack);

            return false;
        }

        @Override
        public void updateInput(int slot) {
            if ( slot == 0 && ModConfig.vaporizers.modules.fishing.allowRod )
                updateRod();
        }

        @Nonnull
        @Override
        public ItemStack getInputGhost(int slot) {
            if ( slot == 0 )
                return GHOST;

            return ItemStack.EMPTY;
        }

        public ElementModuleBase getGUI(@Nonnull GuiBaseVaporizer gui) {
            return new ElementFishingModule(gui, this);
        }

        public boolean wantsFluid() {
            return false;
        }

        public boolean canRun() {
            if ( ModConfig.vaporizers.modules.fishing.requireRod && !hasRod )
                return false;

            return true;
        }

        public boolean canRun(boolean ignorePower) {
            return canRun() && (ignorePower || vaporizer.getEnergyStored() >= energyCost);
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            if ( ModConfig.vaporizers.modules.fishing.requireRod && !hasRod )
                return "info." + WirelessUtils.MODID + ".vaporizer.missing_rod";

            return null;
        }

        public int getExperienceMode() {
            return Math.max(experienceMode, ModConfig.vaporizers.modules.fishing.collectExperienceMinimum);
        }

        public int getDropMode() {
            return Math.max(dropMode, ModConfig.vaporizers.modules.fishing.collectDropsMinimum);
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

        @Nullable
        public Class<? extends Entity> getEntityClass() {
            return null;
        }

        @Nullable
        public Predicate<? super Entity> getEntityFilter() {
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
        public boolean canWorkBlock(@Nonnull BlockPosDimension target, @Nonnull ItemStack source, @Nonnull World world, @Nonnull IBlockState block, @Nullable TileEntity tile) {
            if ( world.isAirBlock(target) && world.getBlockState(target.down()).getMaterial() == Material.WATER )
                return true;

            return false;
            //return block.getMaterial() == Material.WATER && world.isAirBlock(target.up());
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            if ( target.pos == null )
                return IWorkProvider.WorkResult.FAILURE_REMOVE;

            if ( !canRun() ) {
                destroyHooks();
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            WUFakePlayer player = vaporizer.getFakePlayer(world);
            if ( player == null ) {
                destroyHooks();
                return IWorkProvider.WorkResult.FAILURE_STOP;
            }

            // Do we already have a fishing hook for that block?
            if ( hooks.containsKey(target.pos) ) {
                final EntityFishHook existing = hooks.get(target.pos);
                // Make sure it's a valid hook, and maybe reel it in.
                if ( existing != null && !existing.isDead ) {
                    if ( existing.ticksCatchable > 0 ) {
                        ItemStack rod = hasRod ? vaporizer.getInput().getStackInSlot(0) : ItemStack.EMPTY;
                        if ( !rod.isEmpty() )
                            existing.setLuck(EnchantmentHelper.getFishingLuckBonus(rod));

                        int damage = existing.handleHookRetraction();
                        world.playSound(null, target.pos, SoundEvents.ENTITY_BOBBER_RETRIEVE, SoundCategory.BLOCKS, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
                        if ( damage > 0 && !rod.isEmpty() ) {
                            rod.damageItem(damage, player);
                            if ( rod.isEmpty() )
                                vaporizer.getInput().setStackInSlot(0, rod);

                            vaporizer.markChunkDirty();
                        }

                        if ( !canRun() ) {
                            destroyHooks();
                            return IWorkProvider.WorkResult.SUCCESS_STOP;
                        }

                        return IWorkProvider.WorkResult.SUCCESS_CONTINUE;

                    } else
                        return IWorkProvider.WorkResult.FAILURE_CONTINUE;

                } else
                    hooks.remove(target.pos);
            }

            // Make sure we don't have more active hooks than we're allowed.
            if ( hooks.size() >= ModConfig.vaporizers.modules.fishing.maxCasts )
                return IWorkProvider.WorkResult.FAILURE_CONTINUE;

            final ItemStack rod = hasRod ? vaporizer.getInput().getStackInSlot(0) : ItemStack.EMPTY;
            player.inventory.clear();
            player.setHeldItem(EnumHand.MAIN_HAND, hasRod ? rod : new ItemStack(Items.FISHING_ROD));

            final EntityFishHook hook = new EntityFishHook(world, player);

            hook.setPosition(target.pos.getX() + 0.5D, target.pos.getY() + 0.5D, target.pos.getZ() + 0.5D);
            hook.motionX = 0;
            hook.motionY = 0;
            hook.motionZ = 0;

            final int speed = EnchantmentHelper.getFishingSpeedBonus(rod);
            if ( speed > 0 )
                hook.setLureSpeed(speed);

            hooks.put(target.pos, hook);
            world.spawnEntity(hook);
            world.playSound(null, target.pos, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.BLOCKS, 0.25F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}

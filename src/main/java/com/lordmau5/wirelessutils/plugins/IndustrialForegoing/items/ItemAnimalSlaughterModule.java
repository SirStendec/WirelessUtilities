package com.lordmau5.wirelessutils.plugins.IndustrialForegoing.items;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemFilteringModule;
import com.lordmau5.wirelessutils.item.module.ItemSlaughterModule;
import com.lordmau5.wirelessutils.plugins.IndustrialForegoing.IFPlugin;
import com.lordmau5.wirelessutils.plugins.IndustrialForegoing.gui.ElementAnimalSlaughterModule;
import com.lordmau5.wirelessutils.tile.base.IWorkProvider;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemAnimalSlaughterModule extends ItemFilteringModule {

    public ItemAnimalSlaughterModule() {
        super();
        setName("animal_slaughter_module");
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        FluidStack fluid;
        if ( getMeatMode(stack) )
            fluid = FluidRegistry.getFluidStack("meat", 0);
        else
            fluid = FluidRegistry.getFluidStack("if.pink_slime", 0);

        tooltip.add(new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".animal_slaughter_module.producing",
                TextHelpers.getComponent(fluid)
        ).getFormattedText());
    }

    @Override
    public boolean allowPlayerMode() {
        return false;
    }

    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return true;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.plugins.industrialForegoing.animalSlaughterModule.requiredLevel);
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.plugins.industrialForegoing.animalSlaughterModule.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.plugins.industrialForegoing.animalSlaughterModule.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable TileBaseVaporizer vaporizer) {
        return ModConfig.plugins.industrialForegoing.animalSlaughterModule.energyDrain;
    }

    @Override
    public boolean isConfigured(@Nonnull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return false;

        return tag.hasKey("MeatMode") || super.isConfigured(stack);
    }

    public boolean getMeatMode(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("MeatMode");
        }

        return false;
    }

    @Nonnull
    public ItemStack setMeatMode(@Nonnull ItemStack stack, boolean enabled) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && !enabled )
            return stack;
        else if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( enabled )
            tag.setBoolean("MeatMode", true);
        else
            tag.removeTag("MeatMode");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        return new AnimalSlaughterBehavior(vaporizer, stack);
    }

    public static class AnimalSlaughterBehavior extends FilteredBehavior {

        private boolean slimeMode = true;

        public AnimalSlaughterBehavior(@Nonnull TileBaseVaporizer vaporizer, @Nonnull ItemStack module) {
            super(vaporizer);

            allowBosses = ModConfig.vaporizers.modules.slaughter.attackBosses;
            allowPlayers = false;

            requireAttackable = true;
            requireAlive = true;

            updateModule(module);
        }

        public boolean getMeatMode() {
            return !slimeMode;
        }

        @Override
        public void updateModePacket(@Nonnull PacketBase packet) {
            super.updateModePacket(packet);
            packet.addBool(getMeatMode());
        }

        @Nonnull
        @Override
        public ItemStack handleModeDelegate(@Nonnull ItemStack stack, @Nonnull PacketBase packet) {
            IFPlugin.itemAnimalSlaughterModule.setMeatMode(stack, packet.getBool());
            return stack;
        }

        @Override
        public void updateModule(@Nonnull ItemStack stack) {
            super.updateModule(stack);

            slimeMode = !IFPlugin.itemAnimalSlaughterModule.getMeatMode(stack);
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
            return new ElementAnimalSlaughterModule(gui, this);
        }

        public boolean wantsFluid() {
            return false;
        }

        public int getExperienceMode() {
            return 3;
        }

        public int getDropMode() {
            return 3;
        }

        public int getBlockEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return 0;
        }

        public int getEntityEnergyCost(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return ModConfig.plugins.industrialForegoing.animalSlaughterModule.energy;
        }

        public int getMaxEntityEnergyCost(@Nonnull TileBaseVaporizer.VaporizerTarget target) {
            return ModConfig.plugins.industrialForegoing.animalSlaughterModule.energy;
        }

        public int getActionCost() {
            return ModConfig.plugins.industrialForegoing.animalSlaughterModule.budget;
        }

        @Override
        public boolean hasSpecialFluid() {
            return true;
        }

        @Override
        public boolean isFluidValid(Fluid fluid) {
            if ( fluid == null )
                return false;

            final String name = fluid.getName();
            return slimeMode ?
                    name.equalsIgnoreCase("if.pink_slime") :
                    name.equalsIgnoreCase("meat");
        }

        @Nullable
        @Override
        public FluidStack getSpecialFluid() {
            if ( slimeMode )
                return FluidRegistry.getFluidStack("if.pink_slime", 0);
            return FluidRegistry.getFluidStack("meat", 0);
        }

        @Nullable
        @Override
        public String getUnconfiguredExplanation() {
            FluidStack stack = vaporizer.getTankFluid();
            if ( stack != null ) {
                if ( stack.amount >= vaporizer.getTank().getCapacity() )
                    return "item.wirelessutils.animal_slaughter_module.fluid_full";

                if ( !isFluidValid(stack.getFluid()) )
                    return "item.wirelessutils.animal_slaughter_module.fluid_invalid";
            }

            return super.getUnconfiguredExplanation();
        }

        @Override
        public boolean canRun(boolean ignorePower) {
            FluidStack stack = vaporizer.getTankFluid();
            if ( stack != null ) {
                if ( stack.amount >= vaporizer.getTank().getCapacity() )
                    return false;

                if ( !isFluidValid(stack.getFluid()) )
                    return false;
            }

            return super.canRun(ignorePower);
        }

        @Nonnull
        public IWorkProvider.WorkResult processEntity(@Nonnull Entity entity, @Nonnull TileBaseVaporizer.VaporizerTarget target) {
            EntityLivingBase living = (EntityLivingBase) entity;

            TileBaseVaporizer.WUVaporizerPlayer player = vaporizer.getFakePlayer(living.world);
            if ( player == null )
                return IWorkProvider.WorkResult.FAILURE_STOP;

            player.capabilities.isCreativeMode = vaporizer.isCreative();
            player.inventory.clear();
            player.updateAttributes(true);
            player.updateCooldown();

            float health = living.getHealth();
            float damage = living.getAbsorptionAmount();
            float max = (float) ModConfig.vaporizers.modules.slaughter.maxDamage;
            int mode = ModConfig.vaporizers.modules.slaughter.damageMode;
            if ( mode == 0 )
                damage += health;
            else if ( mode == 1 )
                damage += living.getMaxHealth();
            else if ( max == 0 )
                damage = 1000000000;
            else
                damage = max;

            if ( max != 0 && damage > max )
                damage = max;

            boolean success = living.attackEntityFrom(new ItemSlaughterModule.VaporizerDamage(player, vaporizer), damage);

            // Make sure we didn't murder things too hard. This is
            // legitimately a thing that can leave you with entities
            // wandering around, unable to die.
            if ( Float.isNaN(living.getAbsorptionAmount()) )
                living.setAbsorptionAmount(0);

            if ( Float.isNaN(living.getHealth()) )
                living.setHealth(1);

            if ( success ) {
                health -= living.getHealth();
                if ( health > 0 ) {
                    FluidStack out;
                    final boolean isAnimal = living instanceof EntityAnimal;
                    if ( slimeMode )
                        out = FluidRegistry.getFluidStack("if.pink_slime", (int) (health * (isAnimal ? ModConfig.plugins.industrialForegoing.animalSlaughterModule.animalSlime : ModConfig.plugins.industrialForegoing.animalSlaughterModule.otherSlime)));
                    else
                        out = FluidRegistry.getFluidStack("meat", (int) (health * (isAnimal ? ModConfig.plugins.industrialForegoing.animalSlaughterModule.animalMeat : ModConfig.plugins.industrialForegoing.animalSlaughterModule.otherMeat)));

                    vaporizer.getTank().fill(out, true);
                }

                if ( vaporizer.isTankFull() )
                    return IWorkProvider.WorkResult.SUCCESS_STOP;
            }

            return IWorkProvider.WorkResult.SUCCESS_CONTINUE;
        }

        @Nonnull
        public IWorkProvider.WorkResult processBlock(@Nonnull TileBaseVaporizer.VaporizerTarget target, @Nonnull World world) {
            return IWorkProvider.WorkResult.FAILURE_REMOVE;
        }
    }
}

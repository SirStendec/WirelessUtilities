package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFluidGenAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFluidGenAugment extends ItemAugment {

    public ItemFluidGenAugment() {
        super();
        setName("fluid_gen_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.fluidGen.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.fluidGen.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        // We want to show our drain for tooltips, but not for actual machines.
        if ( augmentable == null )
            return getEnergy(stack);

        return 0;
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.fluidGen.budgetAddition;
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.fluidGen.budgetMultiplier;
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( !(augmentable instanceof IFluidGenAugmentable) )
            return;

        IFluidGenAugmentable machine = (IFluidGenAugmentable) augmentable;

        if ( stack.isEmpty() || stack.getItem() != this ) {
            machine.setFluidGenAugmented(false, null, 0);
            return;
        }

        machine.setFluidGenAugmented(true, getFluid(stack), getEnergy(stack));
    }

    public int getEnergy(@Nonnull ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() == this ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Energy") )
                return tag.getInteger("Energy");
        }

        return ModConfig.augments.fluidGen.energyCost;
    }

    public FluidStack getFluid(@Nonnull ItemStack stack) {
        FluidStack fluid = null;
        if ( !stack.isEmpty() && stack.getItem() == this ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null ) {
                if ( tag.hasKey("Cows") )
                    fluid = FluidRegistry.getFluidStack("milk", tag.getByte("Cows") * ModConfig.augments.fluidGen.milkRate);

                else if ( tag.hasKey("Mooshrooms") )
                    fluid = FluidRegistry.getFluidStack("mushroom_stew", tag.getByte("Mooshrooms") * ModConfig.augments.fluidGen.stewRate);

                else if ( tag.hasKey("Fluid") )
                    fluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("Fluid"));
            }
        }

        if ( fluid == null ) {
            int amount = ModConfig.augments.fluidGen.fluidRate;
            fluid = FluidRegistry.getFluidStack(ModConfig.augments.fluidGen.fluidName, amount);
            if ( fluid == null )
                fluid = new FluidStack(FluidRegistry.WATER, amount);
        }

        return fluid;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getHighlightTip(@Nonnull ItemStack item, @Nonnull String displayName) {
        displayName = super.getHighlightTip(item, displayName);
        FluidStack fluid = getFluid(item);

        if ( fluid != null ) {
            displayName = new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".tiered.name",
                    displayName,
                    getStackComponent(fluid)
            ).getFormattedText();
        }

        return displayName;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.augments.fluidGen.requiredLevel);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        FluidStack fluid = getFluid(stack);
        int energy = getEnergy(stack);

        if ( fluid != null ) {
            String name = stack.getTranslationKey();

            tooltip.add(new TextComponentTranslation(
                    name + ".fluid",
                    getStackComponent(fluid)
            ).getFormattedText());

            tooltip.add(new TextComponentTranslation(
                    name + ".fluid_amount",
                    StringHelper.formatNumber(fluid.amount)
            ).getFormattedText());

            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("Cows") )
                tooltip.add(new TextComponentTranslation(
                        name + ".cows",
                        tag.getByte("Cows")
                ).getFormattedText());

            if ( tag != null && tag.hasKey("Mooshrooms") )
                tooltip.add(new TextComponentTranslation(
                        name + ".mooshrooms",
                        tag.getByte("Mooshrooms")
                ).getFormattedText());

            /*if ( energy > 0 )
                tooltip.add(new TextComponentTranslation(
                        name + ".energy",
                        TextHelpers.getScaledNumber(energy, "RF/t", true)
                ).getFormattedText());*/
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public ITextComponent getStackComponent(FluidStack stack) {
        if ( stack == null )
            return null;

        Fluid fluid = stack.getFluid();
        if ( fluid == null )
            return null;

        EnumRarity rarity = fluid.getRarity(stack);
        return new TextComponentString(stack.getLocalizedName())
                .setStyle(TextHelpers.getStyle(rarity.color));
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if ( entity.world.isRemote )
            return false;

        NBTTagCompound tag = stack.getTagCompound();

        String name;
        byte count;

        if ( (entity instanceof EntityMooshroom) && FluidRegistry.isFluidRegistered("mushroom_stew") ) {
            byte mooshrooms = tag == null ? 0 : tag.getByte("Mooshrooms");
            if ( mooshrooms >= ModConfig.augments.fluidGen.allowMooshrooms )
                return false;

            if ( tag != null ) {
                if ( tag.hasKey("Cows") || tag.hasKey("Fluid") )
                    return false;
            }

            name = "Mooshrooms";
            count = (byte) (mooshrooms + 1);

            // We don't use instanceof here because mods have subclasses of cows that don't give milk.
            // So, basically... Moo Fluids. TODO support for Moo Fluids?
        } else if ( (entity.getClass().equals(EntityCow.class)) && FluidRegistry.isFluidRegistered("milk") ) {
            byte cows = tag == null ? 0 : tag.getByte("Cows");
            if ( cows >= ModConfig.augments.fluidGen.allowCows )
                return false;

            if ( tag != null ) {
                if ( tag.hasKey("Mooshrooms") || tag.hasKey("Fluid") )
                    return false;
            }

            name = "Cows";
            count = (byte) (cows + 1);

        } else
            return false;

        ItemStack newItem = stack.copy();
        newItem.setCount(1);

        tag = newItem.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        tag.setByte(name, count);
        newItem.setTagCompound(tag);
        entity.setDead();

        player.world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.PLAYERS, 0.6F, 1.0F);

        if ( player.world instanceof WorldServer ) {
            WorldServer ws = (WorldServer) player.world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, entity.posX, entity.posY + (entity.height / 2), entity.posZ, 3, 0.2D, 0.2D, 0.2D, 0D);
        }

        if ( stack.getCount() == 1 )
            player.setHeldItem(hand, newItem);
        else {
            stack.shrink(1);
            player.setHeldItem(hand, stack);
            if ( !player.addItemStackToInventory(newItem) )
                CoreUtils.dropItemStackIntoWorldWithVelocity(newItem, player.world, player.getPositionVector());
        }

        return true;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if ( !stack.isEmpty() && stack.getItem() == this ) {
            FluidStack fluidStack = getFluid(stack);
            if ( fluidStack != null ) {
                Fluid fluid = fluidStack.getFluid();
                int drink = ModConfig.augments.fluidGen.milkDrink;
                if ( drink != 0 && fluidStack.amount >= drink && fluid.getName().equalsIgnoreCase("milk") ) {
                    player.setActiveHand(hand);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }

                int eat = ModConfig.augments.fluidGen.stewEat;
                if ( eat != 0 && fluidStack.amount >= eat && fluid.getName().equalsIgnoreCase("mushroom_stew") ) {
                    if ( player.canEat(false) ) {
                        player.setActiveHand(hand);
                        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                    }

                    return new ActionResult<>(EnumActionResult.FAIL, stack);
                }
            }

            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.getByte("Cows") > 0 ) {
                world.playSound(player, player.getPosition(), SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.PLAYERS, 0.6F, world.rand.nextFloat() * 0.1F + 0.9F);
                player.getCooldownTracker().setCooldown(this, 10);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        FluidStack fluidStack = getFluid(stack);
        if ( fluidStack != null ) {
            Fluid fluid = fluidStack.getFluid();
            int drink = ModConfig.augments.fluidGen.milkDrink;
            if ( drink != 0 && fluidStack.amount >= drink && fluid.getName().equalsIgnoreCase("milk") ) {
                ItemStack bucket = new ItemStack(Items.MILK_BUCKET);
                if ( !worldIn.isRemote )
                    entityLiving.curePotionEffects(bucket);

                if ( entityLiving instanceof EntityPlayerMP ) {
                    EntityPlayerMP player = (EntityPlayerMP) entityLiving;
                    CriteriaTriggers.CONSUME_ITEM.trigger(player, bucket);
                    player.addStat(StatList.getObjectUseStats(Items.MILK_BUCKET));
                }

                return stack;
            }

            int eat = ModConfig.augments.fluidGen.stewEat;
            if ( eat != 0 && fluidStack.amount >= eat && fluid.getName().equalsIgnoreCase("mushroom_stew") ) {
                if ( entityLiving instanceof EntityPlayer ) {
                    EntityPlayer player = (EntityPlayer) entityLiving;
                    ItemStack stew = new ItemStack(Items.MUSHROOM_STEW);
                    player.getFoodStats().addStats((ItemFood) Items.MUSHROOM_STEW, stew);
                    worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
                    player.addStat(StatList.getObjectUseStats(Items.MUSHROOM_STEW));

                    if ( player instanceof EntityPlayerMP ) {
                        CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stew);
                    }
                }

                return stack;
            }
        }

        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        FluidStack fluidStack = getFluid(stack);
        if ( fluidStack != null ) {
            Fluid fluid = fluidStack.getFluid();
            int drink = ModConfig.augments.fluidGen.milkDrink;
            if ( drink != 0 && fluidStack.amount >= drink && fluid.getName().equalsIgnoreCase("milk") )
                return 32;

            int eat = ModConfig.augments.fluidGen.stewEat;
            if ( eat != 0 && fluidStack.amount >= eat && fluid.getName().equalsIgnoreCase("mushroom_stew") )
                return 32;
        }

        return super.getMaxItemUseDuration(stack);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        FluidStack fluidStack = getFluid(stack);
        if ( fluidStack != null ) {
            Fluid fluid = fluidStack.getFluid();
            int drink = ModConfig.augments.fluidGen.milkDrink;
            if ( drink != 0 && fluidStack.amount >= drink && fluid.getName().equalsIgnoreCase("milk") )
                return EnumAction.DRINK;

            int eat = ModConfig.augments.fluidGen.stewEat;
            if ( eat != 0 && fluidStack.amount >= eat && fluid.getName().equalsIgnoreCase("mushroom_stew") )
                return EnumAction.EAT;
        }

        return super.getItemUseAction(stack);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IFluidGenAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IFluidGenAugmentable;
    }
}

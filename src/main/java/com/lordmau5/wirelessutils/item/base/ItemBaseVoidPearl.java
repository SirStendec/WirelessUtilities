package com.lordmau5.wirelessutils.item.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.crafting.INBTPreservingIngredient;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import mezz.jei.api.IModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBaseVoidPearl extends ItemBasePearl implements INBTPreservingIngredient, IDimensionallyStableItem, EntityUtilities.IEntityBall {

    private static final String I18N_KEY = "item." + WirelessUtils.MODID + ".void_pearl";

    public ItemBaseVoidPearl() {
        super();

        addPropertyOverride(
                new ResourceLocation("filled"),
                (stack, worldIn, entityIn) -> {
                    Item item = stack.getItem();
                    if ( item instanceof ItemBaseVoidPearl )
                        return ((ItemBaseVoidPearl) item).isFilledBall(stack) ? 1F : 0F;

                    return 0F;
                });

        EntityUtilities.registerHandler(this, this);
    }

    @Override
    public boolean isValidForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        return output.getItem() instanceof ItemBaseVoidPearl;
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTTagForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return null;

        NBTTagCompound out = new NBTTagCompound();

        if ( tag.hasKey("EntityID") )
            out.setTag("EntityID", tag.getTag("EntityID"));

        if ( tag.hasKey("EntityData") )
            out.setTag("EntityData", tag.getTag("EntityData"));

        if ( out.getSize() != 0 )
            return out;

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, stack -> new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        items.add(new ItemStack(this, 1, 0));
    }

    @Override
    public void registerJEI(IModRegistry registry) {
        IJEIInformationItem.addJEIInformation(registry, new ItemStack(this, 1, 0));
    }

    @Override
    public float getProjectileInaccuracy(@Nonnull ItemStack stack) {
        return 0F;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        if ( isFilledBall(stack) ) {
            tooltip.add(new TextComponentTranslation(
                    I18N_KEY + ".contains",
                    getCapturedName(stack)
            ).getFormattedText());

            float health = getCapturedHealth(stack);
            if ( health > 0 )
                tooltip.add(new TextComponentTranslation(
                        I18N_KEY + ".health",
                        health
                ).getFormattedText());

            int exp = getBaseExperience(stack);
            if ( isBabyEntity(stack) )
                exp = (int) Math.floor(exp * ModConfig.vaporizers.babyMultiplier);

            if ( exp != 0 )
                tooltip.add(new TextComponentTranslation(
                        I18N_KEY + ".value",
                        StringHelper.formatNumber(exp)
                ).getFormattedText());

        } else
            tooltip.add(new TextComponentTranslation(
                    I18N_KEY + ".empty"
            ).setStyle(TextHelpers.GRAY).getFormattedText());

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getHighlightTip(@Nonnull ItemStack stack, @Nonnull String displayName) {
        displayName = super.getHighlightTip(stack, displayName);

        if ( isFilledBall(stack) ) {
            displayName = new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".tiered.name",
                    displayName,
                    getCapturedName(stack)
            ).getFormattedText();
        }

        return displayName;
    }

    @Override
    public void onPortalImpact(@Nonnull ItemStack stack, @Nonnull EntityItem entity, @Nonnull IBlockState state) {
        Block block = state.getBlock();
        if ( block == Blocks.END_PORTAL ) {
            entity.setPosition(entity.posX, Math.ceil(entity.posY), entity.posZ);
            entity.setVelocity(entity.motionX, .2, entity.motionZ);
        } else if ( block == Blocks.END_GATEWAY )
            entity.setVelocity(-entity.motionX, -entity.motionY, -entity.motionZ);

        if ( entity.world != null && !entity.world.isRemote ) {
            NBTTagCompound data = entity.getEntityData();
            int ticks = data.getInteger("WUSoundTicks");
            if ( ticks == 0 || Math.abs(ticks - entity.ticksExisted) > 1 ) {
                entity.world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.NEUTRAL, 0.5F, 0.2F);
                data.setInteger("WUSoundTicks", entity.ticksExisted);
            }
        }
    }

    @Override
    public boolean shouldStackShrink(@Nonnull ItemStack stack, EntityPlayer player) {
        return isFilledBall(stack) || super.shouldStackShrink(stack, player);
    }

    /* Entity Stuff */

    public float getCapturedHealth(@Nonnull ItemStack stack) {
        if ( !isFilledBall(stack) )
            return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null ) {
            NBTTagCompound entity = tag.getCompoundTag("EntityData");
            if ( entity != null )
                return entity.getFloat("Health");
        }

        return 0;
    }

    public ITextComponent getCapturedName(@Nonnull ItemStack stack) {
        if ( !isFilledBall(stack) )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        String name = EntityList.getTranslationName(new ResourceLocation(tag.getString("EntityID")));
        if ( name == null )
            return null;

        ITextComponent out;

        String key = "entity." + name + ".name";
        if ( StringHelper.canLocalize(key) )
            out = new TextComponentTranslation(key);

        else if ( StringHelper.canLocalize(name) )
            out = new TextComponentTranslation(name);

        else
            out = new TextComponentString(name);

        if ( isBabyEntity(stack) )
            out = new TextComponentTranslation(
                    I18N_KEY + ".baby",
                    out
            );

        return out;
    }

    public int getBaseExperience(@Nonnull ItemStack stack) {
        ResourceLocation name = getEntityId(stack);
        if ( name == null )
            return 0;

        return EntityUtilities.getBaseExperience(name, (World) null);
    }

    /* IEntityBall */

    public boolean isValidBall(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == this;
    }

    public boolean isFilledBall(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this || !stack.hasTagCompound() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("EntityID", Constants.NBT.TAG_STRING);
    }

    public boolean isBabyEntity(@Nonnull ItemStack stack) {
        if ( !isFilledBall(stack) )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound entity = tag.getCompoundTag("EntityData");
        return entity != null && (entity.getBoolean("IsBaby") || entity.getInteger("Age") < 0);
    }

    @Nullable
    public Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData) {
        if ( !isFilledBall(stack) )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(tag.getString("EntityID")), world);
        if ( entity != null && withData && tag.hasKey("EntityData", Constants.NBT.TAG_COMPOUND) )
            entity.readFromNBT(tag.getCompoundTag("EntityData"));

        return entity;
    }

    @Nullable
    public Class<? extends Entity> getEntityClass(@Nonnull ItemStack stack) {
        if ( !isFilledBall(stack) )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        return EntityList.getClass(new ResourceLocation(tag.getString("EntityID")));
    }

    @Nullable
    public ResourceLocation getEntityId(@Nonnull ItemStack stack) {
        if ( !isFilledBall(stack) )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        return new ResourceLocation(tag.getString("EntityID"));
    }

    @Nonnull
    public ItemStack saveEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
        if ( !isValidBall(stack) || isFilledBall(stack) )
            return ItemStack.EMPTY;

        if ( !(entity instanceof EntityLiving) || !entity.isEntityAlive() || !entity.isNonBoss() )
            return ItemStack.EMPTY;

        ResourceLocation key = EntityList.getKey(entity);
        if ( key == null || EntityUtilities.isBlacklisted(key) )
            return ItemStack.EMPTY;

        ItemStack out = stack.copy();
        if ( out.getCount() > 1 )
            out.setCount(1);

        NBTTagCompound tag = out.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        NBTTagCompound entityTag = new NBTTagCompound();
        entity.writeToNBT(entityTag);

        for (String badTag : EntityUtilities.BAD_TAGS)
            entityTag.removeTag(badTag);

        tag.setString("EntityID", key.toString());
        tag.setTag("EntityData", entityTag);

        out.setItemDamage(EntityList.getID(entity.getClass()));
        out.setTagCompound(tag);

        return out;
    }

    @Nonnull
    public ItemStack removeEntity(@Nonnull ItemStack stack) {
        if ( !isValidBall(stack) )
            return ItemStack.EMPTY;

        ItemStack out = stack.copy();
        NBTTagCompound tag = out.getTagCompound();
        if ( tag != null ) {
            tag.removeTag("EntityID");
            tag.removeTag("EntityData");

            if ( tag.isEmpty() )
                tag = null;
        }

        out.setTagCompound(tag);
        out.setItemDamage(0);
        return out;
    }

    public boolean canFillBall(@Nonnull ItemStack stack) {
        return isValidBall(stack) && !isFilledBall(stack);
    }

    public boolean canEmptyBall(@Nonnull ItemStack stack) {
        return isFilledBall(stack);
    }
}

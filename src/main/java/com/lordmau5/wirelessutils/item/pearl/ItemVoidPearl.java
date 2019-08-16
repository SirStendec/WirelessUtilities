package com.lordmau5.wirelessutils.item.pearl;

import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.pearl.EntityVoidPearl;
import com.lordmau5.wirelessutils.item.base.IDimensionallyStableItem;
import com.lordmau5.wirelessutils.item.base.IJEIInformationItem;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModStatistics;
import mezz.jei.api.IModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemVoidPearl extends ItemBasePearl implements IDimensionallyStableItem, EntityUtilities.IEntityBall {

    public ItemVoidPearl() {
        super();

        setName("void_pearl");

        addPropertyOverride(
                new ResourceLocation("filled"),
                (stack, worldIn, entityIn) -> {
                    Item item = stack.getItem();
                    if ( item instanceof ItemVoidPearl )
                        return ((ItemVoidPearl) item).isFilledBall(stack) ? 1F : 0F;

                    return 0F;
                });

        EntityUtilities.registerHandler(this, this);
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
                    getTranslationKey() + ".contains",
                    getCapturedName(stack)
            ).getFormattedText());

            float health = getCapturedHealth(stack);
            if ( health > 0 )
                tooltip.add(new TextComponentTranslation(
                        getTranslationKey() + ".health",
                        health
                ).getFormattedText());

            int exp = getBaseExperience(stack);
            if ( isBabyEntity(stack) )
                exp = (int) Math.floor(exp * ModConfig.vaporizers.babyMultiplier);

            if ( exp != 0 )
                tooltip.add(new TextComponentTranslation(
                        getTranslationKey() + ".value",
                        StringHelper.formatNumber(exp)
                ).getFormattedText());

        } else
            tooltip.add(new TextComponentTranslation(
                    getTranslationKey() + ".empty"
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
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if ( entity.world.isRemote || isFilledBall(stack) )
            return false;

        ItemStack out = saveEntity(stack, entity);
        if ( out.isEmpty() )
            return false;

        entity.setDead();
        player.swingArm(hand);
        player.getCooldownTracker().setCooldown(this, 5);

        if ( player instanceof EntityPlayerMP ) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            ModAdvancements.FOR_THEE.trigger(playerMP);
            playerMP.addStat(ModStatistics.CAPTURED_MOBS);
        }

        if ( entity.world instanceof WorldServer )
            entity.world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.NEUTRAL, .2F, .2F);

        if ( stack.getCount() == 1 )
            player.setHeldItem(hand, out);
        else {
            stack.shrink(1);
            player.setHeldItem(hand, stack);
            if ( !player.addItemStackToInventory(out) )
                CoreUtils.dropItemStackIntoWorldWithVelocity(out, player.world, player.getPositionVector());
        }

        return true;
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
    public boolean onEntityItemUpdate(EntityItem entity) {
        if ( entity.world != null && entity.world.isRemote && isFilledBall(entity.getItem()) ) {
            if ( entity.world.rand.nextFloat() > 0.92 ) {
                float offsetX = entity.world.rand.nextFloat() * 0.4F - 0.2F;
                float offsetY = entity.world.rand.nextFloat() * 0.4F + 0.4F;
                float offsetZ = entity.world.rand.nextFloat() * 0.4F - 0.2F;

                entity.world.spawnParticle(EnumParticleTypes.END_ROD, entity.posX + offsetX, entity.posY + offsetY, entity.posZ + offsetZ, 0, 0.005F, 0);
            }
        }

        return super.onEntityItemUpdate(entity);
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityVoidPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityVoidPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityVoidPearl(worldIn, stack);
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
                    getTranslationKey() + ".baby",
                    out
            );

        return out;
    }

    @Override
    public boolean shouldStackShrink(@Nonnull ItemStack stack, EntityPlayer player) {
        return isFilledBall(stack) || super.shouldStackShrink(stack, player);
    }

    @Nonnull
    public ItemStack releaseEntity(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Vec3d pos) {
        Entity entity = getEntity(stack, world, true);
        if ( entity == null )
            return ItemStack.EMPTY;

        entity.setPosition(pos.x, pos.y, pos.z);

        entity.motionX = 0;
        entity.motionY = 0;
        entity.motionZ = 0;

        entity.setUniqueId(UUID.randomUUID());
        world.spawnEntity(entity);

        if ( entity instanceof EntityLiving )
            ((EntityLiving) entity).playLivingSound();

        if ( stack.getCount() > 1 ) {
            stack = stack.copy();
            stack.setCount(1);
        }

        return removeEntity(stack);
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
}

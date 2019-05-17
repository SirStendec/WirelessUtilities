package com.lordmau5.wirelessutils.item.pearl;

import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.pearl.EntityVoidPearl;
import com.lordmau5.wirelessutils.item.base.IDimensionallyStableItem;
import com.lordmau5.wirelessutils.item.base.IJEIInformationItem;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
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
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemVoidPearl extends ItemBasePearl implements IDimensionallyStableItem {

    public ItemVoidPearl() {
        super();

        setName("void_pearl");

        addPropertyOverride(
                new ResourceLocation("filled"),
                (stack, worldIn, entityIn) -> {
                    Item item = stack.getItem();
                    if ( item instanceof ItemVoidPearl )
                        return ((ItemVoidPearl) item).containsEntity(stack) ? 1F : 0F;

                    return 0F;
                });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
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
        if ( containsEntity(stack) ) {
            tooltip.add(StringHelper.localize(getTranslationKey() + ".contains"));
            tooltip.add(new TextComponentTranslation(
                    getTranslationKey() + ".entry",
                    getCapturedName(stack)
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

        if ( containsEntity(stack) ) {
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
        if ( entity.world.isRemote || containsEntity(stack) )
            return false;

        ItemStack out = captureEntity(stack, entity);
        if ( out.isEmpty() )
            return false;

        player.getCooldownTracker().setCooldown(this, 5);

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
        if ( block == Blocks.END_PORTAL )
            entity.setVelocity(entity.motionX, .2, entity.motionZ);
        else if ( block == Blocks.END_GATEWAY )
            entity.setVelocity(-entity.motionX, -entity.motionY, -entity.motionZ);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entity) {
        if ( entity.world != null && entity.world.isRemote && containsEntity(entity.getItem()) ) {
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


    public boolean containsEntity(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this || !stack.hasTagCompound() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("EntityID");
    }

    public ITextComponent getCapturedName(@Nonnull ItemStack stack) {
        if ( !containsEntity(stack) )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        String name = EntityList.getTranslationName(new ResourceLocation(tag.getString("EntityID")));
        if ( name == null )
            return null;

        String key = "entity." + name + ".name";
        if ( StringHelper.canLocalize(key) )
            return new TextComponentTranslation(key);

        else if ( StringHelper.canLocalize(name) )
            return new TextComponentTranslation(name);

        return new TextComponentString(name);
    }

    @Nullable
    public Entity getCapturedEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData) {
        if ( !containsEntity(stack) )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(tag.getString("EntityID")), world);
        if ( entity != null && withData )
            entity.readFromNBT(tag.getCompoundTag("EntityData"));

        return entity;
    }

    @Override
    public boolean shouldStackShrink(@Nonnull ItemStack stack, EntityPlayer player) {
        return containsEntity(stack) || super.shouldStackShrink(stack, player);
    }

    @Nonnull
    public ItemStack releaseEntity(@Nonnull ItemStack stack, @Nonnull World world, Vec3d pos) {
        Entity entity = getCapturedEntity(stack, world, true);
        if ( entity == null )
            return ItemStack.EMPTY;

        entity.setPosition(pos.x, pos.y, pos.z);
        entity.setVelocity(0, 0, 0);
        entity.setUniqueId(UUID.randomUUID());

        world.spawnEntity(entity);

        if ( entity instanceof EntityLiving )
            ((EntityLiving) entity).playLivingSound();

        ItemStack out = stack.copy();
        if ( out.getCount() > 1 )
            out.setCount(1);

        NBTTagCompound tag = out.getTagCompound();
        if ( tag != null ) {
            tag.removeTag("EntityID");
            tag.removeTag("EntityData");

            // Clear empty tags to allow better stacking.
            if ( tag.isEmpty() )
                tag = null;

            out.setTagCompound(tag);
        }

        return out;
    }

    @Nonnull
    public ItemStack captureEntity(@Nonnull ItemStack stack, @Nonnull Entity entity) {
        if ( stack.isEmpty() || stack.getItem() != this || containsEntity(stack) )
            return ItemStack.EMPTY;

        if ( !(entity instanceof EntityLiving) || !entity.isEntityAlive() || !entity.isNonBoss() )
            return ItemStack.EMPTY;

        ResourceLocation key = EntityList.getKey(entity);
        if ( key == null )
            return ItemStack.EMPTY;

        String name = key.toString();
        if ( isBlacklisted(name) )
            return ItemStack.EMPTY;

        ItemStack out = stack.copy();
        if ( out.getCount() > 1 )
            out.setCount(1);

        NBTTagCompound tag = out.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        NBTTagCompound entityTag = new NBTTagCompound();
        entity.writeToNBT(entityTag);

        tag.setString("EntityID", name);
        tag.setTag("EntityData", entityTag);

        out.setTagCompound(tag);
        entity.setDead();

        return out;
    }

    public static boolean isBlacklisted(String key) {
        if ( key == null )
            return false;

        key = key.toLowerCase();
        for (String listed : ModConfig.items.voidPearl.blacklist)
            if ( listed.equals(key) )
                return true;

        return false;
    }
}

package com.lordmau5.wirelessutils.item.pearl;

import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.entity.pearl.EntityVoidPearl;
import com.lordmau5.wirelessutils.item.base.ItemBaseVoidPearl;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModStats;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ItemVoidPearl extends ItemBaseVoidPearl {

    public ItemVoidPearl() {
        super();
        setName("void_pearl");
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        if ( entity.world.isRemote || isFilledBall(stack) )
            return false;

        ItemStack out = saveEntity(stack, entity, player);
        if ( out.isEmpty() )
            return false;

        entity.setDead();
        player.swingArm(hand);
        player.getCooldownTracker().setCooldown(this, 5);

        if ( player instanceof EntityPlayerMP ) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            ModAdvancements.FOR_THEE.trigger(playerMP);
            ModStats.CapturedEntities.addToPlayer(playerMP);
        }

        if ( entity.world instanceof WorldServer ) {
            WorldServer ws = (WorldServer) entity.world;
            ws.playSound(null, entity.getPosition(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.NEUTRAL, .2F, .2F);
            AxisAlignedBB box = entity.getRenderBoundingBox();

            double centerX = entity.posX + (box.maxX - box.minX) / 2;
            double centerY = entity.posY + (box.maxY - box.minY) / 2;
            double centerZ = entity.posZ + (box.maxZ - box.minZ) / 2;

            ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, centerX, centerY, centerZ, 3, .2D, .2D, .2D, 0D);
        }

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

    @Nonnull
    public ItemStack releaseEntity(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Vec3d pos, @Nullable EntityPlayer player) {
        Entity entity = getEntity(stack, world, true, player);
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
}

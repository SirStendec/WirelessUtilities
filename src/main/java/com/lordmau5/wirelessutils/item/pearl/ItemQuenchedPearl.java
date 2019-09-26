package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.pearl.EntityQuenchedPearl;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemQuenchedPearl extends ItemBasePearl {

    public ItemQuenchedPearl() {
        super();
        setName("quenched_pearl");
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entity) {
        World world = entity.getEntityWorld();
        if ( world == null )
            return false;

        if ( world.isRemote ) {
            int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
            if ( setting != 2 ) {
                if ( world.rand.nextFloat() < (setting == 0 ? 0.1F : 0.04F) )
                    world.spawnParticle(EnumParticleTypes.WATER_DROP, entity.posX, entity.posY + 0.5D, entity.posZ, 0, 0, 0);
            }
            return false;
        }

        AxisAlignedBB box = entity.getEntityBoundingBox();

        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(
                (int) (box.minX + entity.motionX), (int) (box.minY + entity.motionY), (int) (box.minZ + entity.motionZ),
                (int) (box.maxX + entity.motionX), (int) (box.maxY + entity.motionY), (int) (box.maxZ + entity.motionZ)
        )) {
            if ( world.isBlockLoaded(pos) ) {
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if ( block instanceof BlockFire ) {
                    world.setBlockToAir(pos);
                    world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                }
            }
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

        if ( ModConfig.items.quenchedPearl.douseEntities )
            entityIn.extinguish();
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityQuenchedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityQuenchedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityQuenchedPearl(worldIn, stack);
    }
}

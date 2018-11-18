package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.entity.pearl.EntityChargedPearl;
import com.lordmau5.wirelessutils.item.base.IGrowableItem;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;

import javax.annotation.Nonnull;

public class ItemChargedPearl extends ItemBasePearl implements IGrowableItem {
    public ItemChargedPearl() {
        super();

        setName("charged_pearl");
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if ( !ModConfig.items.chargedPearl.enableScorching )
            return false;

        BlockPos nextPos = new BlockPos(entityItem.posX + entityItem.motionX, entityItem.posY + entityItem.motionY, entityItem.posZ + entityItem.motionZ);
        World world = entityItem.getEntityWorld();
        if ( world == null || world.isRemote )
            return false;

        IBlockState state = world.getBlockState(nextPos);
        if ( state.getMaterial() != Material.LAVA )
            return false;

        IFluidHandler handler = new BlockLiquidWrapper((BlockLiquid) state.getBlock(), world, nextPos);
        IFluidTankProperties[] properties = handler.getTankProperties();
        if ( properties == null || properties.length != 1 )
            return false;

        FluidStack fluid = properties[0].getContents();
        if ( fluid == null || fluid.getFluid() != FluidRegistry.LAVA || fluid.amount != properties[0].getCapacity() )
            return false;

        ItemStack stack = entityItem.getItem();
        ItemStack newStack = new ItemStack(ModItems.itemScorchedPearl, 1, stack.getMetadata());

        entityItem.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
        if ( world instanceof WorldServer ) {
            WorldServer ws = (WorldServer) world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, entityItem.posX, entityItem.posY, entityItem.posZ, 3, .2D, .2D, .2D, 0D);
        }

        EntityPlayer player = world.getPlayerEntityByName(entityItem.getThrower());
        if ( player instanceof EntityPlayerMP )
            ModAdvancements.SO_HOT_RIGHT_NOW.trigger((EntityPlayerMP) player);

        world.setBlockToAir(nextPos);
        if ( stack.getCount() > 1 ) {
            world.spawnEntity(new EntityItemEnhanced(world, entityItem.posX, entityItem.posY, entityItem.posZ, newStack));
            stack.shrink(1);

        } else
            entityItem.setItem(newStack);

        return true;
    }

    public void growthUpdate(@Nonnull ItemStack stack, @Nonnull EntityItemEnhanced entity) {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.getPosition();
        if ( !world.isBlockLoaded(pos) || world.isRemote || entity.isDead )
            return;

        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();

        boolean lava = false;
        if ( ModConfig.items.chargedPearl.enableScorching && material == Material.LAVA )
            lava = true;

        boolean water = false;
        if ( ModConfig.items.chargedPearl.enableQuenching && material == Material.WATER )
            water = true;

        if ( !lava && !water )
            return;

        IFluidHandler handler = new BlockLiquidWrapper((BlockLiquid) state.getBlock(), world, pos);
        IFluidTankProperties[] properties = handler.getTankProperties();
        if ( properties == null || properties.length != 1 )
            return;

        FluidStack fluid = properties[0].getContents();
        if ( fluid == null || fluid.amount != properties[0].getCapacity() )
            return;

        Item newItem;
        EntityPlayer player = world.getPlayerEntityByName(entity.getThrower());

        if ( water && fluid.getFluid() == FluidRegistry.WATER ) {
            newItem = ModItems.itemQuenchedPearl;
            entity.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
            if ( world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) world;
                ws.spawnParticle(EnumParticleTypes.WATER_SPLASH, entity.posX, entity.posY, entity.posZ, 3, .2D, .2D, .2D, 0D);
            }

            if ( player instanceof EntityPlayerMP )
                ModAdvancements.REFRESHING.trigger((EntityPlayerMP) player);

        } else if ( lava && fluid.getFluid() == FluidRegistry.LAVA ) {
            newItem = ModItems.itemScorchedPearl;
            entity.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
            if ( world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) world;
                ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, entity.posX, entity.posY, entity.posZ, 3, .2D, .2D, .2D, 0D);
            }

            if ( player instanceof EntityPlayerMP )
                ModAdvancements.SO_HOT_RIGHT_NOW.trigger((EntityPlayerMP) player);

        } else
            return;

        ItemStack newStack = new ItemStack(newItem, 1, stack.getMetadata());

        world.setBlockToAir(pos);
        if ( stack.getCount() > 1 ) {
            world.spawnEntity(new EntityItemEnhanced(world, entity.posX, entity.posY, entity.posZ, newStack));
            stack.shrink(1);

        } else
            entity.setItem(newStack);
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, EntityPlayer playerIn, IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityChargedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityChargedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityChargedPearl(worldIn, stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}

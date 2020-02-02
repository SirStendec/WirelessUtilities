package com.lordmau5.wirelessutils.entity.pearl;

import cofh.core.util.helpers.InventoryHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderEncapsulatedItem;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityEncapsulatedItem extends EntityBaseThrowable {

    public static final Predicate<Entity> HIT_PREDICATE = Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith);
    private ItemStack held;

    public EntityEncapsulatedItem(World world) {
        super(world);
    }

    public EntityEncapsulatedItem(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityEncapsulatedItem(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityEncapsulatedItem(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityEncapsulatedItem(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityEncapsulatedItem(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    public void setStack(@Nonnull ItemStack stack) {
        super.setStack(stack);
        held = null;
    }

    @Nonnull
    public ItemStack getHeldStack() {
        if ( held == null )
            held = ModItems.itemEncapsulatedItem.getContainedItem(getStack());
        return held;
    }

    @Nonnull
    @Override
    public ItemStack getRenderStack() {
        return getHeldStack();
    }

    @Nullable
    @Override
    public Predicate<? super Entity> getEntityPredicate() {
        return HIT_PREDICATE;
    }

    @Override
    public boolean shouldHitEntities() {
        return true;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if ( !world.isRemote ) {
            if ( world instanceof WorldServer )
                ((WorldServer) world).spawnParticle(EnumParticleTypes.SLIME, posX, posY, posZ, 4, 0D, 0D, 0D, 0);

            ItemStack stack = getHeldStack();

            if ( result.typeOfHit == RayTraceResult.Type.BLOCK ) {
                BlockPos pos = result.getBlockPos();
                TileEntity tile = world.getTileEntity(pos);
                if ( InventoryHelper.hasItemHandlerCap(tile, result.sideHit) ) {
                    IItemHandler handler = InventoryHelper.getItemHandlerCap(tile, result.sideHit);
                    if ( handler != null )
                        stack = InventoryHelper.insertStackIntoInventory(handler, stack, false);
                }
            }

            if ( !stack.isEmpty() )
                dropItemStack(stack, true);

            playSound(SoundEvents.BLOCK_SLIME_FALL, 0.1F, 1F);
        }

        setDead();
    }

    public static class Factory implements IRenderFactory<EntityEncapsulatedItem> {
        @Override
        public Render<? super EntityEncapsulatedItem> createRenderFor(RenderManager manager) {
            return new RenderEncapsulatedItem(manager, Minecraft.getMinecraft().getRenderItem());
        }
    }

}

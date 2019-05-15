package com.lordmau5.wirelessutils.entity;

import com.lordmau5.wirelessutils.item.base.IDamageableItem;
import com.lordmau5.wirelessutils.item.base.IDimensionallyStableItem;
import com.lordmau5.wirelessutils.item.base.IGrowableItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;

public class EntityItemEnhanced extends EntityItem {
    public EntityItemEnhanced(World world) {
        super(world);
        isImmuneToFire = true;
    }

    public EntityItemEnhanced(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
        isImmuneToFire = true;
    }

    public EntityItemEnhanced(EntityItem toConvert) {
        this(toConvert.getEntityWorld());
        readFromNBT(toConvert.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if ( source == DamageSource.IN_FIRE )
            return true;

        return super.isEntityInvulnerable(source);
    }

    @Override
    public boolean attackEntityFrom(@Nullable DamageSource source, float amount) {
        if ( !world.isRemote && !isDead ) {
            ItemStack stack = getItem();
            if ( !stack.isEmpty() ) {
                Item item = stack.getItem();
                if ( item instanceof IDamageableItem ) {
                    if ( !((IDamageableItem) item).shouldItemTakeDamage(this, stack, source, amount) )
                        return false;

                } else {
                    Block block = Block.getBlockFromItem(item);
                    if ( block instanceof IDamageableItem ) {
                        if ( !((IDamageableItem) block).shouldItemTakeDamage(this, stack, source, amount) )
                            return false;
                    }
                }
            }
        }

        return super.attackEntityFrom(source, amount);
    }

    @Nullable
    @Override
    public Entity changeDimension(int dimensionIn, ITeleporter teleporter) {
        if ( shouldChangeDimension() )
            return super.changeDimension(dimensionIn, teleporter);

        // We don't want to change dimensions.
        return null;
    }

    public boolean shouldChangeDimension() {
        ItemStack stack = getItem();
        Item item = stack.getItem();

        return (!(item instanceof IDimensionallyStableItem) || ((IDimensionallyStableItem) item).allowDimensionalTravel());
    }

    @Override
    protected void onInsideBlock(IBlockState state) {
        super.onInsideBlock(state);
        ItemStack stack = getItem();
        Item item = stack.getItem();

        if ( item instanceof IDimensionallyStableItem ) {
            Block block = state.getBlock();
            if ( block == Blocks.END_GATEWAY || block == Blocks.END_PORTAL )
                ((IDimensionallyStableItem) item).onPortalImpact(stack, this, state);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        ItemStack stack = getItem();
        if ( stack.isEmpty() )
            return;

        Item item = stack.getItem();
        if ( item instanceof IGrowableItem ) {
            IGrowableItem grow = (IGrowableItem) item;
            grow.growthUpdate(stack, this);
        } else {
            Block block = Block.getBlockFromItem(item);
            if ( block instanceof IGrowableItem ) {
                IGrowableItem grow = (IGrowableItem) block;
                grow.growthUpdate(stack, this);
            }
        }
    }
}

package com.lordmau5.wirelessutils.entity;

import com.lordmau5.wirelessutils.item.base.IDamageableItem;
import com.lordmau5.wirelessutils.item.base.IGrowableItem;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

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
    public boolean attackEntityFrom(DamageSource source, float amount) {
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

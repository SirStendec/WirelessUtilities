package com.lordmau5.wirelessutils.render;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class RenderAsItem<T> extends RenderSnowball {

    public RenderAsItem(RenderManager manager, Item item, RenderItem renderItem) {
        super(manager, item, renderItem);
    }

    @Override
    @Nonnull
    public ItemStack getStackToRender(Entity entityIn) {
        if ( entityIn instanceof EntityBaseThrowable ) {
            ItemStack stack = ((EntityBaseThrowable) entityIn).getRenderStack();
            if ( !stack.isEmpty() )
                return stack;
        }

        return new ItemStack(item);
    }
}

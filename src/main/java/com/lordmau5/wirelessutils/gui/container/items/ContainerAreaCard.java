package com.lordmau5.wirelessutils.gui.container.items;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.item.base.ItemBaseAreaCard;
import com.lordmau5.wirelessutils.tile.base.IConfigurableRange;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerAreaCard extends BaseContainerItem implements IConfigurableRange {

    protected final ItemBaseAreaCard item;
    protected final EntityPlayer player;

    public ContainerAreaCard(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);
        item = (ItemBaseAreaCard) stack.getItem();
        player = inventory.player;
    }

    public ItemBaseAreaCard getItem() {
        return item;
    }

    /* IConfigurableRange */

    public boolean isFacingY() {
        return false;
    }

    public void saveRanges() {
        /* Ignore */
    }

    public int getRange() {
        return item.getRange(stack);
    }

    public int getRangeLength() {
        return item.getRangeLength(stack);
    }

    public int getRangeWidth() {
        return item.getRangeWidth(stack);
    }

    public int getRangeHeight() {
        return item.getRangeHeight(stack);
    }

    public void setRangeLength(int length) {
        ItemStack out = item.setRangeLength(stack, (byte) length);
        if ( !out.isEmpty() )
            setItemStack(out);
    }

    public void setRangeWidth(int width) {
        ItemStack out = item.setRangeWidth(stack, (byte) width);
        if ( !out.isEmpty() )
            setItemStack(out);
    }

    public void setRangeHeight(int height) {
        ItemStack out = item.setRangeHeight(stack, (byte) height);
        if ( !out.isEmpty() )
            setItemStack(out);
    }

    public void setRanges(int height, int length, int width) {
        ItemStack out = item.setRanges(stack, height, length, width);
        if ( !out.isEmpty() )
            setItemStack(out);
    }

    public int getOffsetHorizontal() {
        return item.getOffsetHorizontal(stack);
    }

    public int getOffsetVertical() {
        return item.getOffsetVertical(stack);
    }

    public void setOffsetHorizontal(int offset) {
        ItemStack out = item.setOffsetHorizontal(stack, (byte) offset);
        if ( !out.isEmpty() )
            setItemStack(out);
    }

    public void setOffsetVertical(int offset) {
        ItemStack out = item.setOffsetVertical(stack, (byte) offset);
        if ( !out.isEmpty() )
            setItemStack(out);
    }
}

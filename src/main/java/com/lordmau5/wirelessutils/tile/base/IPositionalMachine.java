package com.lordmau5.wirelessutils.tile.base;

import com.google.common.collect.Iterables;
import com.lordmau5.wirelessutils.item.base.ItemBaseEntityPositionalCard;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.List;

public interface IPositionalMachine {

    boolean isInterdimensional();

    int getRange();

    BlockPosDimension getPosition();

    default boolean allowEntityCards() {
        return true;
    }

    default boolean isPositionalCardValid(ItemStack stack) {
        if ( stack.isEmpty() || !(stack.getItem() instanceof ItemBasePositionalCard) )
            return false;

        ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();

        if ( !allowEntityCards() && card instanceof ItemBaseEntityPositionalCard )
            return false;

        return card.isCardConfigured(stack);
    }

    default boolean isTargetInRange(BlockPosDimension target) {
        return isTargetInRange(target, getRange(), isInterdimensional());
    }

    default boolean isTargetInRange(BlockPosDimension target, int range, boolean interdimensional) {
        if ( interdimensional )
            return true;

        BlockPosDimension origin = getPosition();
        if ( origin.getDimension() != target.getDimension() )
            return false;

        return Math.floor(Math.sqrt(origin.distanceSq(target))) <= range;
    }

    @Nullable
    static Iterable<Tuple<BlockPosDimension, ItemStack>> buildTargetIterator(List<Iterable<Tuple<BlockPosDimension, ItemStack>>> iterables) {
        if ( iterables == null || iterables.isEmpty() )
            return null;

        if ( iterables.size() == 1 )
            return iterables.get(0);

        return Iterables.concat(iterables);
    }
}

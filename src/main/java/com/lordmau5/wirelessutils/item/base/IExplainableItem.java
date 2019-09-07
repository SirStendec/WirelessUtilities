package com.lordmau5.wirelessutils.item.base;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.util.text.Style;

import javax.annotation.Nonnull;
import java.util.List;

public interface IExplainableItem {

    default void addExplanation(@Nonnull List<String> tooltip, @Nonnull String name, Object... args) {
        String path = name + ".0";
        if ( StringHelper.canLocalize(path) ) {
            if ( StringHelper.isShiftKeyDown() )
                addLocalizedLines(tooltip, name, args);
            else
                tooltip.add(StringHelper.shiftForDetails());
        }
    }

    default void addLocalizedLines(@Nonnull List<String> tooltip, @Nonnull String name, Object... args) {
        addLocalizedLines(tooltip, name, TextHelpers.GRAY, args);
    }

    default void addLocalizedLines(@Nonnull List<String> tooltip, @Nonnull String name, Style style, Object... args) {
        TextHelpers.addLocalizedLines(tooltip, name, style, args);
    }

}

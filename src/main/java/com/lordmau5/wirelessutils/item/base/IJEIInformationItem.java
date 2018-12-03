package com.lordmau5.wirelessutils.item.base;

import cofh.core.util.helpers.StringHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public interface IJEIInformationItem {

    void registerJEI(IModRegistry registry);

    static void addJEIInformation(IModRegistry registry, ItemStack stack) {
        addJEIInformation(registry, stack, stack.getTranslationKey() + ".jei");
    }

    static void addJEIInformation(IModRegistry registry, ItemStack stack, String name) {
        int i = 0;
        String path = name + "." + i;
        ArrayList<String> out = new ArrayList<>();
        while ( StringHelper.canLocalize(path) ) {
            out.add(path);
            i++;
            path = name + "." + i;
        }

        if ( !out.isEmpty() )
            registry.addIngredientInfo(stack, VanillaTypes.ITEM, out.toArray(new String[0]));
    }

}

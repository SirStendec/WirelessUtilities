package com.lordmau5.wirelessutils.utils.crafting;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreIngredient;

import java.util.function.BooleanSupplier;

public class ConditionOreExists implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        OreIngredient ingredient = new OreIngredient(JsonUtils.getString(json, "ore"));
        return () -> ingredient.getMatchingStacks().length > 0;
    }
}

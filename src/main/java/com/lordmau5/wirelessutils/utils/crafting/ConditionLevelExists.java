package com.lordmau5.wirelessutils.utils.crafting;

import com.google.gson.JsonObject;
import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class ConditionLevelExists implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        int level = JsonUtils.getInt(json, "level");
        return () -> level >= 0 && level < Level.values().length;
    }
}

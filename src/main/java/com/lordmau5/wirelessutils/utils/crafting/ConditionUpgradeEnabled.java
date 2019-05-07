package com.lordmau5.wirelessutils.utils.crafting;

import com.google.gson.JsonObject;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class ConditionUpgradeEnabled implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
        return () -> ModConfig.upgrades.allowCrafting;
    }
}

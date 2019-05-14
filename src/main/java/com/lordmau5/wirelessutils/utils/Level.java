package com.lordmau5.wirelessutils.utils;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;

public class Level {
    private static final ArrayList<Level> levels = new ArrayList<>();

    static {
        addLevel(new Level(1, EnumRarity.COMMON, 0xFFFFFF, 5000, 100000, 10, 5, (byte) 30, 5000, 25, 4000, 4, 4, 1));
        addLevel(new Level(2, EnumRarity.COMMON, 0xFF0000, 10000, 200000, 15, 10, (byte) 20, 10000, 100, 16000, 8, 8, 1));
        addLevel(new Level(3, EnumRarity.UNCOMMON, 0xFFFF00, 25000, 500000, 30, 20, (byte) 15, 25000, 250, 40000, 16, 16, 1));
        addLevel(new Level(4, EnumRarity.UNCOMMON, 0x00FF00, 100000, 2000000, 60, 50, (byte) 10, 50000, 1000, 160000, 32, 32, 1));
        addLevel(new Level(6, EnumRarity.RARE, 0x00FFFF, 1000000, 20000000, 120, 100, (byte) 5, 200000, 16000, 2560000, 64, 64, 1));
        addLevel(new Level(9, EnumRarity.EPIC, 0xFF00FF, Long.MAX_VALUE, Long.MAX_VALUE, Integer.MAX_VALUE, 5, (byte) 0, Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 1, true));
    }

    public static Level getMinLevel() {
        return levels.get(0);
    }

    public static Level getMaxLevel() {
        return levels.get(levels.size() - 1);
    }

    public static boolean addLevel(Level level) {
        if ( levels.size() >= Byte.MAX_VALUE )
            return false;

        return levels.add(level);
    }

    public static void clearLevels() {
        levels.clear();
    }

    public static boolean removeLevel(Level level) {
        return levels.remove(level);
    }

    public static boolean insertLevel(int index, Level level) {
        if ( levels.size() >= Byte.MAX_VALUE )
            return false;

        levels.add(index, level);
        return true;
    }

    public static Level getLevel(int index) {
        return levels.get(index);
    }

    public static Level fromAugment(ItemStack stack) {
        if ( !stack.isEmpty() && stack.getItem() instanceof ItemAugment )
            return fromInt(stack.getMetadata());

        return Level.getMinLevel();
    }

    public static Level fromItemStack(ItemStack stack) {
        if ( stack == null || stack.isEmpty() )
            return Level.getMinLevel();

//        // Preparation for 1.13
//        if ( stack.hasTagCompound() ) {
//            NBTTagCompound tag = stack.getTagCompound();
//            if ( tag != null && tag.hasKey("Level") ) {
//                return Level.getLevel(tag.getByte("Level"));
//            }
//        }
        return Level.fromInt(stack.getMetadata());
    }

    public static Level[] values() {
        return levels.toArray(new Level[0]);
    }

    public static Level fromInt(int level) {
        if ( level < 0 )
            level = 0;

        if ( level >= levels.size() )
            level = levels.size() - 1;

        return levels.get(level);
    }

    public static int toInt(Level level) {
        return levels.indexOf(level);
    }

    // The Class Now
    public String name;

    public int augmentSlots;
    public EnumRarity rarity;
    public boolean isCreative;
    public int color;

    // Chargers
    public long maxChargerCapacity;
    public long maxChargerTransfer;
    public int craftingTPT;

    // Other Machines
    public long maxEnergyCapacity;
    public int baseEnergyPerOperation;
    public byte gatherTicks;

    // Condensers
    public int maxCondenserCapacity;
    public int maxCondenserTransfer;

    // Desublimators
    public int budgetPerTick;
    public int maxBudget;
    public int costPerItem;

    public Level(int augmentSlots, EnumRarity rarity, int color, long maxChargerTransfer,
                 long maxChargerCapacity, int craftingTPT, int baseEnergyPerOperation, byte gatherTicks,
                 long maxEnergyCapacity, int maxCondenserTransfer, int maxCondenserCapacity, int budgetPerTick, int maxBudget, int costPerItem) {
        this(null, augmentSlots, rarity, color, maxChargerTransfer, maxChargerCapacity, craftingTPT, baseEnergyPerOperation, gatherTicks, maxEnergyCapacity, maxCondenserTransfer, maxCondenserCapacity, budgetPerTick, maxBudget, costPerItem, false);
    }

    public Level(String name, int augmentSlots, EnumRarity rarity, int color, long maxChargerTransfer,
                 long maxChargerCapacity, int craftingTPT, int baseEnergyPerOperation, byte gatherTicks,
                 long maxEnergyCapacity, int maxCondenserTransfer, int maxCondenserCapacity, int budgetPerTick, int maxBudget, int costPerItem) {
        this(name, augmentSlots, rarity, color, maxChargerTransfer, maxChargerCapacity, craftingTPT, baseEnergyPerOperation, gatherTicks, maxEnergyCapacity, maxCondenserTransfer, maxCondenserCapacity, budgetPerTick, maxBudget, costPerItem, false);
    }

    public Level(int augmentSlots, EnumRarity rarity, int color, long maxChargerTransfer,
                 long maxChargerCapacity, int craftingTPT, int baseEnergyPerOperation, byte gatherTicks, long maxEnergyCapacity,
                 int maxCondenserTransfer, int maxCondenserCapacity, int budgetPerTick, int maxBudget, int costPerItem, boolean isCreative) {
        this(null, augmentSlots, rarity, color, maxChargerTransfer, maxChargerCapacity, craftingTPT, baseEnergyPerOperation, gatherTicks, maxEnergyCapacity, maxCondenserTransfer, maxCondenserCapacity, budgetPerTick, maxBudget, costPerItem, isCreative);
    }

    public Level(String name, int augmentSlots, EnumRarity rarity, int color, long maxChargerTransfer,
                 long maxChargerCapacity, int craftingTPT, int baseEnergyPerOperation, byte gatherTicks,
                 long maxEnergyCapacity, int maxCondenserTransfer, int maxCondenserCapacity,
                 int budgetPerTick, int maxBudget, int costPerItem, boolean isCreative) {
        this.name = name;
        this.augmentSlots = augmentSlots;
        this.rarity = rarity;
        this.isCreative = isCreative;
        this.color = color;
        this.maxChargerCapacity = maxChargerCapacity;
        this.maxChargerTransfer = maxChargerTransfer;
        this.craftingTPT = craftingTPT;
        this.maxEnergyCapacity = maxEnergyCapacity;
        this.baseEnergyPerOperation = baseEnergyPerOperation;
        this.gatherTicks = gatherTicks;
        this.maxCondenserTransfer = maxCondenserTransfer;
        this.maxCondenserCapacity = maxCondenserCapacity;
        this.budgetPerTick = budgetPerTick;
        this.maxBudget = maxBudget;
        this.costPerItem = costPerItem;
    }

    public int ordinal() {
        return toInt();
    }

    public int toInt() {
        return toInt(this);
    }

    public ITextComponent getTextComponent() {
        return new TextComponentString(getName()).setStyle(TextHelpers.getStyle(rarity.color));
    }

    public String getName() {
        if ( name != null )
            return name;

        String key = "info." + WirelessUtils.MODID + ".tiered.level." + (isCreative ? "creative" : toInt());
        if ( StringHelper.canLocalize(key) )
            return StringHelper.localize(key);

        return new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.tier",
                toInt() + 1
        ).getUnformattedText();
    }
}

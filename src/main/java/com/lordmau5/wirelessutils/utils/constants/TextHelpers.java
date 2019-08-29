package com.lordmau5.wirelessutils.utils.constants;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TextHelpers {
    public static final Style GRAY = new Style().setColor(TextFormatting.GRAY);
    public static final Style WHITE = new Style().setColor(TextFormatting.WHITE);
    public static final Style RED = new Style().setColor(TextFormatting.RED);
    public static final Style GOLD = new Style().setColor(TextFormatting.GOLD);
    public static final Style GREEN = new Style().setColor(TextFormatting.GREEN);
    public static final Style YELLOW = new Style().setColor(TextFormatting.YELLOW);
    public static final Style BLACK = new Style().setColor(TextFormatting.BLACK);

    public static final Style ITALIC = new Style().setItalic(true);

    public static Style getStyle(TextFormatting color) {
        return new Style().setColor(color);
    }

    public static Style getStyle(TextFormatting color, boolean obfuscated) {
        return getStyle(color).setObfuscated(obfuscated);
    }

    public static Style getStyle(TextFormatting color, boolean obfuscated, boolean italic) {
        return getStyle(color, obfuscated).setItalic(italic);
    }

    public static Style getStyle(TextFormatting color, boolean obfuscated, boolean italic, boolean bold) {
        return getStyle(color, obfuscated, italic).setBold(bold);
    }

    public static ITextComponent getComponent(int value) {
        return getComponent(StringHelper.formatNumber(value));
    }

    public static ITextComponent getComponent(String value) {
        return new TextComponentString(value).setStyle(WHITE);
    }

    public static String formatRelative(long number) {
        return (number < 0 ? "" : "+") + StringHelper.formatNumber(number);
    }

    public static String getScaledNumber(long number, String postfix, boolean si) {
        int unit = si ? 1000 : 1024;
        if ( number < unit )
            return "" + number;

        int exp = (int) (Math.log(number) / Math.log(unit));
        String post = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %s%s", number / Math.pow(unit, exp), post, postfix);
    }

    @Nullable
    public static ITextComponent getModifier(double multiplier, int addition) {
        ITextComponent out = null;
        if ( multiplier != 1 )
            out = new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".modifier.multiplier",
                    String.format("%.2f", multiplier)
            );

        if ( addition != 0 && out != null ) {
            return new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".modifier.combiner",
                    out,
                    new TextComponentTranslation(
                            "info." + WirelessUtils.MODID + ".modifier." + (addition > 0 ? "addition" : "subtraction"),
                            StringHelper.isShiftKeyDown() ? StringHelper.formatNumber(Math.abs(addition)) : getScaledNumber(Math.abs(addition), "", true)
                    )
            );
        } else if ( addition != 0 )
            return new TextComponentString(StringHelper.isShiftKeyDown() ? StringHelper.formatNumber(addition) : getScaledNumber(addition, "", true));

        return out;
    }


    @Nullable
    public static String[] getLocalizedLines(@Nonnull String base) {
        int i = 0;
        String key = base + "." + i;

        while ( StringHelper.canLocalize(key) ) {
            i++;
            key = base + "." + i;
        }

        if ( i == 0 )
            return null;

        int total = 0;
        int blank = 0;
        String[] out = new String[i];
        for (int j = 0; j < i; j++) {
            String line = StringHelper.localize(base + "." + j);
            out[j] = line;
            if ( line.isEmpty() )
                blank++;
            else {
                total += blank;
                blank = 0;
                total++;
            }
        }

        if ( total < i ) {
            String[] realOut = new String[total];
            System.arraycopy(out, 0, realOut, 0, total);
            return realOut;
        }

        return out;
    }

}

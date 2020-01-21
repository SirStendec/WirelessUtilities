package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum BusTransferMode {
    DISABLED(false, false),
    IMPORT(true, false),
    EXPORT(false, true),
    BOTH(true, true);

    public final boolean _import;
    public final boolean _export;

    BusTransferMode(boolean _import, boolean _export) {
        this._import = _import;
        this._export = _export;
    }

    public BusTransferMode next() {
        return byIndex(ordinal() + 1);
    }

    public BusTransferMode previous() {
        return byIndex(ordinal() - 1);
    }

    public ITextComponent getComponent() {
        return new TextComponentTranslation("btn." + WirelessUtils.MODID + ".bus_mode." + ordinal());
    }

    public BusTransferMode withImport(boolean enabled) {
        if ( enabled ) {
            switch (this) {
                case IMPORT:
                case DISABLED:
                    return IMPORT;
                case EXPORT:
                case BOTH:
                default:
                    return BOTH;
            }
        } else {
            switch (this) {
                case DISABLED:
                case IMPORT:
                    return DISABLED;
                case EXPORT:
                case BOTH:
                default:
                    return EXPORT;
            }
        }
    }

    public BusTransferMode withExport(boolean enabled) {
        if ( enabled ) {
            switch (this) {
                case EXPORT:
                case DISABLED:
                    return EXPORT;
                case IMPORT:
                case BOTH:
                default:
                    return BOTH;
            }
        } else {
            switch (this) {
                case DISABLED:
                case EXPORT:
                    return DISABLED;
                case IMPORT:
                case BOTH:
                default:
                    return IMPORT;
            }
        }
    }

    public static final BusTransferMode[] VALUES;

    public static BusTransferMode byIndex(int index) {
        while ( index < 0 )
            index += VALUES.length;

        return VALUES[index % VALUES.length];
    }

    static {
        VALUES = values();
    }
}

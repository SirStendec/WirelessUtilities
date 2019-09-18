package com.lordmau5.wirelessutils.render.particles;

public enum WUParticleTypes {

    LINE;

    public static WUParticleTypes byIndex(int index) {
        WUParticleTypes[] values = values();
        if ( index < 0 )
            index = 0;

        return values[index % values.length];
    }

}

package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * What a Landscape keeps after Heaven has attacked it and lost. Scars are history rather than
 * experience: each one is a small permanent change to how that ecology behaves.
 */
public enum HeavenScar implements StringRepresentable {
    /** Metal Qi produced from future lightning arrives cleaner. */
    LIGHTNING("lightning"),
    /** Plants tolerate brief shortages of their phase. */
    DROUGHT("drought"),
    /** One reversed Qi route loses less energy. */
    REVERSAL("reversal"),
    /** Failed fire reactions occasionally leave usable catalyst ash. */
    ASH("ash");

    /** A Landscape can only hold a few marks before older ones fade. */
    public static final int MAXIMUM_HELD = 3;

    public static final HeavenScar[] VALUES = values();
    public static final Codec<HeavenScar> CODEC = StringRepresentable.fromEnum(HeavenScar::values);

    private final String name;

    HeavenScar(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.scar." + this.name;
    }
}

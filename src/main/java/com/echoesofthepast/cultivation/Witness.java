package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The three demonstrations a mortal must give before the First Breath Ritual will take. They are
 * proofs of understanding rather than a currency: each is earned once, by doing the thing, and all
 * three are consumed together when Breath Gathering opens.
 */
public enum Witness implements StringRepresentable {
    /** Draw from a Dragon Vein and hold a connected reservoir steady without roughening it. */
    EARTH("earth"),
    /** Let a complete natural spiritual cycle run and finish without breaking or force-feeding it. */
    HEAVEN("heaven"),
    /** Put real practice into one channel of your own body. */
    SELF("self");

    public static final Witness[] VALUES = values();
    public static final Codec<Witness> CODEC = StringRepresentable.fromEnum(Witness::values);

    private final String name;

    Witness(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.witness." + this.name;
    }
}

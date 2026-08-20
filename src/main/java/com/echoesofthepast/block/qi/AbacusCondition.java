package com.echoesofthepast.block.qi;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** What the beads on a jade abacus are counting. */
public enum AbacusCondition implements StringRepresentable {
    /** Fires once the reservoir it is set on top of passes the bead count in tenths. */
    FULLNESS("fullness"),
    /** Fires after the bead count of pulses have arrived. */
    PULSES("pulses"),
    /** Fires after the bead count of tones have been heard. */
    TONES("tones"),
    /** Fires every bead count of seconds. */
    INTERVAL("interval"),
    /** Fires when the reservoir below drops under the bead count in tenths. */
    EMPTINESS("emptiness");

    public static final AbacusCondition[] VALUES = values();
    public static final Codec<AbacusCondition> CODEC = StringRepresentable.fromEnum(AbacusCondition::values);

    private final String name;

    AbacusCondition(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public AbacusCondition next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public String translationKey() {
        return "eotp.abacus." + this.name;
    }
}

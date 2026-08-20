package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * A short ladder. Each rung changes what a cultivator is <em>able to do</em> rather than handing out
 * numbers, so there is no reason to add twenty of them.
 */
public enum Realm implements StringRepresentable {
    /** No cultivation at all. Qi is invisible and artifacts are inert lumps of jade. */
    MORTAL("mortal", 0.0F, 0),
    /**
     * Breath Gathering. Qi can be sensed and pushed by hand into a device; a trickle of personal Qi
     * refills on its own.
     */
    BREATH_GATHERING("breath_gathering", 60.0F, 1),
    /**
     * Foundation. Formations answer to the cultivator, and talismans can be charged from the body
     * rather than from a reservoir.
     */
    FOUNDATION("foundation", 220.0F, 2),
    /**
     * Golden Core. Personal Qi becomes a real reservoir that persists and recharges, and bound
     * artifacts work passively.
     */
    GOLDEN_CORE("golden_core", 700.0F, 3),
    /** Nascent Spirit. The cultivator can leave the body for a while. */
    NASCENT_SPIRIT("nascent_spirit", 1800.0F, 4);

    public static final Realm[] VALUES = values();
    public static final Codec<Realm> CODEC = StringRepresentable.fromEnum(Realm::values);

    private final String name;
    private final float qiCapacity;
    private final int tier;

    Realm(String name, float qiCapacity, int tier) {
        this.name = name;
        this.qiCapacity = qiCapacity;
        this.tier = tier;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int tier() {
        return this.tier;
    }

    /** How much Qi the body can hold at this realm. */
    public float qiCapacity() {
        return this.qiCapacity;
    }

    public String translationKey() {
        return "eotp.realm." + this.name;
    }

    public Realm next() {
        return this.tier >= VALUES.length - 1 ? this : VALUES[this.tier + 1];
    }

    public boolean atLeast(Realm other) {
        return this.tier >= other.tier;
    }

    /** Formations only obey a cultivator who has laid a foundation. */
    public boolean canRunFormations() {
        return this.atLeast(FOUNDATION);
    }

    /** Below a golden core, personal Qi drains away when the cultivator stops concentrating. */
    public boolean hasPersistentQi() {
        return this.atLeast(GOLDEN_CORE);
    }

    public boolean canProject() {
        return this.atLeast(NASCENT_SPIRIT);
    }

    /** How much insight the next breakthrough needs. */
    public float insightRequired() {
        return switch (this) {
            case MORTAL -> 40.0F;
            case BREATH_GATHERING -> 160.0F;
            case FOUNDATION -> 520.0F;
            case GOLDEN_CORE -> 1400.0F;
            case NASCENT_SPIRIT -> Float.MAX_VALUE;
        };
    }

    /** Whether advancing out of this realm draws a heavenly tribulation. */
    public boolean drawsTribulation() {
        return this == FOUNDATION || this == GOLDEN_CORE;
    }

    /** Whether advancing out of this realm can call up a heart demon. */
    public boolean drawsHeartDemon() {
        return this == BREATH_GATHERING || this == FOUNDATION || this == GOLDEN_CORE;
    }
}

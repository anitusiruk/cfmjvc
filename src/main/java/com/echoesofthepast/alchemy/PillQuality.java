package com.echoesofthepast.alchemy;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Quality is never rolled. It is read off the process: how close the heat stayed to the window, how
 * well the phases balanced, whether ingredients went in in the right order, and how dirty the
 * cauldron was.
 */
public enum PillQuality implements StringRepresentable {
    CRACKED("cracked", 0.4F, 0.5F),
    ORDINARY("ordinary", 1.0F, 1.0F),
    REFINED("refined", 1.35F, 1.4F),
    PERFECT("perfect", 1.8F, 2.0F);

    public static final PillQuality[] VALUES = values();
    public static final Codec<PillQuality> CODEC = StringRepresentable.fromEnum(PillQuality::values);

    private final String name;
    private final float potency;
    private final float duration;

    PillQuality(String name, float potency, float duration) {
        this.name = name;
        this.potency = potency;
        this.duration = duration;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** Multiplier on the pill's strength. */
    public float potency() {
        return this.potency;
    }

    /** Multiplier on how long the pill's effects last. */
    public float duration() {
        return this.duration;
    }

    public String translationKey() {
        return "eotp.quality." + this.name;
    }

    /**
     * Turns a process score into a grade. The thresholds are tight at the top on purpose: perfect
     * pills should be the reward for a cauldron setup that actually controls its conditions.
     */
    public static PillQuality fromScore(float score) {
        if (score >= 0.93F) return PERFECT;
        if (score >= 0.75F) return REFINED;
        if (score >= 0.45F) return ORDINARY;
        return CRACKED;
    }
}

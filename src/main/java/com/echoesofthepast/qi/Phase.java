package com.echoesofthepast.qi;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The five phases of wuxing. Qi always carries some mixture of these; they are qualities of Qi
 * rather than separate resources.
 *
 * <p>Two relationships matter mechanically:
 * <ul>
 *   <li>generating ({@link #generates()}) - wood feeds fire, fire makes earth, and so on. Flow along
 *       this cycle is cheap and calm.</li>
 *   <li>overcoming ({@link #overcomes()}) - water kills fire, fire melts metal. Mixing phases that
 *       overcome each other is what produces turbulence.</li>
 * </ul>
 */
public enum Phase implements StringRepresentable {
    WOOD("wood", 0x6FBF5A),
    FIRE("fire", 0xD1462F),
    EARTH("earth", 0xC2A265),
    METAL("metal", 0xD8DCE0),
    WATER("water", 0x4C7FB5);

    public static final Phase[] VALUES = values();
    public static final Codec<Phase> CODEC = StringRepresentable.fromEnum(Phase::values);

    private final String name;
    private final int color;

    Phase(String name, int color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** Colour used for every particle, ink line and glow that represents this phase. */
    public int color() {
        return this.color;
    }

    /** The phase this one feeds, following the generating cycle. */
    public Phase generates() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    /** The phase that feeds this one. */
    public Phase generatedBy() {
        return VALUES[(this.ordinal() + VALUES.length - 1) % VALUES.length];
    }

    /** The phase this one suppresses, following the overcoming cycle. */
    public Phase overcomes() {
        return VALUES[(this.ordinal() + 2) % VALUES.length];
    }

    /** The phase that suppresses this one. */
    public Phase overcomeBy() {
        return VALUES[(this.ordinal() + 3) % VALUES.length];
    }

    /**
     * How comfortably this phase sits next to another one, from -1 (actively destructive) to 1.
     */
    public float affinity(Phase other) {
        if (this == other) return 1.0F;
        if (this.generates() == other || this.generatedBy() == other) return 0.5F;
        if (this.overcomes() == other || this.overcomeBy() == other) return -1.0F;
        return 0.0F;
    }

    public String translationKey() {
        return "eotp.phase." + this.name;
    }

    public static Phase byName(String name, Phase fallback) {
        for (Phase phase : VALUES) {
            if (phase.name.equals(name)) return phase;
        }
        return fallback;
    }
}

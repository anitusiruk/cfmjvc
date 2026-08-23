package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/**
 * The common principle three mastered Verses condense into. This, rather than the larger Qi pool, is
 * what Golden Core is actually for: each thesis changes how one of the mod's systems behaves for
 * that cultivator specifically.
 */
public enum CoreThesis implements StringRepresentable {
    /** Flow + Edge + Return: a returning blade gives back part of what it cost. */
    REVOLVING_EDGE("revolving_edge", Set.of(Principle.FLOW, Principle.EDGE, Principle.RETURN)),
    /** Stillness + Earth + Preservation: standing your ground compounds. */
    IMMOVABLE_MOUNTAIN("immovable_mountain", Set.of(Principle.STILLNESS, Principle.EARTH, Principle.PRESERVATION)),
    /** Growth + Transformation + Fire: excess heat becomes something living. */
    VERMILION_FURNACE("vermilion_furnace", Set.of(Principle.GROWTH, Principle.TRANSFORMATION, Principle.FIRE)),
    /** Echo + Command + Repetition: written instructions cooperate. */
    TEN_THOUSAND_WORDS("ten_thousand_words", Set.of(Principle.ECHO, Principle.COMMAND, Principle.REPETITION)),
    /** A coherent set that matches no recorded thesis. Still a real core, still yours. */
    UNWRITTEN("unwritten", Set.of());

    public static final CoreThesis[] VALUES = values();
    public static final Codec<CoreThesis> CODEC = StringRepresentable.fromEnum(CoreThesis::values);

    private final String name;
    private final Set<Principle> signature;

    CoreThesis(String name, Set<Principle> signature) {
        this.name = name;
        this.signature = signature;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.thesis." + this.name;
    }

    /**
     * Condenses three demonstrated Verses. A thesis forms when the principles they share cover a
     * recorded signature; anything else coherent becomes an unwritten core rather than a failure.
     */
    public static @Nullable CoreThesis condense(List<Verse> verses) {
        if (verses.size() < 3) return null;
        java.util.EnumSet<Principle> combined = java.util.EnumSet.noneOf(Principle.class);
        for (Verse verse : verses) {
            combined.addAll(verse.principles());
        }
        for (CoreThesis thesis : VALUES) {
            if (!thesis.signature.isEmpty() && combined.containsAll(thesis.signature)) {
                return thesis;
            }
        }
        return UNWRITTEN;
    }

    public boolean isAbout(Principle principle) {
        return this.signature.contains(principle);
    }
}

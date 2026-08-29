package com.echoesofthepast.formation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The kinds of mark a formation is built from. On their own they do nothing at all; what matters is
 * how many of each are present and whether they form a closed circuit.
 */
public enum FormationPart implements StringRepresentable {
    /** A point where Qi can be held or turned. Every formation needs at least one. */
    NODE("node"),
    /** Plain conducting line. */
    LINE("line"),
    /** Curved line, used to close a ring without a hard corner. */
    ARC("arc"),
    /** A trigram plate. Expensive, and the only part that carries meaning rather than shape. */
    TRIGRAM("trigram"),
    /** Ink drawn straight onto the floor. Weak, cheap, and phase aligned. */
    INK("ink");

    public static final FormationPart[] VALUES = values();
    public static final Codec<FormationPart> CODEC = StringRepresentable.fromEnum(FormationPart::values);

    private final String name;

    FormationPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** How much a part contributes to a formation's ability to hold Qi. */
    public float conductance() {
        return switch (this) {
            case NODE -> 1.0F;
            case LINE -> 0.6F;
            case ARC -> 0.7F;
            case TRIGRAM -> 1.4F;
            case INK -> 0.25F;
        };
    }

    public String translationKey() {
        return "eotp.formation_part." + this.name;
    }
}

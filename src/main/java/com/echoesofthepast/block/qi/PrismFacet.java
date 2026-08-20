package com.echoesofthepast.block.qi;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * How a prism has been cut. The facet decides what a beam entering it does next, and re-cutting a
 * prism into anything past the first two takes an understanding the player has to acquire.
 */
public enum PrismFacet implements StringRepresentable {
    /** Straight through, weakened. Useful only for reaching further than one beam can. */
    RELAY("relay", false),
    /** Into two beams, left and right of the entry direction. */
    SPLIT("split", false),
    /** Everything into one beam, strengthened at the cost of the rest of the light. */
    FOCUS("focus", true),
    /** Turns the beam a quarter turn, which is how a beam gets around a corner. */
    BEND("bend", true),
    /**
     * Passes only the part of the beam matching the phase the prism is turned to, dumping the rest.
     */
    FILTER("filter", true),
    /** Splits into all four horizontal directions at once. */
    SCATTER("scatter", true);

    public static final PrismFacet[] VALUES = values();
    public static final Codec<PrismFacet> CODEC = StringRepresentable.fromEnum(PrismFacet::values);

    private final String name;
    private final boolean advanced;

    PrismFacet(String name, boolean advanced) {
        this.name = name;
        this.advanced = advanced;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** Advanced facets require the prism-cutting discovery before they can be selected. */
    public boolean advanced() {
        return this.advanced;
    }

    public PrismFacet next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public String translationKey() {
        return "eotp.facet." + this.name;
    }
}

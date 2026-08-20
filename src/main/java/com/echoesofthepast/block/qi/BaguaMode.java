package com.echoesofthepast.block.qi;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * How a bagua divides what arrives at its centre. The mode is readable from the markings and the way
 * the block is turned, so there is no interface to open.
 */
public enum BaguaMode implements StringRepresentable {
    /** One trigram at a time, advancing on every delivery. */
    ALTERNATE("alternate"),
    /** Sweeps around the ring, one step per pulse. */
    CLOCKWISE("clockwise"),
    /** Opposite trigrams together, splitting the flow in two. */
    OPPOSED("opposed"),
    /** Everything to the trigram the block faces, and only the overflow elsewhere. */
    PRIORITY("priority"),
    /** All eight at once, evenly. */
    SPREAD("spread"),
    /**
     * Reads what each neighbour is holding and sends Qi where it will be generative rather than
     * destructive. Slow, wasteful, and the only routing that never spoils a reservoir.
     */
    GENERATIVE("generative");

    public static final BaguaMode[] VALUES = values();
    public static final Codec<BaguaMode> CODEC = StringRepresentable.fromEnum(BaguaMode::values);

    private final String name;

    BaguaMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public BaguaMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public String translationKey() {
        return "eotp.bagua." + this.name;
    }
}

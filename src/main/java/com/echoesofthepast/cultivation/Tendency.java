package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Rough categories the world quietly files a cultivator's significant Echoes under. Nobody chooses
 * a tendency: it is the shape of what somebody has actually spent their time doing, and it is the
 * raw material the Self-Script ritual reads back to them.
 */
public enum Tendency implements StringRepresentable {
    PROTECTING("protecting"),
    RETURNING("returning"),
    TRANSFORMING("transforming"),
    WANDERING("wandering"),
    ENDURING("enduring"),
    CREATING("creating"),
    CUTTING("cutting"),
    TENDING("tending"),
    COMMANDING("commanding"),
    OBSERVING("observing"),
    STILLNESS("stillness");

    public static final Tendency[] VALUES = values();
    public static final Codec<Tendency> CODEC = StringRepresentable.fromEnum(Tendency::values);

    private final String name;

    Tendency(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.tendency." + this.name;
    }
}

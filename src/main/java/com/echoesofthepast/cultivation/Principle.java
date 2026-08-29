package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The vocabulary a cultivator reasons in. A principle is not a recipe unlock: it is a relationship
 * the world has watched the player actually enact, and Verses are built by proving several of them
 * are true at the same time.
 */
public enum Principle implements StringRepresentable {
    FLOW("flow"),
    DIVIDE("divide"),
    RETURN("return"),
    HEAT("heat"),
    TRANSFORMATION("transformation"),
    PRESERVATION("preservation"),
    MOTION("motion"),
    EDGE("edge"),
    SOUND("sound"),
    COMMAND("command"),
    REPETITION("repetition"),
    GROWTH("growth"),
    WATER("water"),
    STILLNESS("stillness"),
    EARTH("earth"),
    ECHO("echo"),
    FIRE("fire");

    public static final Principle[] VALUES = values();
    public static final Codec<Principle> CODEC = StringRepresentable.fromEnum(Principle::values);

    private final String name;

    Principle(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.principle." + this.name;
    }
}

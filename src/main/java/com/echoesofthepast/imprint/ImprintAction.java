package com.echoesofthepast.imprint;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The vocabulary an ancestral tablet can learn. Deliberately small: a tablet imitates a gesture, it
 * does not simulate a player, so there is no way to teach one to do something a person could not
 * have been standing there doing.
 */
public enum ImprintAction implements StringRepresentable {
    /** Striking something: a bell, a stone, a chime. */
    STRIKE("strike"),
    /** Putting an item into something: a cauldron, a censer, a rack. */
    FEED("feed"),
    /** Turning or rotating a device: a bagua, a wheel, an abacus. */
    TURN("turn"),
    /** Stirring or tending a process without adding anything. */
    STIR("stir"),
    /** Taking a finished thing out. */
    HARVEST("harvest");

    public static final ImprintAction[] VALUES = values();
    public static final Codec<ImprintAction> CODEC = StringRepresentable.fromEnum(ImprintAction::values);

    private final String name;

    ImprintAction(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.imprint." + this.name;
    }
}

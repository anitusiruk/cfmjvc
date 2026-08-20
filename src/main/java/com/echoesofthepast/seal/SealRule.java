package com.echoesofthepast.seal;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * A carved seal holds a rule, not a spell. The same rule means something different stamped onto a
 * talisman, a formation tile, a lantern or a paper crane, which is why there are only a handful.
 */
public enum SealRule implements StringRepresentable {
    /** Holds a thing where it is: roots mobs, locks items in place, pins Qi in a reservoir. */
    BIND("bind", 0xB03A2E),
    /** Pushes outwards: knockback, projectile deflection, splitting Qi away from the centre. */
    REPEL("repel", 0xE0A030),
    /** Draws inwards: pulls wisps, items and ambient Qi towards the stamped thing. */
    GATHER("gather", 0x3E8E7E),
    /** Suppresses expression: quiets sound, hides Qi from detection, mutes spirits. */
    SILENCE("silence", 0x50566B),
    /** Stops change: no despawning, no decay, no spoiling of a delicate process. */
    PRESERVE("preserve", 0x8FBFD0),
    /** Sends a thing back where it came from: reflection, homing cranes, retreat. */
    RETURN("return", 0x7A5CA8),
    /** Divides one into many: splits Qi evenly, splits a beam, splits a delivery. */
    DIVIDE("divide", 0xD8DCE0);

    public static final SealRule[] VALUES = values();
    public static final Codec<SealRule> CODEC = StringRepresentable.fromEnum(SealRule::values);

    private final String name;
    private final int color;

    SealRule(String name, int color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int color() {
        return this.color;
    }

    public String translationKey() {
        return "eotp.seal." + this.name;
    }

    public SealRule next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public static SealRule byName(String name, SealRule fallback) {
        for (SealRule rule : VALUES) {
            if (rule.name.equals(name)) return rule;
        }
        return fallback;
    }
}

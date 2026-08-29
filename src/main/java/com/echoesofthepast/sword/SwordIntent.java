package com.echoesofthepast.sword;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * A bound sword remembers how it has been used, and the memory shapes what it can do. Intent is not
 * chosen and cannot be enchanted on: it accumulates from actual fights.
 */
public enum SwordIntent implements StringRepresentable {
    /** Earned against undead and spirits. Burns what should not still be walking. */
    PURIFYING("purifying"),
    /** Earned by striking from above. Turns a fall into a single heavy descent. */
    FALLING_STAR("falling_star"),
    /** Earned by standing and blocking. Steadies the wielder and shrugs off knockback. */
    MOUNTAIN("mountain"),
    /** Earned by fighting several enemies at once. The blade will not stop at one target. */
    FLOWING_RIVER("flowing_river"),
    /** Earned by finishing fights in a single strike. Rewards patience with precision. */
    STILL_WATER("still_water");

    public static final SwordIntent[] VALUES = values();
    public static final Codec<SwordIntent> CODEC = StringRepresentable.fromEnum(SwordIntent::values);

    private final String name;

    SwordIntent(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String translationKey() {
        return "eotp.intent." + this.name;
    }
}

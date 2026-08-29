package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Five channels, opened one at a time. An open meridian is a circuit, not a stat: it decides
 * <em>where</em> a cultivator's Qi is allowed to go.
 *
 * <p>Each meridian opens by doing the thing it governs, which means the way a player fights and
 * builds decides which abilities they get first.
 */
public enum Meridian implements StringRepresentable {
    /** Qi into a held weapon: sword qi, flying sword control, talisman throwing. */
    HAND("hand", 100.0F),
    /** Qi into movement: cloudstepping, footwork seals, ribbon work. */
    FOOT("foot", 100.0F),
    /** Qi into the body: armour techniques, resisting backlash, holding a ritual together. */
    HEART("heart", 140.0F),
    /** Qi into perception: reading echoes, seeing veins and formation lines unaided. */
    CROWN("crown", 140.0F),
    /** The lower field: personal capacity, pill absorption, keeping a golden core stable. */
    DANTIAN("dantian", 200.0F);

    public static final Meridian[] VALUES = values();
    public static final Codec<Meridian> CODEC = StringRepresentable.fromEnum(Meridian::values);

    private final String name;
    private final float effortRequired;

    Meridian(String name, float effortRequired) {
        this.name = name;
        this.effortRequired = effortRequired;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** How much relevant practice it takes to force this channel open. */
    public float effortRequired() {
        return this.effortRequired;
    }

    public String translationKey() {
        return "eotp.meridian." + this.name;
    }
}

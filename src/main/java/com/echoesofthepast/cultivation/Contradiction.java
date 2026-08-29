package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * What Heaven actually tests. Tribulation is not a stack of lightning bolts aimed at a health bar:
 * the formation reads what the cultivator claims to understand and attacks that specific claim.
 *
 * <p>Each contradiction targets a system the player has been leaning on, so a workshop built
 * entirely on bells fears Silence and a Return-shaped cultivator fears Return Denied.
 */
public enum Contradiction implements StringRepresentable {
    /** Qi periodically runs backwards through the network. */
    REVERSAL("reversal", Principle.FLOW),
    /** One phase floods everything and has to be spent or converted. */
    EXCESS("excess", Principle.TRANSFORMATION),
    /** Bells and resonance stones go quiet, forcing a fallback control route. */
    SILENCE("silence", Principle.SOUND),
    /** One formation clause becomes unreadable and the circuit misfires. */
    FRACTURE("fracture", Principle.COMMAND),
    /** Ghost actions replay old inputs and can operate machinery by themselves. */
    FALSE_ECHO("false_echo", Principle.ECHO),
    /** The classic: conductor-seeking bolts fall on the workshop. */
    LIGHTNING("lightning", Principle.EDGE),
    /** Plants in the active Landscape stop producing their expected phase. */
    WITHERING("withering", Principle.GROWTH),
    /** Flying swords and return effects cannot complete on their own. */
    RETURN_DENIED("return_denied", Principle.RETURN);

    public static final Contradiction[] VALUES = values();
    public static final Codec<Contradiction> CODEC = StringRepresentable.fromEnum(Contradiction::values);

    private final String name;
    private final Principle targets;

    Contradiction(String name, Principle targets) {
        this.name = name;
        this.targets = targets;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** The principle this contradiction argues against. */
    public Principle targets() {
        return this.targets;
    }

    public String translationKey() {
        return "eotp.contradiction." + this.name;
    }

    /** How relevant this contradiction is to a particular cultivator's claims. */
    public float relevanceTo(Cultivator cultivator) {
        float relevance = 0.2F;
        CoreThesis thesis = cultivator.path().thesis();
        if (thesis != null && thesis.isAbout(this.targets)) relevance += 1.0F;

        for (Verse verse : cultivator.path().masteredVerses()) {
            if (verse.contains(this.targets)) relevance += 0.4F;
        }

        InnerLandscape landscape = cultivator.path().landscape();
        if (landscape != null && landscape.relationship() == this.targets) relevance += 0.6F;

        SelfScript script = cultivator.path().selfScript();
        if (script != null && this == LIGHTNING && script.family() == SelfScript.Family.MOUNTAIN) {
            relevance += 0.3F;
        }
        return relevance;
    }
}

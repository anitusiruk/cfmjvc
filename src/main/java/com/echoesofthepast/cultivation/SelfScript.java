package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * What Foundation actually establishes: a written record of the kind of cultivator somebody has
 * turned out to be.
 *
 * <p>The family is only a name for the shape of the three tendencies that formed it. The tendencies
 * themselves are kept, so two cultivators sharing a family are still not the same person, and a
 * contradictory script stays contradictory rather than being rounded off to a class.
 */
public record SelfScript(Family family, List<Tendency> tendencies) {
    public static final Codec<SelfScript> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Family.CODEC.fieldOf("family").forGetter(SelfScript::family),
        Tendency.CODEC.listOf().fieldOf("tendencies").forGetter(SelfScript::tendencies)
    ).apply(instance, SelfScript::new));

    public enum Family implements net.minecraft.util.StringRepresentable {
        HEARTH("hearth"),
        RIVER_BLADE("river_blade"),
        MOUNTAIN("mountain"),
        DISTANT_SKY("distant_sky"),
        SCRIPTURE("scripture"),
        /** A script whose tendencies do not settle into a known shape. Rarer and stranger. */
        UNNAMED("unnamed");

        public static final Codec<Family> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Family::values);

        private final String name;

        Family(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String translationKey() {
            return "eotp.script." + this.name;
        }
    }

    /** Reads three tendencies and names the shape they make. */
    public static SelfScript from(List<Tendency> tendencies) {
        return new SelfScript(classify(tendencies), List.copyOf(tendencies));
    }

    private static Family classify(List<Tendency> tendencies) {
        boolean tending = tendencies.contains(Tendency.TENDING);
        boolean creating = tendencies.contains(Tendency.CREATING);
        boolean transforming = tendencies.contains(Tendency.TRANSFORMING);
        boolean returning = tendencies.contains(Tendency.RETURNING);
        boolean cutting = tendencies.contains(Tendency.CUTTING);
        boolean wandering = tendencies.contains(Tendency.WANDERING);
        boolean enduring = tendencies.contains(Tendency.ENDURING);
        boolean protecting = tendencies.contains(Tendency.PROTECTING);
        boolean commanding = tendencies.contains(Tendency.COMMANDING);
        boolean observing = tendencies.contains(Tendency.OBSERVING);
        boolean stillness = tendencies.contains(Tendency.STILLNESS);

        if (tending && transforming && (creating || protecting)) return Family.HEARTH;
        if (returning && cutting && (wandering || observing)) return Family.RIVER_BLADE;
        if (enduring && (stillness || protecting)) return Family.MOUNTAIN;
        if (wandering && (observing || returning)) return Family.DISTANT_SKY;
        if (commanding && (creating || observing)) return Family.SCRIPTURE;
        return Family.UNNAMED;
    }

    /**
     * How hard this script was to hold together. A contradictory script takes longer to stabilise
     * and, in exchange, opens stranger interactions later.
     */
    public float complexity() {
        float complexity = this.family == Family.UNNAMED ? 1.4F : 1.0F;
        if (this.tendencies.contains(Tendency.CUTTING) && this.tendencies.contains(Tendency.TENDING)) complexity += 0.3F;
        if (this.tendencies.contains(Tendency.WANDERING) && this.tendencies.contains(Tendency.ENDURING)) complexity += 0.3F;
        if (this.tendencies.contains(Tendency.COMMANDING) && this.tendencies.contains(Tendency.STILLNESS)) complexity += 0.2F;
        return complexity;
    }

    public Component describe() {
        return Component.translatable(this.family.translationKey());
    }
}

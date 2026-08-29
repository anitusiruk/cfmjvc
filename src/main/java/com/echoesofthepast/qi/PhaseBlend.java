package com.echoesofthepast.qi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * An immutable mixture of the five phases. Values are weights, not amounts of Qi: a blend describes
 * what a body of Qi <em>is like</em>, while {@link QiStorage} tracks how much of it there is.
 */
public final class PhaseBlend {
    public static final PhaseBlend EMPTY = new PhaseBlend(0, 0, 0, 0, 0);
    public static final PhaseBlend BALANCED = new PhaseBlend(1, 1, 1, 1, 1).normalised();

    public static final Codec<PhaseBlend> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.optionalFieldOf("wood", 0.0F).forGetter(b -> b.wood),
        Codec.FLOAT.optionalFieldOf("fire", 0.0F).forGetter(b -> b.fire),
        Codec.FLOAT.optionalFieldOf("earth", 0.0F).forGetter(b -> b.earth),
        Codec.FLOAT.optionalFieldOf("metal", 0.0F).forGetter(b -> b.metal),
        Codec.FLOAT.optionalFieldOf("water", 0.0F).forGetter(b -> b.water)
    ).apply(instance, PhaseBlend::new));

    private final float wood;
    private final float fire;
    private final float earth;
    private final float metal;
    private final float water;

    public PhaseBlend(float wood, float fire, float earth, float metal, float water) {
        this.wood = Math.max(0.0F, wood);
        this.fire = Math.max(0.0F, fire);
        this.earth = Math.max(0.0F, earth);
        this.metal = Math.max(0.0F, metal);
        this.water = Math.max(0.0F, water);
    }

    public static PhaseBlend of(Phase phase) {
        return of(phase, 1.0F);
    }

    public static PhaseBlend of(Phase phase, float weight) {
        return switch (phase) {
            case WOOD -> new PhaseBlend(weight, 0, 0, 0, 0);
            case FIRE -> new PhaseBlend(0, weight, 0, 0, 0);
            case EARTH -> new PhaseBlend(0, 0, weight, 0, 0);
            case METAL -> new PhaseBlend(0, 0, 0, weight, 0);
            case WATER -> new PhaseBlend(0, 0, 0, 0, weight);
        };
    }

    public static PhaseBlend of(Phase first, Phase second) {
        return of(first).plus(of(second)).normalised();
    }

    public float get(Phase phase) {
        return switch (phase) {
            case WOOD -> this.wood;
            case FIRE -> this.fire;
            case EARTH -> this.earth;
            case METAL -> this.metal;
            case WATER -> this.water;
        };
    }

    public float total() {
        return this.wood + this.fire + this.earth + this.metal + this.water;
    }

    public boolean isEmpty() {
        return this.total() <= 1.0E-4F;
    }

    public PhaseBlend plus(PhaseBlend other) {
        return new PhaseBlend(
            this.wood + other.wood,
            this.fire + other.fire,
            this.earth + other.earth,
            this.metal + other.metal,
            this.water + other.water
        );
    }

    public PhaseBlend with(Phase phase, float weight) {
        return this.plus(of(phase, weight));
    }

    public PhaseBlend scaled(float factor) {
        return new PhaseBlend(this.wood * factor, this.fire * factor, this.earth * factor, this.metal * factor, this.water * factor);
    }

    /** Same character, weights summing to one. Empty blends stay empty. */
    public PhaseBlend normalised() {
        float total = this.total();
        return total <= 1.0E-4F ? EMPTY : this.scaled(1.0F / total);
    }

    /**
     * Mixes two bodies of Qi, weighting each blend by how much Qi carries it.
     */
    public static PhaseBlend mix(PhaseBlend a, float amountA, PhaseBlend b, float amountB) {
        if (amountA + amountB <= 1.0E-4F) return EMPTY;
        return a.normalised().scaled(amountA).plus(b.normalised().scaled(amountB)).normalised();
    }

    /** Nudges this blend a fraction of the way towards another one. */
    public PhaseBlend lerp(PhaseBlend target, float delta) {
        float t = Mth.clamp(delta, 0.0F, 1.0F);
        return this.normalised().scaled(1.0F - t).plus(target.normalised().scaled(t)).normalised();
    }

    /** The strongest phase present, or null for perfectly balanced or empty Qi. */
    public @Nullable Phase dominant() {
        Phase best = null;
        float bestWeight = 0.0F;
        boolean tie = false;
        for (Phase phase : Phase.VALUES) {
            float weight = this.get(phase);
            if (weight > bestWeight + 1.0E-4F) {
                best = phase;
                bestWeight = weight;
                tie = false;
            } else if (Math.abs(weight - bestWeight) <= 1.0E-4F && best != null && phase != best) {
                tie = true;
            }
        }
        return tie ? null : best;
    }

    /** The weakest phase present - the one a formation or recipe is usually missing. */
    public Phase weakest() {
        Phase worst = Phase.WOOD;
        float worstWeight = Float.MAX_VALUE;
        for (Phase phase : Phase.VALUES) {
            float weight = this.get(phase);
            if (weight < worstWeight) {
                worst = phase;
                worstWeight = weight;
            }
        }
        return worst;
    }

    /**
     * How well the phases present get along, from 0 (phases actively destroying each other) to 1.
     * Pure Qi of a single phase is perfectly harmonious; equal parts fire and water is not.
     */
    public float harmony() {
        PhaseBlend n = this.normalised();
        if (n.isEmpty()) return 1.0F;
        float score = 0.0F;
        float weightSum = 0.0F;
        for (Phase a : Phase.VALUES) {
            float wa = n.get(a);
            if (wa <= 1.0E-4F) continue;
            for (Phase b : Phase.VALUES) {
                float wb = n.get(b);
                if (wb <= 1.0E-4F) continue;
                float pairWeight = wa * wb;
                score += pairWeight * a.affinity(b);
                weightSum += pairWeight;
            }
        }
        if (weightSum <= 1.0E-4F) return 1.0F;
        return Mth.clamp((score / weightSum + 1.0F) * 0.5F, 0.0F, 1.0F);
    }

    /** Complement of {@link #harmony()}: what makes Qi leak, spark and hurt people. */
    public float turbulence() {
        return 1.0F - this.harmony();
    }

    /**
     * How closely this blend matches a target character, from 0 to 1. Used by every recipe that
     * cares about relationships between phases instead of raw throughput.
     */
    public float similarity(PhaseBlend target) {
        PhaseBlend a = this.normalised();
        PhaseBlend b = target.normalised();
        if (a.isEmpty() && b.isEmpty()) return 1.0F;
        float distance = 0.0F;
        for (Phase phase : Phase.VALUES) {
            distance += Math.abs(a.get(phase) - b.get(phase));
        }
        return Mth.clamp(1.0F - distance * 0.5F, 0.0F, 1.0F);
    }

    /** Average of the phase colours present, for particles and ink. */
    public int color() {
        PhaseBlend n = this.normalised();
        if (n.isEmpty()) return 0xBFD4D8;
        float r = 0, g = 0, b = 0;
        for (Phase phase : Phase.VALUES) {
            float weight = n.get(phase);
            int color = phase.color();
            r += weight * ((color >> 16) & 0xFF);
            g += weight * ((color >> 8) & 0xFF);
            b += weight * (color & 0xFF);
        }
        return (Mth.clamp((int) r, 0, 255) << 16) | (Mth.clamp((int) g, 0, 255) << 8) | Mth.clamp((int) b, 0, 255);
    }

    @Override
    public String toString() {
        PhaseBlend n = this.normalised();
        StringBuilder builder = new StringBuilder();
        for (Phase phase : Phase.VALUES) {
            float weight = n.get(phase);
            if (weight <= 0.01F) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(phase.getSerializedName()).append(' ').append(Math.round(weight * 100.0F)).append('%');
        }
        return builder.isEmpty() ? "empty" : builder.toString();
    }
}

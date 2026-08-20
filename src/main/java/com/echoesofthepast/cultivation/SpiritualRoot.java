package com.echoesofthepast.cultivation;

import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A cultivator's attunement. Roots are chosen in a ritual and can be changed later, at a price, so
 * a player is never stuck with a bad roll from world creation.
 *
 * <p>A single root is clean and reliable. Two roots that feed each other are the strongest
 * ordinary choice. Two roots that fight each other are a gamble: techniques hit harder, but Qi runs
 * rough and deviation is much more likely.
 */
public record SpiritualRoot(List<Phase> phases) {
    public static final SpiritualRoot NONE = new SpiritualRoot(List.of());
    public static final int MAX_PHASES = 3;

    public static final Codec<SpiritualRoot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Phase.CODEC.listOf().optionalFieldOf("phases", List.of()).forGetter(SpiritualRoot::phases)
    ).apply(instance, SpiritualRoot::new));

    public SpiritualRoot {
        List<Phase> unique = List.copyOf(new LinkedHashSet<>(phases));
        phases = unique.size() > MAX_PHASES ? List.copyOf(unique.subList(0, MAX_PHASES)) : unique;
    }

    public static SpiritualRoot of(Phase... phases) {
        return new SpiritualRoot(List.of(phases));
    }

    public boolean isEmpty() {
        return this.phases.isEmpty();
    }

    public boolean has(Phase phase) {
        return this.phases.contains(phase);
    }

    /** The character of Qi this cultivator naturally produces. */
    public PhaseBlend blend() {
        PhaseBlend blend = PhaseBlend.EMPTY;
        for (Phase phase : this.phases) {
            blend = blend.with(phase, 1.0F);
        }
        return blend.normalised();
    }

    /**
     * How smoothly the roots work together. One phase, or two in a generating relationship, runs
     * clean; phases that overcome each other run rough.
     */
    public float coherence() {
        if (this.phases.size() <= 1) return 1.0F;
        return this.blend().harmony();
    }

    /** A conflicted root hits harder. This is the reward for taking the dangerous attunement. */
    public float powerMultiplier() {
        return 1.0F + (1.0F - this.coherence()) * 0.5F;
    }

    /** ...and the risk that goes with it. */
    public float deviationRisk() {
        return (1.0F - this.coherence()) * 0.6F;
    }

    /** How efficiently this cultivator can absorb Qi of a given phase. */
    public float affinityWith(Phase phase) {
        if (this.phases.isEmpty()) return 0.6F;
        if (this.has(phase)) return 1.4F;
        for (Phase root : this.phases) {
            if (root.generates() == phase || root.generatedBy() == phase) return 1.0F;
        }
        return 0.7F;
    }

    public @Nullable Phase primary() {
        return this.phases.isEmpty() ? null : this.phases.get(0);
    }

    /**
     * A mixed root in an overcoming relationship is what unlocks the odder techniques - the ones
     * that need Qi to be at war with itself.
     */
    public boolean isConflicted() {
        for (Phase a : this.phases) {
            for (Phase b : this.phases) {
                if (a != b && (a.overcomes() == b || a.overcomeBy() == b)) return true;
            }
        }
        return false;
    }

    public String describe() {
        if (this.phases.isEmpty()) return "unattuned";
        StringBuilder builder = new StringBuilder();
        for (Phase phase : this.phases) {
            if (!builder.isEmpty()) builder.append('-');
            builder.append(phase.getSerializedName());
        }
        return builder.toString();
    }
}

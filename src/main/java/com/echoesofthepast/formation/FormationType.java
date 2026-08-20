package com.echoesofthepast.formation;

import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.seal.SealRule;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/**
 * What a completed circuit turns out to be. A survey is tested against these in order, so a
 * formation that satisfies several definitions becomes the most specific one.
 *
 * <p>Requirements are about shape and content, never about exact block placement.
 */
public enum FormationType implements StringRepresentable {
    /**
     * Pulls ambient Qi out of the air and the ground into the core. The bread and butter formation:
     * a ring with a node in it.
     */
    GATHERING("gathering", Discovery.FORMATION_GATHERING, 8, PhaseBlend.EMPTY) {
        @Override
        public boolean matches(FormationSurvey survey) {
            return survey.closed() && survey.count(FormationPart.NODE) >= 1 && survey.size() >= 8;
        }
    },
    /**
     * Throws things away from the centre. Wants a repel seal somewhere on the circuit, which is what
     * stops it from being the default reading of every ring.
     */
    REPULSION("repulsion", Discovery.FORMATION_REPULSION, 12, PhaseBlend.of(Phase.METAL)) {
        @Override
        public boolean matches(FormationSurvey survey) {
            return survey.closed() && survey.seals().contains(SealRule.REPEL) && survey.size() >= 12;
        }
    },
    /**
     * Holds a cultivator steady through a breakthrough. Needs trigram plates and at least three
     * phases of ink, because a breakthrough is about relationships rather than volume.
     */
    CULTIVATION("cultivation", Discovery.FORMATION_CULTIVATION, 20, PhaseBlend.BALANCED) {
        @Override
        public boolean matches(FormationSurvey survey) {
            return survey.closed()
                && survey.count(FormationPart.TRIGRAM) >= 4
                && survey.inkPhases().size() >= 3
                && survey.size() >= 16;
        }
    },
    /**
     * Stops things changing inside it: dropped items stay, nothing spawns, processes hold their
     * state. Needs a preserve seal.
     */
    PRESERVATION("preservation", Discovery.FORMATION_PRESERVATION, 10, PhaseBlend.of(Phase.EARTH)) {
        @Override
        public boolean matches(FormationSurvey survey) {
            return survey.closed() && survey.seals().contains(SealRule.PRESERVE) && survey.size() >= 10;
        }
    },
    /**
     * Rewrites a cultivator's spiritual root. Needs all five phases drawn into it, which makes it
     * the most expensive floor in the mod to lay.
     */
    ATTUNEMENT("attunement", Discovery.FORMATION_ATTUNEMENT, 40, PhaseBlend.BALANCED) {
        @Override
        public boolean matches(FormationSurvey survey) {
            return survey.closed()
                && survey.inkPhases().size() == 5
                && survey.count(FormationPart.TRIGRAM) >= 2
                && survey.size() >= 20;
        }
    };

    public static final FormationType[] VALUES = values();
    public static final Codec<FormationType> CODEC = StringRepresentable.fromEnum(FormationType::values);

    private final String name;
    private final String discovery;
    private final float upkeep;
    private final PhaseBlend preferredBlend;

    FormationType(String name, String discovery, float upkeep, PhaseBlend preferredBlend) {
        this.name = name;
        this.discovery = discovery;
        this.upkeep = upkeep;
        this.preferredBlend = preferredBlend;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** The teaching a cultivator must have before the circuit will answer them. */
    public String discovery() {
        return this.discovery;
    }

    /** Qi drawn per second while running. */
    public float upkeep() {
        return this.upkeep;
    }

    /** What the formation would like to be fed; feeding it something else weakens the effect. */
    public PhaseBlend preferredBlend() {
        return this.preferredBlend;
    }

    public String translationKey() {
        return "eotp.formation." + this.name;
    }

    public abstract boolean matches(FormationSurvey survey);

    /** Formations are tested most-specific first, so gathering is the fallback reading. */
    public static @Nullable FormationType identify(FormationSurvey survey) {
        FormationType best = null;
        int bestRequirement = -1;
        for (FormationType type : VALUES) {
            if (!type.matches(survey)) continue;
            int requirement = Math.round(type.upkeep);
            if (requirement > bestRequirement) {
                best = type;
                bestRequirement = requirement;
            }
        }
        return best;
    }
}

package com.echoesofthepast.formation;

import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.seal.SealRule;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/**
 * The rules a written statement can turn out to say.
 *
 * <p>Each formation begins from a governing seal and adds requirements about orientation, clause
 * count and phase language. Reading fails with a specific complaint rather than a blanket refusal,
 * so a player is told which clause is wrong instead of being left to compare their floor against a
 * picture.
 */
public enum FormationType implements StringRepresentable {
    /**
     * Gather: marks turned inward, drawing ambient vein Qi toward the core. It does not receive Qi
     * through its own lines - the lines only say what the separately supplied Qi should do.
     */
    GATHERING("gathering", Discovery.FORMATION_GATHERING, SealRule.GATHER, 8, 8, PhaseBlend.EMPTY),
    /** Repel: marks turned outward, pushing what approaches away from the centre. */
    REPULSION("repulsion", Discovery.FORMATION_REPULSION, SealRule.REPEL, 12, 12, PhaseBlend.of(Phase.METAL)),
    /** Preserve: an uninterrupted outer clause that stops things inside it from ageing. */
    PRESERVATION("preservation", Discovery.FORMATION_PRESERVATION, SealRule.PRESERVE, 10, 10, PhaseBlend.of(Phase.EARTH)),
    /** Bind: a balanced statement steady enough to hold a cultivator through a ritual. */
    CULTIVATION("cultivation", Discovery.FORMATION_CULTIVATION, SealRule.BIND, 16, 20, PhaseBlend.BALANCED),
    /** Return: a five-phase statement used to read a Landscape back into the person who grew it. */
    ATTUNEMENT("attunement", Discovery.FORMATION_ATTUNEMENT, SealRule.RETURN, 20, 40, PhaseBlend.BALANCED);

    public static final FormationType[] VALUES = values();
    public static final Codec<FormationType> CODEC = StringRepresentable.fromEnum(FormationType::values);

    private final String name;
    private final String discovery;
    private final SealRule governingSeal;
    private final int marksRequired;
    private final float upkeep;
    private final PhaseBlend preferredBlend;

    FormationType(String name, String discovery, SealRule governingSeal, int marksRequired, float upkeep, PhaseBlend preferredBlend) {
        this.name = name;
        this.discovery = discovery;
        this.governingSeal = governingSeal;
        this.marksRequired = marksRequired;
        this.upkeep = upkeep;
        this.preferredBlend = preferredBlend;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String discovery() {
        return this.discovery;
    }

    public SealRule governingSeal() {
        return this.governingSeal;
    }

    public float upkeep() {
        return this.upkeep;
    }

    public PhaseBlend preferredBlend() {
        return this.preferredBlend;
    }

    public String translationKey() {
        return "eotp.formation." + this.name;
    }

    /**
     * Reads the statement as this formation.
     *
     * @return null if it reads cleanly, or the translation key of the clause that fails
     */
    public @Nullable String validate(FormationSurvey survey) {
        if (survey.governingSeal() != this.governingSeal) {
            return "eotp.reading.wrong_seal";
        }
        if (survey.size() < this.marksRequired) {
            return "eotp.reading.too_short";
        }
        if (!survey.closed()) {
            return "eotp.reading.no_return";
        }

        return switch (this) {
            case GATHERING -> {
                if (survey.clauses() < 1) yield "eotp.reading.no_clause";
                yield survey.readsInward() ? null : "eotp.reading.not_inward";
            }
            case REPULSION -> survey.readsOutward() ? null : "eotp.reading.not_outward";
            case PRESERVATION -> null;
            case CULTIVATION -> {
                if (survey.trigrams() < 4) yield "eotp.reading.needs_trigrams";
                if (survey.inkPhases().size() < 3) yield "eotp.reading.needs_phases";
                // A cultivation statement has to both draw in and give back.
                if (survey.inwardMarks() < 1 || survey.outwardMarks() < 1) yield "eotp.reading.needs_both_clauses";
                yield null;
            }
            case ATTUNEMENT -> {
                if (survey.inkPhases().size() < Phase.VALUES.length) yield "eotp.reading.needs_five_phases";
                if (survey.trigrams() < 2) yield "eotp.reading.needs_trigrams";
                yield null;
            }
        };
    }

    /** A complete reading: what the statement says, or why it cannot be read. */
    public record Reading(@Nullable FormationType type, @Nullable String failure) {
        public boolean isReadable() {
            return this.type != null;
        }
    }

    /**
     * Identifies the statement. The governing seal decides which formation is even being attempted,
     * so an unreadable circuit can explain the specific clause that fails rather than saying no.
     */
    public static Reading read(FormationSurvey survey) {
        if (survey.parts().isEmpty()) {
            return new Reading(null, "eotp.reading.nothing_written");
        }
        if (survey.governingSeal() == null) {
            return new Reading(null, "eotp.reading.no_governing_seal");
        }

        // Two seals of different rules on one statement give it two equally valid readings.
        if (survey.seals().size() > 1) {
            return new Reading(null, "eotp.reading.ambiguous");
        }

        for (FormationType type : VALUES) {
            if (type.governingSeal != survey.governingSeal()) continue;
            String failure = type.validate(survey);
            return failure == null ? new Reading(type, null) : new Reading(null, failure);
        }
        return new Reading(null, "eotp.reading.unknown_rule");
    }

    /** Convenience for callers that only care whether the statement runs. */
    public static @Nullable FormationType identify(FormationSurvey survey) {
        return read(survey).type();
    }
}

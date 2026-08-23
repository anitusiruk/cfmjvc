package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The part of a cultivator that is a record of what they have proven rather than a pile of numbers:
 * the Witnesses they have given, the tendencies the world has filed them under, the Self-Script
 * those tendencies collapsed into, the Verses they can demonstrate, the Core Thesis those Verses
 * condensed to, the Landscape whose history they carry, and the Discord left by anything that
 * collapsed on them.
 *
 * <p>Kept as one nested object so the cultivator record stays inside a single codec group.
 */
public final class CultivationPath {
    public static final Codec<CultivationPath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Witness.CODEC.listOf().optionalFieldOf("witnesses", List.of()).forGetter(path -> List.copyOf(path.witnesses)),
        Codec.unboundedMap(Tendency.CODEC, Codec.FLOAT).optionalFieldOf("tendencies", Map.of()).forGetter(path -> path.tendencies),
        SelfScript.CODEC.optionalFieldOf("self_script").forGetter(path -> Optional.ofNullable(path.selfScript)),
        Verse.CODEC.listOf().optionalFieldOf("verses", List.of()).forGetter(path -> List.copyOf(path.masteredVerses)),
        CoreThesis.CODEC.optionalFieldOf("thesis").forGetter(path -> Optional.ofNullable(path.thesis)),
        InnerLandscape.CODEC.optionalFieldOf("landscape").forGetter(path -> Optional.ofNullable(path.landscape)),
        Principle.CODEC.listOf().optionalFieldOf("discord", List.of()).forGetter(path -> List.copyOf(path.discord)),
        Codec.INT.optionalFieldOf("survived_tribulations", 0).forGetter(path -> path.survivedTribulations)
    ).apply(instance, CultivationPath::new));

    private final Set<Witness> witnesses;
    private final Map<Tendency, Float> tendencies;
    private @Nullable SelfScript selfScript;
    private final List<Verse> masteredVerses;
    private @Nullable CoreThesis thesis;
    private @Nullable InnerLandscape landscape;
    private final Set<Principle> discord;
    private int survivedTribulations;

    public CultivationPath() {
        this(List.of(), Map.of(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(), List.of(), 0);
    }

    private CultivationPath(
        List<Witness> witnesses,
        Map<Tendency, Float> tendencies,
        Optional<SelfScript> selfScript,
        List<Verse> masteredVerses,
        Optional<CoreThesis> thesis,
        Optional<InnerLandscape> landscape,
        List<Principle> discord,
        int survivedTribulations
    ) {
        this.witnesses = EnumSet.noneOf(Witness.class);
        this.witnesses.addAll(witnesses);
        this.tendencies = new EnumMap<>(Tendency.class);
        this.tendencies.putAll(tendencies);
        this.selfScript = selfScript.orElse(null);
        this.masteredVerses = new ArrayList<>(masteredVerses);
        this.thesis = thesis.orElse(null);
        this.landscape = landscape.orElse(null);
        this.discord = EnumSet.noneOf(Principle.class);
        this.discord.addAll(discord);
        this.survivedTribulations = survivedTribulations;
    }

    // -------------------------------------------------------------------------------- witnesses

    public boolean hasWitness(Witness witness) {
        return this.witnesses.contains(witness);
    }

    /** @return true if this Witness had not been given before. */
    public boolean giveWitness(Witness witness) {
        return this.witnesses.add(witness);
    }

    public boolean hasAllWitnesses() {
        return this.witnesses.size() == Witness.VALUES.length;
    }

    public Set<Witness> witnesses() {
        return Set.copyOf(this.witnesses);
    }

    /** The Witnesses are proofs, not currency: opening Breath Gathering spends all three. */
    public void consumeWitnesses() {
        this.witnesses.clear();
    }

    // ------------------------------------------------------------------------------- tendencies

    public void noteTendency(Tendency tendency, float weight) {
        this.tendencies.merge(tendency, weight, Float::sum);
    }

    public float tendency(Tendency tendency) {
        return this.tendencies.getOrDefault(tendency, 0.0F);
    }

    /** The strongest few tendencies, most pronounced first. */
    public List<Tendency> strongestTendencies(int count) {
        List<Tendency> ordered = new ArrayList<>(this.tendencies.keySet());
        ordered.sort((a, b) -> Float.compare(this.tendency(b), this.tendency(a)));
        return ordered.size() > count ? List.copyOf(ordered.subList(0, count)) : List.copyOf(ordered);
    }

    // ------------------------------------------------------------------------------ self-script

    public @Nullable SelfScript selfScript() {
        return this.selfScript;
    }

    public void setSelfScript(SelfScript script) {
        this.selfScript = script;
    }

    // ----------------------------------------------------------------------------------- verses

    public List<Verse> masteredVerses() {
        return List.copyOf(this.masteredVerses);
    }

    public boolean hasMastered(Verse verse) {
        return this.masteredVerses.stream().anyMatch(known -> known.key().equals(verse.key()));
    }

    /** @return true if this Verse is newly mastered. */
    public boolean masterVerse(Verse verse) {
        if (this.hasMastered(verse)) return false;
        this.masteredVerses.add(verse);
        return true;
    }

    // ------------------------------------------------------------------------------ core thesis

    public @Nullable CoreThesis thesis() {
        return this.thesis;
    }

    public void setThesis(CoreThesis thesis) {
        this.thesis = thesis;
    }

    // -------------------------------------------------------------------------------- landscape

    public @Nullable InnerLandscape landscape() {
        return this.landscape;
    }

    public void setLandscape(InnerLandscape landscape) {
        this.landscape = landscape;
    }

    public void addScar(HeavenScar scar) {
        if (this.landscape != null) {
            this.landscape = this.landscape.withScar(scar);
        }
    }

    // ---------------------------------------------------------------------------------- discord

    /**
     * What was failing when a ritual collapsed. Discord is repaired by demonstrating that principle
     * again in ordinary play, not by waiting or by paying anything back.
     */
    public void addDiscord(Principle principle) {
        this.discord.add(principle);
    }

    public boolean hasDiscord() {
        return !this.discord.isEmpty();
    }

    public Set<Principle> discord() {
        return Set.copyOf(this.discord);
    }

    /** @return true if this demonstration settled an outstanding Discord. */
    public boolean resolveDiscord(Principle principle) {
        return this.discord.remove(principle);
    }

    /** A stabilising pill suppresses the oldest outstanding Discord. */
    public boolean suppressOneDiscord() {
        var iterator = this.discord.iterator();
        if (!iterator.hasNext()) return false;
        iterator.next();
        iterator.remove();
        return true;
    }

    // ---------------------------------------------------------------------------- tribulations

    public int survivedTribulations() {
        return this.survivedTribulations;
    }

    public void noteSurvivedTribulation() {
        this.survivedTribulations++;
    }

    /** Everything a Nascent Spirit attempt insists on being true at once. */
    public boolean readyForNascentSpirit(int openMeridians) {
        return this.selfScript != null
            && this.thesis != null
            && this.landscape != null
            && openMeridians >= 3
            && this.survivedTribulations >= 1;
    }

    /** Used by the manual and the compass to list what a Nascent attempt is still missing. */
    public List<String> missingForNascentSpirit(int openMeridians) {
        Set<String> missing = new LinkedHashSet<>();
        if (this.selfScript == null) missing.add("eotp.requirement.self_script");
        if (this.thesis == null) missing.add("eotp.requirement.thesis");
        if (this.landscape == null) missing.add("eotp.requirement.landscape");
        if (openMeridians < 3) missing.add("eotp.requirement.meridians");
        if (this.survivedTribulations < 1) missing.add("eotp.requirement.tribulation");
        return List.copyOf(missing);
    }
}

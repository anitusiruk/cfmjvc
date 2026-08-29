package com.echoesofthepast.cultivation;

import com.echoesofthepast.EOTPConfig;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Everything the world remembers about one cultivator. Kept in the world's own save data rather
 * than on the player entity, so it survives death without any special handling and can be read by
 * blocks that the player is nowhere near.
 */
public final class Cultivator {
    public static final Codec<Cultivator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Realm.CODEC.optionalFieldOf("realm", Realm.MORTAL).forGetter(c -> c.realm),
        CultivationPath.CODEC.optionalFieldOf("path", new CultivationPath()).forGetter(c -> c.path),
        SpiritualRoot.CODEC.optionalFieldOf("root", SpiritualRoot.NONE).forGetter(c -> c.root),
        Codec.unboundedMap(Meridian.CODEC, Codec.FLOAT).optionalFieldOf("meridians", Map.of()).forGetter(c -> c.meridianEffort),
        Codec.FLOAT.optionalFieldOf("qi", 0.0F).forGetter(c -> c.qi),
        PhaseBlend.CODEC.optionalFieldOf("qi_blend", PhaseBlend.EMPTY).forGetter(c -> c.qiBlend),
        Codec.BOOL.optionalFieldOf("core_formed", false).forGetter(c -> c.coreFormed),
        Codec.INT.optionalFieldOf("core_instability", 0).forGetter(c -> c.coreInstability),
        Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("tolerance", Map.of()).forGetter(c -> c.pillTolerance),
        Codec.STRING.listOf().optionalFieldOf("discoveries", java.util.List.of()).forGetter(c -> java.util.List.copyOf(c.discoveries)),
        Meridian.CODEC.optionalFieldOf("sealed_meridian").forGetter(c -> java.util.Optional.ofNullable(c.sealedMeridian)),
        Codec.INT.optionalFieldOf("sealed_until", 0).forGetter(c -> c.sealedUntil),
        Codec.INT.optionalFieldOf("failed_breakthroughs", 0).forGetter(c -> c.failedBreakthroughs),
        Codec.INT.optionalFieldOf("cloudsteps_used", 0).forGetter(c -> c.cloudstepsUsed)
    ).apply(instance, Cultivator::new));

    private Realm realm;
    /** Everything the cultivator has proven rather than accumulated. */
    private final CultivationPath path;
    private SpiritualRoot root;
    private final Map<Meridian, Float> meridianEffort;
    private float qi;
    private PhaseBlend qiBlend;
    private boolean coreFormed;
    private int coreInstability;
    private final Map<String, Float> pillTolerance;
    private final Set<String> discoveries;
    private @Nullable Meridian sealedMeridian;
    private int sealedUntil;
    private int failedBreakthroughs;
    private int cloudstepsUsed;

    public Cultivator() {
        this(Realm.MORTAL, new CultivationPath(), SpiritualRoot.NONE, Map.of(), 0.0F, PhaseBlend.EMPTY, false, 0,
            Map.of(), java.util.List.of(), java.util.Optional.empty(), 0, 0, 0);
    }

    private Cultivator(
        Realm realm,
        CultivationPath path,
        SpiritualRoot root,
        Map<Meridian, Float> meridianEffort,
        float qi,
        PhaseBlend qiBlend,
        boolean coreFormed,
        int coreInstability,
        Map<String, Float> pillTolerance,
        java.util.List<String> discoveries,
        java.util.Optional<Meridian> sealedMeridian,
        int sealedUntil,
        int failedBreakthroughs,
        int cloudstepsUsed
    ) {
        this.realm = realm;
        this.path = path;
        this.root = root;
        this.meridianEffort = new EnumMap<>(Meridian.class);
        this.meridianEffort.putAll(meridianEffort);
        this.qi = qi;
        this.qiBlend = qiBlend;
        this.coreFormed = coreFormed;
        this.coreInstability = coreInstability;
        this.pillTolerance = new HashMap<>(pillTolerance);
        this.discoveries = new HashSet<>(discoveries);
        this.sealedMeridian = sealedMeridian.orElse(null);
        this.sealedUntil = sealedUntil;
        this.failedBreakthroughs = failedBreakthroughs;
        this.cloudstepsUsed = cloudstepsUsed;
    }

    // ------------------------------------------------------------------------------------- realms

    public Realm realm() {
        return this.realm;
    }

    /** Everything this cultivator has demonstrated: Witnesses, Script, Verses, Thesis, Landscape. */
    public CultivationPath path() {
        return this.path;
    }

    /**
     * Whether the next realm can be attempted at all. There is no bar to fill: each realm asks for a
     * different kind of proof, and the ritual itself is where that proof gets tested.
     */
    public boolean readyToBreakThrough() {
        return switch (this.realm) {
            case MORTAL -> this.path.hasAllWitnesses();
            // Foundation reads the tendencies the world has already filed; it needs enough of them.
            case BREATH_GATHERING -> this.path.strongestTendencies(3).size() >= 3;
            case FOUNDATION -> this.path.masteredVerses().size() >= 3;
            case GOLDEN_CORE -> this.path.readyForNascentSpirit(this.openMeridianCount());
            case NASCENT_SPIRIT -> false;
        };
    }

    public void advanceRealm() {
        this.realm = this.realm.next();
        if (this.realm == Realm.GOLDEN_CORE) {
            this.coreFormed = true;
        }
    }

    /**
     * A collapsed ritual leaves Discord on whichever principle was failing, rather than deleting
     * progress the player earned by demonstration.
     */
    public void recordDiscord(Principle principle) {
        this.path.addDiscord(principle);
        this.failedBreakthroughs++;
    }

    public int failedBreakthroughs() {
        return this.failedBreakthroughs;
    }

    public void forgiveFailure() {
        this.failedBreakthroughs = Math.max(0, this.failedBreakthroughs - 1);
        this.path.suppressOneDiscord();
    }

    // ------------------------------------------------------------------------------------- roots

    public SpiritualRoot root() {
        return this.root;
    }

    public void setRoot(SpiritualRoot root) {
        this.root = root;
    }

    // --------------------------------------------------------------------------------- meridians

    public float meridianEffort(Meridian meridian) {
        return this.meridianEffort.getOrDefault(meridian, 0.0F);
    }

    /**
     * Practising the thing a meridian governs. Returns true if this was the push that opened it.
     */
    public boolean practise(Meridian meridian, float amount) {
        boolean wasOpen = this.isOpen(meridian);
        float before = this.meridianEffort(meridian);
        float after = before + amount * (float) EOTPConfig.cultivationRate();
        this.meridianEffort.put(meridian, after);
        return !wasOpen && this.isOpen(meridian);
    }

    /** True once any one channel has had real work put into it: the Witness of Self. */
    public boolean hasSubstantialPractice() {
        for (Meridian meridian : Meridian.VALUES) {
            if (this.meridianEffort(meridian) >= meridian.effortRequired() * 0.6F) return true;
        }
        return false;
    }

    /** Forces a meridian the rest of the way open, as a meridian-opening pill does. */
    public void forceOpen(Meridian meridian) {
        this.meridianEffort.put(meridian, meridian.effortRequired());
    }

    /**
     * A channel opens once it has been practised enough, but never before the body can hold Qi at
     * all. Mortals still accumulate practice, so nothing done before Breath Gathering is wasted.
     */
    public boolean isOpen(Meridian meridian) {
        if (this.realm == Realm.MORTAL) return false;
        if (this.meridianEffort(meridian) < meridian.effortRequired()) return false;
        // The lower field is not a sixth skill: it is what the other channels add up to.
        if (meridian == Meridian.DANTIAN && this.practisedChannelCount() < 3) return false;
        return true;
    }

    /** How many other channels have meaningful practice in them, which is what Dantian asks for. */
    public int practisedChannelCount() {
        int count = 0;
        for (Meridian meridian : Meridian.VALUES) {
            if (meridian == Meridian.DANTIAN) continue;
            if (this.meridianEffort(meridian) >= meridian.effortRequired() * 0.5F) count++;
        }
        return count;
    }

    /** Progress toward a channel, for the compass and the ritual glyphs. */
    public float meridianProgress(Meridian meridian) {
        return Mth.clamp(this.meridianEffort(meridian) / meridian.effortRequired(), 0.0F, 1.0F);
    }

    /** An open meridian that has been needled shut is not usable, but its Qi goes elsewhere. */
    public boolean isUsable(Meridian meridian, int gameTime) {
        return this.isOpen(meridian) && !this.isSealed(meridian, gameTime);
    }

    public int openMeridianCount() {
        int count = 0;
        for (Meridian meridian : Meridian.VALUES) {
            if (this.isOpen(meridian)) count++;
        }
        return count;
    }

    public boolean isSealed(Meridian meridian, int gameTime) {
        return this.sealedMeridian == meridian && gameTime < this.sealedUntil;
    }

    public @Nullable Meridian sealedMeridian(int gameTime) {
        return gameTime < this.sealedUntil ? this.sealedMeridian : null;
    }

    /**
     * Needling a channel shut on purpose. Qi that would have gone there is routed into the channels
     * that remain, which is how an advanced cultivator retunes their abilities.
     */
    public void sealMeridian(Meridian meridian, int untilGameTime) {
        this.sealedMeridian = meridian;
        this.sealedUntil = untilGameTime;
    }

    /**
     * Multiplier on techniques belonging to a meridian. Sealing one channel makes the others
     * stronger, which is the whole point of the needle.
     */
    public float channelStrength(Meridian meridian, int gameTime) {
        if (!this.isUsable(meridian, gameTime)) return 0.0F;
        float base = 1.0F;
        Meridian sealed = this.sealedMeridian(gameTime);
        if (sealed != null && sealed != meridian && this.isOpen(sealed)) {
            base += 0.35F;
        }
        // A Landscape whose relationship suits the channel makes its techniques land harder, and a
        // Core Thesis about the same principle pushes them further still.
        InnerLandscape landscape = this.path.landscape();
        if (landscape != null) {
            base *= switch (meridian) {
                case HAND -> landscape.returnBonus();
                case FOOT -> landscape.growthBonus();
                case HEART -> landscape.stabilityBonus();
                case CROWN, DANTIAN -> landscape.transformationBonus();
            };
        } else {
            base *= this.root.powerMultiplier();
        }
        // Outstanding Discord makes everything run rough until the principle is demonstrated again.
        if (this.path.hasDiscord()) {
            base *= 1.0F - Math.min(0.4F, this.path.discord().size() * 0.15F);
        }
        return base;
    }

    // ------------------------------------------------------------------------------- personal qi

    public float qi() {
        return this.qi;
    }

    public float qiCapacity() {
        float capacity = this.realm.qiCapacity();
        if (this.isOpen(Meridian.DANTIAN)) capacity *= 1.5F;
        if (this.coreFormed) capacity *= 1.0F + 0.4F * (1.0F - this.coreInstabilityFraction());
        return capacity;
    }

    public float qiFillRatio() {
        float capacity = this.qiCapacity();
        return capacity <= 0.0F ? 0.0F : Mth.clamp(this.qi / capacity, 0.0F, 1.0F);
    }

    public PhaseBlend qiBlend() {
        return this.qiBlend.isEmpty() ? this.naturalBlend() : this.qiBlend;
    }

    /**
     * The character of Qi this body makes on its own. An imprinted Landscape speaks first: affinity
     * is supposed to be the memory of a place that was actually cultivated.
     */
    public PhaseBlend naturalBlend() {
        InnerLandscape landscape = this.path.landscape();
        if (landscape != null) return landscape.blend();
        return this.root.blend();
    }

    /** How well this body takes in an arriving phase. */
    public float affinityWith(Phase phase) {
        InnerLandscape landscape = this.path.landscape();
        return landscape != null ? landscape.affinityWith(phase) : this.root.affinityWith(phase);
    }

    public float addQi(float amount, PhaseBlend flavour) {
        float capacity = this.qiCapacity();
        float efficiency = 1.0F;
        Phase dominant = flavour.dominant();
        if (dominant != null) {
            efficiency = this.affinityWith(dominant);
        }
        float gained = Math.min(amount * efficiency, capacity - this.qi);
        if (gained <= 0.0F) return 0.0F;
        this.qiBlend = PhaseBlend.mix(this.qiBlend, this.qi, flavour, gained);
        this.qi += gained;
        return gained;
    }

    public boolean spendQi(float cost) {
        if (this.qi + 1.0E-3F < cost) return false;
        this.qi -= cost;
        if (this.qi <= 1.0E-3F) {
            this.qi = 0.0F;
            this.qiBlend = PhaseBlend.EMPTY;
        }
        return true;
    }

    /** Natural recovery. Below a golden core this is slow and stops entirely when the body is hurt. */
    public void regenerateQi(float ambient, boolean resting) {
        if (this.realm == Realm.MORTAL) return;
        float rate = this.realm.hasPersistentQi() ? 0.35F : 0.12F;
        if (resting) rate *= 3.0F;
        rate *= 1.0F + ambient;
        rate *= (float) EOTPConfig.qiRegenRate();
        if (this.coreInstability > 0) rate *= 0.4F;
        this.addQi(rate, this.naturalBlend());
    }

    /** Without a core, Qi held in the body seeps away when the cultivator is not concentrating. */
    public void seepQi() {
        if (this.realm.hasPersistentQi()) return;
        this.qi = Math.max(0.0F, this.qi - this.qiCapacity() * 0.002F);
    }

    // ------------------------------------------------------------------------------- golden core

    public boolean coreFormed() {
        return this.coreFormed;
    }

    public int coreInstability() {
        return this.coreInstability;
    }

    public float coreInstabilityFraction() {
        return Mth.clamp(this.coreInstability / 12000.0F, 0.0F, 1.0F);
    }

    /**
     * Dying does not destroy a core, it shakes it loose. The cultivator keeps their realm but runs
     * at reduced capacity until the core settles, which is a recovery mechanic rather than a
     * punishment.
     */
    public void destabiliseCore(int ticks) {
        if (!this.coreFormed) return;
        this.coreInstability = Math.min(24000, this.coreInstability + ticks);
        this.qi = Math.min(this.qi, this.qiCapacity());
    }

    public void settleCore(int ticks) {
        this.coreInstability = Math.max(0, this.coreInstability - ticks);
    }

    // ----------------------------------------------------------------------------------- pills

    /**
     * Diminishing returns on pills. Tolerance rises sharply when a pill is eaten again soon and
     * decays over time, so hoarding stacks is pointless but keeping one for an emergency is not.
     */
    public float pillEffectiveness(String pillId) {
        float tolerance = this.pillTolerance.getOrDefault(pillId, 0.0F);
        return 1.0F / (1.0F + tolerance);
    }

    public void notePillTaken(String pillId) {
        this.pillTolerance.merge(pillId, 1.0F, Float::sum);
    }

    public void decayTolerance(float amount) {
        this.pillTolerance.replaceAll((id, tolerance) -> Math.max(0.0F, tolerance - amount));
        this.pillTolerance.entrySet().removeIf(entry -> entry.getValue() <= 0.0F);
    }

    // ------------------------------------------------------------------------------ discoveries

    public boolean knows(String discovery) {
        return this.discoveries.contains(discovery);
    }

    /** @return true if this was new knowledge. */
    public boolean learn(String discovery) {
        return this.discoveries.add(discovery);
    }

    public Set<String> discoveries() {
        return Set.copyOf(this.discoveries);
    }

    // ------------------------------------------------------------------------------- cloudstep

    public int cloudstepsUsed() {
        return this.cloudstepsUsed;
    }

    public void setCloudstepsUsed(int used) {
        this.cloudstepsUsed = used;
    }
}

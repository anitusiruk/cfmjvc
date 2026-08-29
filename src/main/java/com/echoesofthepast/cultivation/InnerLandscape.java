package com.echoesofthepast.cultivation;

import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;

/**
 * The memory of a place somebody actually cultivated, carried inside them.
 *
 * <p>This replaces picking an element from a list. Affinity here is a consequence: it is whatever
 * the garden the cultivator built and kept alive turned out to be, including how cyclical, how
 * stable and how transformative it was.
 *
 * @param dominant     the phase the ecology produced most of
 * @param secondary    the phase supporting it
 * @param relationship the strongest relationship the place expressed
 * @param cyclical     how much of what was produced was also consumed, from 0 to 1
 * @param stability    how free of turbulence the place stayed, from 0 to 1
 * @param diversity    how many distinct phases and living things it sustained, from 0 to 1
 * @param scars        marks left by surviving Heaven
 */
public record InnerLandscape(
    Phase dominant,
    Phase secondary,
    Principle relationship,
    float cyclical,
    float stability,
    float diversity,
    List<HeavenScar> scars
) {
    public static final Codec<InnerLandscape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Phase.CODEC.fieldOf("dominant").forGetter(InnerLandscape::dominant),
        Phase.CODEC.fieldOf("secondary").forGetter(InnerLandscape::secondary),
        Principle.CODEC.fieldOf("relationship").forGetter(InnerLandscape::relationship),
        Codec.FLOAT.fieldOf("cyclical").forGetter(InnerLandscape::cyclical),
        Codec.FLOAT.fieldOf("stability").forGetter(InnerLandscape::stability),
        Codec.FLOAT.fieldOf("diversity").forGetter(InnerLandscape::diversity),
        HeavenScar.CODEC.listOf().optionalFieldOf("scars", List.of()).forGetter(InnerLandscape::scars)
    ).apply(instance, InnerLandscape::new));

    /** The character of Qi this cultivator now naturally produces and absorbs best. */
    public PhaseBlend blend() {
        return PhaseBlend.of(this.dominant, this.dominant).plus(PhaseBlend.of(this.secondary, 0.5F)).normalised();
    }

    /** How efficiently personal Qi absorbs an arriving phase. */
    public float affinityWith(Phase phase) {
        if (phase == this.dominant) return 1.45F;
        if (phase == this.secondary) return 1.2F;
        if (this.dominant.generates() == phase || this.dominant.generatedBy() == phase) return 1.0F;
        return 0.7F;
    }

    /** A highly cyclical place makes returning and circulating techniques stronger. */
    public float returnBonus() {
        return 1.0F + this.cyclical * 0.35F + (this.relationship == Principle.RETURN ? 0.15F : 0.0F);
    }

    /** A stable place makes barriers and alchemy hold their conditions. */
    public float stabilityBonus() {
        return 1.0F + this.stability * 0.3F + (this.relationship == Principle.PRESERVATION ? 0.15F : 0.0F);
    }

    /** A transformative place makes wheels, fire techniques and the Ding react harder. */
    public float transformationBonus() {
        return 1.0F + (this.relationship == Principle.TRANSFORMATION ? 0.25F : 0.0F) + this.diversity * 0.15F;
    }

    /** A biologically varied place makes plants and restorative work go further. */
    public float growthBonus() {
        return 1.0F + this.diversity * 0.35F + (this.relationship == Principle.GROWTH ? 0.15F : 0.0F);
    }

    public boolean hasScar(HeavenScar scar) {
        return this.scars.contains(scar);
    }

    /** Adds a mark, keeping only the most recent few. */
    public InnerLandscape withScar(HeavenScar scar) {
        Set<HeavenScar> kept = new LinkedHashSet<>(this.scars);
        kept.remove(scar);
        kept.add(scar);
        List<HeavenScar> ordered = List.copyOf(kept);
        if (ordered.size() > HeavenScar.MAXIMUM_HELD) {
            ordered = List.copyOf(ordered.subList(ordered.size() - HeavenScar.MAXIMUM_HELD, ordered.size()));
        }
        return new InnerLandscape(this.dominant, this.secondary, this.relationship, this.cyclical, this.stability, this.diversity, ordered);
    }

    /**
     * The name a place earns from what it became, e.g. a water-dominant, wood-supported, returning
     * garden reads as a Moonlit River Garden.
     */
    public Component name() {
        return Component.translatable(
            "eotp.landscape.name",
            Component.translatable(this.dominant.translationKey()),
            Component.translatable(this.secondary.translationKey()),
            Component.translatable(this.relationship.translationKey())
        );
    }

    public Component describe() {
        return Component.translatable(
            "eotp.landscape.describe",
            this.name(),
            Math.round(this.cyclical * 100.0F),
            Math.round(this.stability * 100.0F),
            Math.round(this.diversity * 100.0F),
            this.scars.size()
        );
    }
}

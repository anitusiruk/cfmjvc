package com.echoesofthepast.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Two to four principles proved to hold together at once. A Verse is not a recipe: it is evidence
 * that the cultivator understands a relationship between systems, which is why it has to be
 * demonstrated in the world before the scroll will awaken.
 */
public record Verse(List<Principle> principles) {
    public static final int MINIMUM = 2;
    public static final int MAXIMUM = 4;

    public static final Codec<Verse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Principle.CODEC.listOf().fieldOf("principles").forGetter(Verse::principles)
    ).apply(instance, Verse::new));

    public Verse {
        // Canonical ordering keeps "Flow + Return" and "Return + Flow" the same mastered Verse.
        EnumSet<Principle> unique = EnumSet.noneOf(Principle.class);
        unique.addAll(principles);
        principles = List.copyOf(new ArrayList<>(unique));
    }

    public static Verse of(Principle... principles) {
        return new Verse(List.of(principles));
    }

    public boolean isWellFormed() {
        return this.principles.size() >= MINIMUM && this.principles.size() <= MAXIMUM;
    }

    public boolean contains(Principle principle) {
        return this.principles.contains(principle);
    }

    public Component describe() {
        Component text = Component.translatable(this.principles.get(0).translationKey());
        for (int index = 1; index < this.principles.size(); index++) {
            text = Component.translatable(
                "eotp.verse.join", text, Component.translatable(this.principles.get(index).translationKey())
            );
        }
        return text;
    }

    /** Stable key used for tooltips, storage and comparison. */
    public String key() {
        StringBuilder builder = new StringBuilder();
        for (Principle principle : this.principles) {
            if (!builder.isEmpty()) builder.append('+');
            builder.append(principle.getSerializedName());
        }
        return builder.toString();
    }
}

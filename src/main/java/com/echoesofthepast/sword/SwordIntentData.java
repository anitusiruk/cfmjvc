package com.echoesofthepast.sword;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The tally of experiences a bound sword carries. The dominant entry decides the blade's technique;
 * a sword used for everything stays a generalist and gets no signature at all.
 */
public record SwordIntentData(Map<SwordIntent, Integer> tallies) {
    public static final int MASTERY_THRESHOLD = 120;
    /** How far ahead of the runner up an intent must be before it defines the blade. */
    private static final float DOMINANCE = 1.4F;

    public static final SwordIntentData EMPTY = new SwordIntentData(Map.of());

    public static final Codec<SwordIntentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(SwordIntent.CODEC, Codec.INT).optionalFieldOf("tallies", Map.of()).forGetter(SwordIntentData::tallies)
    ).apply(instance, SwordIntentData::new));

    public int get(SwordIntent intent) {
        return this.tallies.getOrDefault(intent, 0);
    }

    public SwordIntentData plus(SwordIntent intent, int amount) {
        Map<SwordIntent, Integer> updated = new EnumMap<>(SwordIntent.class);
        updated.putAll(this.tallies);
        updated.merge(intent, amount, Integer::sum);
        return new SwordIntentData(Map.copyOf(updated));
    }

    public int total() {
        int total = 0;
        for (int value : this.tallies.values()) {
            total += value;
        }
        return total;
    }

    /**
     * The intent that has taken hold, or null while the blade is still undecided.
     */
    public @Nullable SwordIntent dominant() {
        SwordIntent best = null;
        int bestValue = 0;
        int secondValue = 0;
        for (SwordIntent intent : SwordIntent.VALUES) {
            int value = this.get(intent);
            if (value > bestValue) {
                secondValue = bestValue;
                bestValue = value;
                best = intent;
            } else if (value > secondValue) {
                secondValue = value;
            }
        }
        if (best == null || bestValue < 25) return null;
        return bestValue >= secondValue * DOMINANCE ? best : null;
    }

    /** How far the dominant intent has matured, from 0 to 1. */
    public float mastery() {
        SwordIntent dominant = this.dominant();
        return dominant == null ? 0.0F : Math.min(1.0F, this.get(dominant) / (float) MASTERY_THRESHOLD);
    }
}

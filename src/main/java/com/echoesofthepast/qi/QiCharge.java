package com.echoesofthepast.qi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

/**
 * Qi held inside an item: a spirit stone, a hairpin, a charged talisman, a bound sword.
 *
 * <p>Item Qi is deliberately simpler than block Qi - no turbulence, no leakage - because an item in
 * a pocket is not plumbed into anything.
 */
public record QiCharge(float amount, float capacity, PhaseBlend blend) {
    public static final Codec<QiCharge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("amount").forGetter(QiCharge::amount),
        Codec.FLOAT.fieldOf("capacity").forGetter(QiCharge::capacity),
        PhaseBlend.CODEC.optionalFieldOf("blend", PhaseBlend.EMPTY).forGetter(QiCharge::blend)
    ).apply(instance, QiCharge::new));

    public static QiCharge empty(float capacity) {
        return new QiCharge(0.0F, capacity, PhaseBlend.EMPTY);
    }

    public static QiCharge full(float capacity, PhaseBlend blend) {
        return new QiCharge(capacity, capacity, blend);
    }

    public float fillRatio() {
        return this.capacity <= 0.0F ? 0.0F : Mth.clamp(this.amount / this.capacity, 0.0F, 1.0F);
    }

    public boolean has(float cost) {
        return this.amount + 1.0E-3F >= cost;
    }

    public QiCharge minus(float cost) {
        return new QiCharge(Math.max(0.0F, this.amount - cost), this.capacity, this.amount - cost <= 0.0F ? PhaseBlend.EMPTY : this.blend);
    }

    public QiCharge plus(float gain, PhaseBlend flavour) {
        float added = Math.min(gain, this.capacity - this.amount);
        if (added <= 0.0F) return this;
        return new QiCharge(this.amount + added, this.capacity, PhaseBlend.mix(this.blend, this.amount, flavour, added));
    }

    /** Spirit stones and lamps dim as they empty; this is the brightness to render them at. */
    public int lightLevel(int maximum) {
        return Math.round(this.fillRatio() * maximum);
    }
}

package com.echoesofthepast.qi;

import com.echoesofthepast.seal.SealRule;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;

/**
 * A short, discrete burst of Qi. Meridian threads, bells, abacuses and prisms all speak in pulses
 * rather than continuous flow, which is what makes timing and logic possible without a GUI.
 *
 * @param id     identity of the burst, so a pulse never doubles back through the same thread
 * @param amount how much Qi the burst carries
 * @param blend  what the burst is made of
 * @param rule   the seal stamped on whatever emitted the pulse, if any
 * @param hops   blocks travelled so far; pulses fade with distance
 */
public record QiPulse(long id, float amount, PhaseBlend blend, @Nullable SealRule rule, int hops) {
    private static final AtomicLong NEXT_ID = new AtomicLong(1L);

    public static QiPulse create(float amount, PhaseBlend blend) {
        return new QiPulse(NEXT_ID.getAndIncrement(), amount, blend, null, 0);
    }

    public static QiPulse create(float amount, PhaseBlend blend, @Nullable SealRule rule) {
        return new QiPulse(NEXT_ID.getAndIncrement(), amount, blend, rule, 0);
    }

    public QiPulse advanced(float loss) {
        return new QiPulse(this.id, Math.max(0.0F, this.amount - loss), this.blend, this.rule, this.hops + 1);
    }

    public QiPulse withRule(@Nullable SealRule newRule) {
        return new QiPulse(this.id, this.amount, this.blend, newRule, this.hops);
    }

    public QiPulse withAmount(float newAmount) {
        return new QiPulse(this.id, newAmount, this.blend, this.rule, this.hops);
    }

    public boolean isSpent() {
        return this.amount <= 0.05F;
    }

    /** Pulse strength expressed as a musical step, which is how bells and resonance stones read it. */
    public int tone() {
        if (this.amount < 2.0F) return 0;
        if (this.amount < 6.0F) return 1;
        if (this.amount < 14.0F) return 2;
        if (this.amount < 30.0F) return 3;
        if (this.amount < 60.0F) return 4;
        return 5;
    }
}

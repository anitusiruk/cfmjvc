package com.echoesofthepast.qi;

/** A quantity of Qi in transit, together with the character it carries. */
public record QiPacket(float amount, PhaseBlend blend) {
    public static final QiPacket EMPTY = new QiPacket(0.0F, PhaseBlend.EMPTY);

    public boolean isEmpty() {
        return this.amount <= 1.0E-3F;
    }

    public QiPacket scaled(float factor) {
        return new QiPacket(this.amount * factor, this.blend);
    }

    public QiPacket withAmount(float newAmount) {
        return new QiPacket(newAmount, this.blend);
    }

    public QiPacket withBlend(PhaseBlend newBlend) {
        return new QiPacket(this.amount, newBlend);
    }
}

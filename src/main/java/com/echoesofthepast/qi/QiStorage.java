package com.echoesofthepast.qi;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A body of Qi held by a block, an artifact or a person.
 *
 * <p>Qi is stored as a single amount plus the {@link PhaseBlend} it carries. Inserting Qi of a
 * different character does not create a second pool - it dilutes the existing one, which is how
 * fire Qi can spoil a reservoir of water Qi.
 */
public class QiStorage {
    private final float baseCapacity;
    /** Raised by resonance between devices, jade work and formation support. */
    private float capacityMultiplier = 1.0F;
    private float amount;
    private PhaseBlend blend = PhaseBlend.EMPTY;

    /**
     * Rising when incompatible phases are forced together. Turbulent Qi leaks, sparks, and is what
     * Lingzhi feeds on.
     */
    private float turbulence;

    public QiStorage(float capacity) {
        this.baseCapacity = capacity;
    }

    public float capacity() {
        return this.baseCapacity * this.capacityMultiplier;
    }

    public void setCapacityMultiplier(float multiplier) {
        this.capacityMultiplier = Math.max(0.1F, multiplier);
        if (this.amount > this.capacity()) {
            this.amount = this.capacity();
        }
    }

    public float capacityMultiplier() {
        return this.capacityMultiplier;
    }

    public float amount() {
        return this.amount;
    }

    public float space() {
        return Math.max(0.0F, this.capacity() - this.amount);
    }

    public float fillRatio() {
        float capacity = this.capacity();
        return capacity <= 0.0F ? 0.0F : Mth.clamp(this.amount / capacity, 0.0F, 1.0F);
    }

    public boolean isEmpty() {
        return this.amount <= 1.0E-3F;
    }

    public PhaseBlend blend() {
        return this.blend;
    }

    public float turbulence() {
        return this.turbulence;
    }

    public void addTurbulence(float value) {
        this.turbulence = Mth.clamp(this.turbulence + value, 0.0F, 1.0F);
    }

    /** Turbulence settles slowly on its own; formations and Lingzhi speed this up. */
    public void calmTurbulence(float value) {
        this.turbulence = Math.max(0.0F, this.turbulence - value);
    }

    /**
     * @return the amount actually accepted.
     */
    public float insert(float requested, PhaseBlend flavour, boolean simulate) {
        if (requested <= 0.0F) return 0.0F;
        float accepted = Math.min(requested, this.space());
        if (accepted <= 0.0F || simulate) return accepted;

        PhaseBlend mixed = PhaseBlend.mix(this.blend, this.amount, flavour, accepted);
        // Forcing in Qi that fights what is already stored roughens the whole pool.
        float clash = Math.max(0.0F, this.blend.isEmpty() ? 0.0F : mixed.turbulence() - this.blend.turbulence());
        this.addTurbulence(clash * (accepted / Math.max(1.0F, this.capacity())) * 4.0F);
        this.blend = mixed;
        this.amount += accepted;
        return accepted;
    }

    public float insert(float requested, PhaseBlend flavour) {
        return this.insert(requested, flavour, false);
    }

    /**
     * @return a packet carrying the amount removed and the character it was carrying.
     */
    public QiPacket extract(float requested, boolean simulate) {
        if (requested <= 0.0F || this.isEmpty()) return QiPacket.EMPTY;
        float removed = Math.min(requested, this.amount);
        if (!simulate) {
            this.amount -= removed;
            if (this.amount <= 1.0E-3F) {
                this.amount = 0.0F;
                this.blend = PhaseBlend.EMPTY;
            }
        }
        return new QiPacket(removed, this.blend);
    }

    public QiPacket extract(float requested) {
        return this.extract(requested, false);
    }

    /** Drains Qi only if the whole cost can be paid, for abilities that must not half-fire. */
    public boolean tryConsume(float cost) {
        if (this.amount + 1.0E-3F < cost) return false;
        this.extract(cost, false);
        return true;
    }

    public void setBlend(PhaseBlend blend) {
        this.blend = blend;
    }

    /**
     * Turbulent Qi bleeds away. Insulation (jade, celadon, ink work) is a multiplier below one.
     *
     * @return the amount lost, which is what Lingzhi and Echo residue feed on.
     */
    public float leak(float insulation) {
        if (this.isEmpty()) return 0.0F;
        float rate = (0.0004F + this.turbulence * 0.01F) * Mth.clamp(insulation, 0.0F, 4.0F);
        float lost = Math.min(this.amount, this.capacity() * rate);
        this.amount -= lost;
        this.calmTurbulence(0.0008F);
        if (this.amount <= 1.0E-3F) {
            this.amount = 0.0F;
            this.blend = PhaseBlend.EMPTY;
        }
        return lost;
    }

    public void save(ValueOutput output, String key) {
        ValueOutput child = output.child(key);
        child.putFloat("amount", this.amount);
        child.putFloat("turbulence", this.turbulence);
        child.store("blend", PhaseBlend.CODEC, this.blend);
    }

    public void load(ValueInput input, String key) {
        input.child(key).ifPresent(child -> {
            this.amount = Mth.clamp(child.getFloatOr("amount", 0.0F), 0.0F, this.capacity());
            this.turbulence = Mth.clamp(child.getFloatOr("turbulence", 0.0F), 0.0F, 1.0F);
            this.blend = child.read("blend", PhaseBlend.CODEC).orElse(PhaseBlend.EMPTY);
        });
    }

    /** Human readable state, used by the compass and the mirrors instead of a number readout. */
    public String describe() {
        int percent = Math.round(this.fillRatio() * 100.0F);
        return percent + "% " + this.blend + (this.turbulence > 0.25F ? " (turbulent)" : "");
    }
}

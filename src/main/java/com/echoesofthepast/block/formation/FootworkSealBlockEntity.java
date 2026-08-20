package com.echoesofthepast.block.formation;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.sound.Resonance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class FootworkSealBlockEntity extends QiDeviceBlockEntity {
    /** Ticks before the same seal will read again, so one crossing is one signal. */
    private static final int DEBOUNCE = 8;

    private int quietUntil;

    public FootworkSealBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.FOOTWORK_SEAL.get(), pos, state, 30.0F);
    }

    /** The gaits a seal can tell apart, in order of the signal strength they produce. */
    private enum Gait {
        SNEAK(2.0F, Resonance.Tone.DEEP),
        WALK(6.0F, Resonance.Tone.LOW),
        RUN(16.0F, Resonance.Tone.MIDDLE),
        LEAP(32.0F, Resonance.Tone.HIGH),
        CLOUDSTEP(64.0F, Resonance.Tone.BRIGHT);

        private final float strength;
        private final Resonance.Tone tone;

        Gait(float strength, Resonance.Tone tone) {
            this.strength = strength;
            this.tone = tone;
        }
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        // Signals are produced on contact; between crossings the seal does nothing at all.
    }

    public void readGait(Entity entity) {
        if (!(this.level instanceof ServerLevel level)) return;
        if (this.age < this.quietUntil) return;
        this.quietUntil = this.age + DEBOUNCE;

        Gait gait = classify(entity);
        PhaseBlend blend = this.storage.isEmpty() ? PhaseBlend.of(com.echoesofthepast.qi.Phase.WOOD) : this.storage.blend();

        // The seal spends whatever it holds to make the signal, and falls back to a faint one if it
        // has no Qi at all. That means an unfed seal is still a detector, just a quiet one.
        float available = Math.min(this.storage.amount(), gait.strength);
        if (available > 0.5F) {
            this.storage.extract(available, false);
        }
        float strength = Math.max(gait.strength * 0.25F, available);

        QiPulse pulse = QiPulse.create(strength, blend);
        for (Direction side : Direction.values()) {
            if (side == Direction.DOWN) continue;
            QiNet.sendPulse(level, this.worldPosition, side, pulse);
        }
        Resonance.emit(level, this.worldPosition, gait.tone, strength);

        // Practising footwork over a seal is one of the ways the foot channel opens.
        if (entity instanceof Player player && (gait == Gait.LEAP || gait == Gait.CLOUDSTEP)) {
            Cultivation.practise(player, Meridian.FOOT, gait == Gait.CLOUDSTEP ? 1.5F : 0.5F);
        }
    }

    private static Gait classify(Entity entity) {
        if (entity instanceof Player player) {
            Cultivator cultivator = Cultivation.of(player);
            if (cultivator != null && cultivator.cloudstepsUsed() > 0 && !player.onGround()) {
                return Gait.CLOUDSTEP;
            }
        }
        if (!entity.onGround()) return Gait.LEAP;
        if (entity.isShiftKeyDown()) return Gait.SNEAK;
        if (entity.isSprinting()) return Gait.RUN;
        return Gait.WALK;
    }
}

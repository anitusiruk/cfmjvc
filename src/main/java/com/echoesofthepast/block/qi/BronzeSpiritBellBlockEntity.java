package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiPulseReceiver;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.sound.Resonance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The bell keeps a small amount of Qi so that it can be rung by hand as well as by a pulse, and it
 * passes pulses on rather than swallowing them, so bells can sit in the middle of a run of thread.
 */
public class BronzeSpiritBellBlockEntity extends QiDeviceBlockEntity implements QiPulseReceiver, ImprintTarget {
    /** Rings closer together than this are swallowed, so a stuck device cannot scream. */
    private static final int COOLDOWN = 6;

    private int quietUntil;

    public BronzeSpiritBellBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.BRONZE_SPIRIT_BELL.get(), pos, state, 24.0F);
    }

    @Override
    protected int idleParticleInterval() {
        return 60;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        // Qi that pools in a bell without being pulsed makes it hum faintly and then spill onwards.
        if (this.storage.fillRatio() > 0.9F) {
            QiNet.pushAround(level, this.worldPosition, this.storage, 4.0F, 0.9F);
        }
    }

    @Override
    public boolean onQiPulse(QiPulse pulse, Direction from) {
        if (!(this.level instanceof ServerLevel level)) return false;
        Resonance.Tone tone = Resonance.Tone.ofStrength(pulse.tone());
        this.ring(level, tone, pulse.amount());

        // The bell takes a small toll for reading the pulse and sends the rest on its way.
        QiPulse onward = pulse.advanced(Math.max(0.2F, pulse.amount() * 0.08F));
        if (!onward.isSpent()) {
            QiNet.sendPulse(level, this.worldPosition, from.getOpposite(), onward);
        }
        return true;
    }

    /** A bell struck by hand reports what it is holding rather than what passed through it. */
    public void strikeByHand() {
        if (!(this.level instanceof ServerLevel level)) return;
        int step = Math.round(this.storage.fillRatio() * 4.0F);
        this.ring(level, Resonance.Tone.ofStrength(step), this.storage.amount());
    }

    private void ring(ServerLevel level, Resonance.Tone tone, float strength) {
        if (this.age < this.quietUntil) return;
        this.quietUntil = this.age + COOLDOWN;

        level.playSound(null, this.worldPosition, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.2F, tone.pitch());
        Resonance.emit(level, this.worldPosition, tone, strength);
        QiVisuals.ring(level, net.minecraft.world.phys.Vec3.atCenterOf(this.worldPosition), 0.7, this.storage.blend().color(), 10);
    }

    @Override
    public boolean acceptImprint(ServerLevel level, ImprintAction action, net.minecraft.world.item.ItemStack offered) {
        if (action != ImprintAction.STRIKE) return false;
        this.strikeByHand();
        return true;
    }
}

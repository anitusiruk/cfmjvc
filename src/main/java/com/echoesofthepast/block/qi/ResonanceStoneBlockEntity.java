package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.sound.Resonance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ResonanceStoneBlockEntity extends QiDeviceBlockEntity implements Resonance.Listener {
    private Resonance.Tone tone = Resonance.Tone.MIDDLE;
    /** Set when the stone hears its note; the release happens on the next tick so the delay reads. */
    private boolean triggered;

    public ResonanceStoneBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.RESONANCE_STONE.get(), pos, state, 40.0F);
    }

    public Resonance.Tone tone() {
        return this.tone;
    }

    /** Cycles the note this stone answers to. */
    public Resonance.Tone retune() {
        this.tone = this.tone.next();
        this.setChanged();
        if (this.level instanceof ServerLevel level) {
            level.playSound(null, this.worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, this.tone.pitch());
        }
        return this.tone;
    }

    @Override
    protected int idleParticleInterval() {
        return 80;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (!this.triggered) return;
        this.triggered = false;

        // Everything the stone was holding goes out at once, in every direction, as one burst.
        if (this.storage.amount() < 1.0F) return;
        var packet = this.storage.extract(this.storage.amount(), false);
        QiPulse pulse = QiPulse.create(packet.amount(), packet.blend());
        for (Direction side : Direction.values()) {
            QiNet.sendPulse(level, this.worldPosition, side, pulse);
        }
        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition), packet.blend());
    }

    @Override
    public void onResonance(Resonance.Tone heard, BlockPos source, float strength) {
        if (heard != this.tone) return;
        if (source.equals(this.worldPosition)) return;
        this.triggered = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("tone", Resonance.Tone.CODEC, this.tone);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.tone = input.read("tone", Resonance.Tone.CODEC).orElse(Resonance.Tone.MIDDLE);
    }
}

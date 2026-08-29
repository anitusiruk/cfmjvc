package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiPulseReceiver;
import com.echoesofthepast.qi.QiStorage;
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

public class JadeAbacusBlockEntity extends QiDeviceBlockEntity implements QiPulseReceiver, Resonance.Listener {
    private static final float OUTPUT_STRENGTH = 6.0F;

    private AbacusCondition condition = AbacusCondition.FULLNESS;
    private int count;
    /** Prevents a condition that stays true from firing every single tick. */
    private boolean latched;

    public JadeAbacusBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.JADE_ABACUS.get(), pos, state, 30.0F);
    }

    public AbacusCondition condition() {
        return this.condition;
    }

    public AbacusCondition cycleCondition() {
        this.condition = this.condition.next();
        this.resetCount();
        return this.condition;
    }

    public void resetCount() {
        this.count = 0;
        this.latched = false;
        this.setChanged();
    }

    private int threshold() {
        return this.getBlockState().getValue(JadeAbacusBlock.BEADS);
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        boolean met = switch (this.condition) {
            case FULLNESS -> this.watchedFill(level) >= this.threshold() / 10.0F;
            case EMPTINESS -> this.watchedFill(level) <= this.threshold() / 10.0F;
            case INTERVAL -> this.age % (this.threshold() * 20) == 0;
            case PULSES, TONES -> this.count >= this.threshold();
        };

        if (!met) {
            this.latched = false;
            return;
        }
        if (this.latched && this.condition != AbacusCondition.INTERVAL) return;

        this.latched = true;
        if (this.condition == AbacusCondition.PULSES || this.condition == AbacusCondition.TONES) {
            this.count = 0;
        }
        this.fire(level);
    }

    /** The abacus reads whatever it has been set on top of. */
    private float watchedFill(ServerLevel level) {
        QiNode below = QiNet.nodeAt(level, this.worldPosition.below());
        if (below == null) return 0.0F;
        QiStorage storage = below.qiStorage(Direction.UP);
        return storage == null ? 0.0F : storage.fillRatio();
    }

    private void fire(ServerLevel level) {
        float amount = Math.max(OUTPUT_STRENGTH, this.storage.amount());
        var packet = this.storage.extract(this.storage.amount(), false);
        QiPulse pulse = QiPulse.create(amount, packet.isEmpty() ? this.storage.blend() : packet.blend());
        for (Direction side : Direction.values()) {
            if (side == Direction.DOWN) continue;
            QiNet.sendPulse(level, this.worldPosition, side, pulse);
        }
        level.playSound(null, this.worldPosition, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.5F, 1.4F);
        this.setChanged();
    }

    @Override
    public boolean onQiPulse(QiPulse pulse, Direction from) {
        if (this.condition == AbacusCondition.PULSES) {
            this.count++;
            this.setChanged();
        }
        this.storage.insert(pulse.amount(), pulse.blend(), false);
        return true;
    }

    @Override
    public void onResonance(Resonance.Tone tone, BlockPos source, float strength) {
        if (this.condition != AbacusCondition.TONES) return;
        this.count++;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("condition", AbacusCondition.CODEC, this.condition);
        output.putInt("count", this.count);
        output.putBoolean("latched", this.latched);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.condition = input.read("condition", AbacusCondition.CODEC).orElse(AbacusCondition.FULLNESS);
        this.count = input.getIntOr("count", 0);
        this.latched = input.getBooleanOr("latched", false);
    }
}

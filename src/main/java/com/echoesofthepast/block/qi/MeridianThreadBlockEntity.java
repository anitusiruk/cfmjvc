package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiPulseReceiver;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * One tick of travel per block, a fixed toll per block, and a pulse identity so a burst never
 * doubles back on itself. A long thread is therefore a delay line, and a branching thread is a
 * fan-out - both useful, both visible as the pulse crawls along the wall.
 */
public class MeridianThreadBlockEntity extends QiDeviceBlockEntity implements QiPulseReceiver {
    private static final float TOLL_PER_BLOCK = 0.25F;
    /** Threads are wire, not storage; anything that tries to fill them is refused. */
    private static final float CAPACITY = 0.0F;

    private @Nullable QiPulse carried;
    private @Nullable Direction carriedFrom;
    private long lastPulseId;

    public MeridianThreadBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.MERIDIAN_THREAD.get(), pos, state, CAPACITY);
    }

    @Override
    public @Nullable QiStorage qiStorage(@Nullable Direction side) {
        return null;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    public boolean onQiPulse(QiPulse pulse, Direction from) {
        if (pulse.id() == this.lastPulseId) return false;
        this.lastPulseId = pulse.id();
        this.carried = pulse.advanced(TOLL_PER_BLOCK);
        this.carriedFrom = from;
        return true;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        QiPulse pulse = this.carried;
        if (pulse == null) return;
        this.carried = null;
        Direction from = this.carriedFrom;
        this.carriedFrom = null;

        if (pulse.isSpent()) {
            QiVisuals.leak(level, this.worldPosition, pulse.blend(), pulse.amount());
            return;
        }

        // A pulse continues in every direction except the one it came from. Threads that branch
        // duplicate the pulse rather than splitting it, which is deliberate: thread is for signals,
        // and a bagua distributor is for dividing power.
        for (Direction side : Direction.values()) {
            if (side == from) continue;
            QiNet.sendPulse(level, this.worldPosition, side, pulse);
        }

        QiVisuals.line(level,
            Vec3.atCenterOf(this.worldPosition),
            Vec3.atCenterOf(this.worldPosition).add(0.0, 0.1, 0.0),
            pulse.blend().color(), 2);
    }
}

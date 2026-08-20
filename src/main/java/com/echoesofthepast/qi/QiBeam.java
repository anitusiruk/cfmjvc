package com.echoesofthepast.qi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A pulse travelling as a visible beam rather than through plumbing. Beams are what prisms work
 * with: they cross open space, stop at the first thing that can take them, and are blocked by
 * anything solid.
 */
public final class QiBeam {
    public static final int MAX_LENGTH = 16;

    private QiBeam() {}

    /**
     * Fires a beam and delivers the pulse to the first receiver it meets.
     *
     * @return true if something took the pulse.
     */
    public static boolean fire(ServerLevel level, BlockPos origin, Direction direction, QiPulse pulse) {
        BlockPos.MutableBlockPos cursor = origin.mutable();
        for (int step = 1; step <= MAX_LENGTH; step++) {
            cursor.set(origin).move(direction, step);
            BlockPos pos = cursor.immutable();

            QiPulse arriving = pulse.advanced(pulse.amount() * 0.02F);
            QiPulseReceiver receiver = QiNet.receiverAt(level, pos);
            if (receiver != null && receiver.onQiPulse(arriving, direction.getOpposite())) {
                draw(level, origin, pos, pulse);
                QiVisuals.pulseArrival(level, pos, direction.getOpposite(), arriving);
                return true;
            }

            QiNode node = QiNet.nodeAt(level, pos);
            if (node != null) {
                QiStorage storage = node.qiStorage(direction.getOpposite());
                if (storage != null && storage.insert(arriving.amount(), arriving.blend(), false) > 0.0F) {
                    draw(level, origin, pos, pulse);
                    return true;
                }
            }

            BlockState state = level.getBlockState(pos);
            if (state.isSolidRender()) {
                // The beam splashes against the wall; the Qi is simply lost.
                draw(level, origin, pos, pulse);
                QiVisuals.leak(level, pos, pulse.blend(), pulse.amount());
                return false;
            }
        }
        draw(level, origin, origin.relative(direction, MAX_LENGTH), pulse);
        return false;
    }

    private static void draw(ServerLevel level, BlockPos from, BlockPos to, QiPulse pulse) {
        QiVisuals.line(level, Vec3.atCenterOf(from), Vec3.atCenterOf(to), pulse.blend().color(), 3);
    }
}

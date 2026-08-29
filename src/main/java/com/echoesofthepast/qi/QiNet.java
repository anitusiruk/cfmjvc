package com.echoesofthepast.qi;

import com.echoesofthepast.registry.EOTPTags;
import com.echoesofthepast.world.DragonVeins;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

/** Shared plumbing for moving Qi between nodes and sending pulses along threads. */
public final class QiNet {
    private QiNet() {}

    public static @Nullable QiNode nodeAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof QiNode node ? node : null;
    }

    public static @Nullable QiPulseReceiver receiverAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof QiPulseReceiver receiver ? receiver : null;
    }

    /**
     * Pushes Qi from one storage into the neighbour on the given face, respecting both nodes'
     * transfer rates and losing whatever the route wastes.
     *
     * @return the amount that arrived at the far end.
     */
    public static float push(ServerLevel level, BlockPos from, QiStorage source, Direction side, float max, float efficiency) {
        if (source.isEmpty() || max <= 0.0F) return 0.0F;
        BlockPos targetPos = from.relative(side);
        QiNode target = nodeAt(level, targetPos);
        if (target == null || !target.acceptsQiFrom(side.getOpposite())) return 0.0F;

        QiStorage targetStorage = target.qiStorage(side.getOpposite());
        if (targetStorage == null) return 0.0F;

        // Qi only flows downhill: a full reservoir does not suck a flue dry.
        float head = source.fillRatio() - targetStorage.fillRatio();
        if (head <= 0.01F) return 0.0F;

        float offered = Math.min(max, Math.min(source.amount(), targetStorage.space()));
        offered = Math.min(offered, source.capacity() * head * 0.5F);
        if (offered <= 0.01F) return 0.0F;

        QiPacket drawn = source.extract(offered, false);
        float delivered = drawn.amount() * efficiency;
        float accepted = targetStorage.insert(delivered, drawn.blend(), false);
        float wasted = drawn.amount() - accepted;

        // Anything that did not arrive is simply gone; Qi is not conserved by bad plumbing.
        if (wasted > 0.02F) {
            QiVisuals.leak(level, from, drawn.blend(), wasted);
        }
        if (accepted > 0.05F) {
            QiVisuals.flow(level, from, targetPos, drawn.blend(), accepted);
            target.onQiArrived(side.getOpposite(), accepted);
        }
        return accepted;
    }

    /**
     * Pushes Qi to an arbitrary position rather than a face neighbour, which is how the bagua
     * distributor reaches the four corners around it.
     */
    public static float pushToPos(ServerLevel level, BlockPos from, QiStorage source, BlockPos to, float max, float efficiency) {
        if (source.isEmpty() || max <= 0.0F) return 0.0F;
        QiNode target = nodeAt(level, to);
        if (target == null) return 0.0F;
        QiStorage targetStorage = target.qiStorage(null);
        if (targetStorage == null) return 0.0F;

        float head = source.fillRatio() - targetStorage.fillRatio();
        if (head <= 0.01F) return 0.0F;

        float offered = Math.min(max, Math.min(source.amount(), targetStorage.space()));
        if (offered <= 0.01F) return 0.0F;

        QiPacket drawn = source.extract(offered, false);
        float accepted = targetStorage.insert(drawn.amount() * efficiency, drawn.blend(), false);
        if (accepted > 0.05F) {
            QiVisuals.flow(level, from, to, drawn.blend(), accepted);
        }
        return accepted;
    }

    /** Spreads Qi to every accepting neighbour, evenly. */
    public static float pushAround(ServerLevel level, BlockPos from, QiStorage source, float max, float efficiency) {
        float moved = 0.0F;
        for (Direction side : Direction.values()) {
            moved += push(level, from, source, side, max, efficiency);
        }
        return moved;
    }

    /**
     * Hands a pulse to whatever sits on the given face.
     *
     * @return true if something took it.
     */
    public static boolean sendPulse(ServerLevel level, BlockPos from, Direction side, QiPulse pulse) {
        if (pulse.isSpent()) return false;
        BlockPos targetPos = from.relative(side);
        QiPulseReceiver receiver = receiverAt(level, targetPos);
        if (receiver != null && receiver.onQiPulse(pulse, side.getOpposite())) {
            QiVisuals.pulseArrival(level, targetPos, side.getOpposite(), pulse);
            return true;
        }
        // A pulse with nowhere to go still tries to dump itself into any storage it can reach.
        QiNode node = nodeAt(level, targetPos);
        if (node != null && node.acceptsQiFrom(side.getOpposite())) {
            QiStorage storage = node.qiStorage(side.getOpposite());
            if (storage != null && storage.insert(pulse.amount(), pulse.blend(), false) > 0.0F) {
                QiVisuals.pulseArrival(level, targetPos, side.getOpposite(), pulse);
                return true;
            }
        }
        return false;
    }

    /**
     * Efficiency multiplier for a device standing here: dragon veins help, jade and celadon nearby
     * keep Qi from bleeding out of the walls.
     */
    public static float siteQuality(Level level, BlockPos pos) {
        float quality = 1.0F + DragonVeins.strength(level, pos) * 0.6F;
        int insulating = 0;
        for (Direction side : Direction.values()) {
            if (level.getBlockState(pos.relative(side)).is(EOTPTags.Blocks.QI_INSULATING)) {
                insulating++;
            }
        }
        return quality + insulating * 0.04F;
    }

    /** Leak multiplier for a device standing here; jade walls literally pay for themselves. */
    public static float insulation(Level level, BlockPos pos) {
        int insulating = 0;
        for (Direction side : Direction.values()) {
            if (level.getBlockState(pos.relative(side)).is(EOTPTags.Blocks.QI_INSULATING)) {
                insulating++;
            }
        }
        return Math.max(0.25F, 1.0F - insulating * 0.12F);
    }
}

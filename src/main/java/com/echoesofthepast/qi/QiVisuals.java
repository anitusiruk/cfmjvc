package com.echoesofthepast.qi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Qi is meant to be read with the eyes rather than off a number, so every device speaks through
 * particles. Everything here is sent from the server with vanilla particle types, tinted by phase.
 */
public final class QiVisuals {
    private QiVisuals() {}

    /** A slow, quiet drift of Qi sitting inside a block. */
    public static void resting(ServerLevel level, BlockPos pos, QiStorage storage) {
        float fill = storage.fillRatio();
        if (fill <= 0.02F) return;
        int count = 1 + (int) (fill * 3.0F);
        level.sendParticles(
            new DustParticleOptions(storage.blend().color(), 0.6F + fill * 0.8F),
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            count, 0.28, 0.28, 0.28, 0.005 + fill * 0.01
        );
        if (storage.turbulence() > 0.35F) {
            level.sendParticles(
                ParticleTypes.CRIT,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                1 + (int) (storage.turbulence() * 3.0F), 0.3, 0.3, 0.3, 0.05
            );
        }
    }

    /** Qi visibly moving from one block to the next. */
    public static void flow(ServerLevel level, BlockPos from, BlockPos to, PhaseBlend blend, float amount) {
        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        int duration = 8 + Mth.clamp((int) amount, 0, 12);
        level.sendParticles(
            new TrailParticleOption(end, blend.color(), duration),
            start.x, start.y, start.z,
            1, 0.05, 0.05, 0.05, 0.0
        );
    }

    /** A pulse arriving somewhere, drawn as a brief flare on the face it entered by. */
    public static void pulseArrival(ServerLevel level, BlockPos pos, Direction from, QiPulse pulse) {
        Vec3 offset = Vec3.atLowerCornerOf(from.getUnitVec3i()).scale(0.4);
        level.sendParticles(
            new DustParticleOptions(pulse.blend().color(), 0.7F + pulse.tone() * 0.25F),
            pos.getX() + 0.5 + offset.x, pos.getY() + 0.5 + offset.y, pos.getZ() + 0.5 + offset.z,
            2 + pulse.tone(), 0.1, 0.1, 0.1, 0.02
        );
    }

    /** Vapour rising out of bamboo joints and censers. */
    public static void vapour(ServerLevel level, BlockPos pos, PhaseBlend blend, float strength) {
        level.sendParticles(
            new DustParticleOptions(blend.color(), 1.2F),
            pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
            1 + (int) (strength * 2.0F), 0.18, 0.05, 0.18, 0.02
        );
    }

    /** Qi bleeding out of a badly built device. */
    public static void leak(ServerLevel level, BlockPos pos, PhaseBlend blend, float amount) {
        if (amount <= 0.01F) return;
        level.sendParticles(
            new DustParticleOptions(blend.color(), 0.5F),
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            1, 0.5, 0.5, 0.5, 0.04
        );
    }

    /** A line drawn between two points, used for beams, formation links and bound artifacts. */
    public static void line(ServerLevel level, Vec3 from, Vec3 to, int color, int density) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-3) return;
        int steps = Math.max(1, Math.min(64, (int) (length * density)));
        Vec3 step = delta.scale(1.0 / steps);
        for (int i = 0; i <= steps; i++) {
            Vec3 point = from.add(step.scale(i));
            level.sendParticles(
                new DustParticleOptions(color, 0.5F),
                point.x, point.y, point.z,
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    /** A ring on the ground, for formations coming alive and rituals taking hold. */
    public static void ring(ServerLevel level, Vec3 center, double radius, int color, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 / points) * i;
            level.sendParticles(
                new DustParticleOptions(color, 0.8F),
                center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius,
                1, 0.0, 0.01, 0.0, 0.0
            );
        }
    }

    /** The look of something remembered rather than present. */
    public static void echo(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(ParticleTypes.SOUL, at.x, at.y, at.z, count, 0.15, 0.2, 0.15, 0.005);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, Math.max(1, count / 2), 0.2, 0.2, 0.2, 0.001);
    }

    /** Used when something magical fails: a breakthrough, a pill, a formation. */
    public static void backlash(ServerLevel level, Vec3 at, PhaseBlend blend) {
        level.sendParticles(new DustParticleOptions(blend.color(), 1.6F), at.x, at.y, at.z, 24, 0.6, 0.6, 0.6, 0.2);
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 12, 0.4, 0.4, 0.4, 0.05);
    }

    /** Used when something magical succeeds. */
    public static void bloom(ServerLevel level, Vec3 at, PhaseBlend blend) {
        level.sendParticles(new DustParticleOptions(blend.color(), 1.4F), at.x, at.y, at.z, 40, 0.5, 0.7, 0.5, 0.08);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 16, 0.3, 0.5, 0.3, 0.03);
    }
}

package com.echoesofthepast.world;

import com.echoesofthepast.qi.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Dragon veins are not structures and nothing is generated for them. They are a function of the
 * world seed evaluated on demand, so every block in every world already knows whether a current of
 * natural Qi runs through it.
 *
 * <p>Two independent ridge fields are laid over the world. A vein exists where a field is close to
 * its ridge line; where both fields ridge at once you get an intersection, which is the kind of
 * place a cultivator builds a house on.
 */
public final class DragonVeins {
    /** Roughly how far apart veins run, in blocks. */
    private static final double SCALE_A = 71.0;
    private static final double SCALE_B = 113.0;
    /** How wide the ridge is; wider means fatter, weaker veins. */
    private static final double RIDGE_WIDTH = 0.16;

    private DragonVeins() {}

    private static long seedOf(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getSeed();
        }
        return level.dimension().identifier().hashCode();
    }

    private static long hash(long seed, long x, long z, int salt) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL) ^ (salt * 0x165667B19E3779F9L);
        h ^= (h >>> 33);
        h *= 0xFF51AFD7ED558CCDL;
        h ^= (h >>> 33);
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= (h >>> 33);
        return h;
    }

    private static double gridValue(long seed, long x, long z, int salt) {
        return (hash(seed, x, z, salt) >>> 11) / (double) (1L << 53) * 2.0 - 1.0;
    }

    /** Smooth value noise; good enough for veins and far cheaper than a real noise sampler. */
    private static double noise(long seed, double x, double z, double scale, int salt) {
        double sx = x / scale;
        double sz = z / scale;
        long x0 = Mth.lfloor(sx);
        long z0 = Mth.lfloor(sz);
        double fx = sx - x0;
        double fz = sz - z0;
        double ux = fx * fx * (3.0 - 2.0 * fx);
        double uz = fz * fz * (3.0 - 2.0 * fz);

        double v00 = gridValue(seed, x0, z0, salt);
        double v10 = gridValue(seed, x0 + 1, z0, salt);
        double v01 = gridValue(seed, x0, z0 + 1, salt);
        double v11 = gridValue(seed, x0 + 1, z0 + 1, salt);

        double bottom = v00 + (v10 - v00) * ux;
        double top = v01 + (v11 - v01) * ux;
        return bottom + (top - bottom) * uz;
    }

    private static double ridge(long seed, double x, double z, double scale, int salt) {
        double value = noise(seed, x, z, scale, salt) + noise(seed, x, z, scale * 0.37, salt + 17) * 0.35;
        double distance = Math.abs(value);
        if (distance >= RIDGE_WIDTH) return 0.0;
        return 1.0 - distance / RIDGE_WIDTH;
    }

    /**
     * How strongly a dragon vein runs through this block, from 0 to 1. Intersections read above the
     * strength of either vein alone.
     */
    public static float strength(Level level, BlockPos pos) {
        long seed = seedOf(level);
        double a = ridge(seed, pos.getX(), pos.getZ(), SCALE_A, 1);
        double b = ridge(seed, pos.getX(), pos.getZ(), SCALE_B, 2);
        double combined = Math.max(a, b) + Math.min(a, b) * 0.75;

        // Veins run at a depth that wanders with the terrain rather than sitting at a fixed level.
        double veinY = 40.0 + noise(seed, pos.getX(), pos.getZ(), 160.0, 5) * 46.0;
        double verticalFalloff = Mth.clamp(1.0 - Math.abs(pos.getY() - veinY) / 44.0, 0.0, 1.0);

        return (float) Mth.clamp(combined * (0.35 + 0.65 * verticalFalloff), 0.0, 1.0);
    }

    /** True where two veins cross: the best cultivation sites in a world. */
    public static boolean isIntersection(Level level, BlockPos pos) {
        long seed = seedOf(level);
        return ridge(seed, pos.getX(), pos.getZ(), SCALE_A, 1) > 0.35
            && ridge(seed, pos.getX(), pos.getZ(), SCALE_B, 2) > 0.35;
    }

    /**
     * Direction the current runs, found by walking along the ridge rather than across it. Used to
     * draw the flowing lines the compass shows and to orient devices with the vein.
     */
    public static Vec3 flow(Level level, BlockPos pos) {
        long seed = seedOf(level);
        double step = 2.0;
        double dx = ridge(seed, pos.getX() + step, pos.getZ(), SCALE_A, 1) - ridge(seed, pos.getX() - step, pos.getZ(), SCALE_A, 1);
        double dz = ridge(seed, pos.getX(), pos.getZ() + step, SCALE_A, 1) - ridge(seed, pos.getX(), pos.getZ() - step, SCALE_A, 1);
        // The current runs perpendicular to the gradient, i.e. along the ridge.
        Vec3 along = new Vec3(-dz, 0.0, dx);
        return along.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : along.normalize();
    }

    /**
     * The character of the Qi a vein carries here. Veins are not neutral: one stretch runs cold and
     * watery, another metallic, and devices fed from a vein inherit that.
     */
    public static Phase phaseOf(Level level, BlockPos pos) {
        long seed = seedOf(level);
        double value = noise(seed, pos.getX(), pos.getZ(), 220.0, 9);
        int index = Mth.clamp((int) ((value + 1.0) * 0.5 * Phase.VALUES.length), 0, Phase.VALUES.length - 1);
        return Phase.VALUES[index];
    }

    /**
     * Ambient Qi available for the taking at a position: veins, open sky at night, and depth all
     * contribute. Generating plants and gathering formations draw on this.
     */
    public static float ambientQi(Level level, BlockPos pos) {
        float vein = strength(level, pos);
        float sky = level.canSeeSky(pos) ? (level.isDarkOutside() ? 0.35F : 0.15F) : 0.05F;
        float depth = pos.getY() < 0 ? 0.2F : 0.0F;
        return vein * 0.8F + sky + depth;
    }
}

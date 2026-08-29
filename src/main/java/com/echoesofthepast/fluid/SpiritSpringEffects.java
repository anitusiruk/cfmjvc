package com.echoesofthepast.fluid;

import com.echoesofthepast.registry.EOTPFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * Shared tests for the rare Spirit Spring fluid. Keeping this in one place makes every progression
 * bonus agree on what counts as touching a spring.
 */
public final class SpiritSpringEffects {
    private SpiritSpringEffects() {}

    public static boolean isSpring(Level level, BlockPos pos) {
        Fluid fluid = level.getFluidState(pos).getType();
        return fluid == EOTPFluids.SPIRIT_SPRING.get() || fluid == EOTPFluids.SPIRIT_SPRING_FLOWING.get();
    }

    public static boolean nearby(Level level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            if (isSpring(level, pos)) return true;
        }
        return false;
    }
}

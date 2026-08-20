package com.echoesofthepast.formation;

import com.echoesofthepast.block.formation.FormationBannerBlock;
import com.echoesofthepast.block.formation.FormationInkBlock;
import com.echoesofthepast.block.formation.FormationTileBlock;
import com.echoesofthepast.block.talisman.PlacedTalismanBlockEntity;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.seal.SealRule;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a formation core can see of the circuit drawn around it.
 *
 * <p>The survey is deliberately shape-based rather than pattern-matched against fixed templates: any
 * arrangement that closes on itself and contains the right parts will work, so players design their
 * own layouts instead of copying a diagram block for block.
 */
public record FormationSurvey(
    Set<BlockPos> parts,
    Map<FormationPart, Integer> counts,
    PhaseBlend inkBlend,
    Set<Phase> inkPhases,
    Set<SealRule> seals,
    int banners,
    boolean closed,
    int width,
    int depth,
    float conductance
) {
    /** How far from the core a circuit may reach. */
    public static final int MAX_RADIUS = 12;
    /** Hard cap on parts, so a griefed floor of ink cannot stall the server. */
    private static final int MAX_PARTS = 512;

    public int size() {
        return this.parts.size();
    }

    public int count(FormationPart part) {
        return this.counts.getOrDefault(part, 0);
    }

    /** Longest side of the circuit, which formations use for their area of effect. */
    public double radius() {
        return Math.max(1.5, Math.max(this.width, this.depth) / 2.0);
    }

    /**
     * Walks the circuit outwards from the core. Tiles and ink conduct to their orthogonal
     * neighbours, and a one-block step up or down is allowed so that a formation can climb a stair
     * or cross a threshold.
     */
    public static FormationSurvey scan(Level level, BlockPos corePos) {
        Set<BlockPos> found = new HashSet<>();
        Map<FormationPart, Integer> counts = new EnumMap<>(FormationPart.class);
        Set<Phase> inkPhases = EnumSet.noneOf(Phase.class);
        Set<SealRule> seals = EnumSet.noneOf(SealRule.class);
        PhaseBlend inkBlend = PhaseBlend.EMPTY;
        float conductance = 0.0F;

        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(corePos);
        visited.add(corePos);

        int minX = corePos.getX();
        int maxX = corePos.getX();
        int minZ = corePos.getZ();
        int maxZ = corePos.getZ();

        while (!queue.isEmpty() && found.size() < MAX_PARTS) {
            BlockPos pos = queue.poll();

            if (!pos.equals(corePos)) {
                FormationPart part = partAt(level, pos);
                if (part == null) continue;
                found.add(pos);
                counts.merge(part, 1, Integer::sum);
                conductance += part.conductance();

                Phase phase = inkPhaseAt(level, pos);
                if (phase != null) {
                    inkPhases.add(phase);
                    inkBlend = inkBlend.with(phase, 1.0F);
                }

                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
                minZ = Math.min(minZ, pos.getZ());
                maxZ = Math.max(maxZ, pos.getZ());
            }

            for (Direction side : Direction.Plane.HORIZONTAL) {
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos next = pos.relative(side).offset(0, dy, 0);
                    if (visited.contains(next)) continue;
                    if (next.distSqr(corePos) > (double) MAX_RADIUS * MAX_RADIUS) continue;
                    if (partAt(level, next) == null) continue;
                    visited.add(next);
                    queue.add(next);
                }
                BlockPos up = pos.relative(side).above();
                if (!visited.contains(up) && up.distSqr(corePos) <= (double) MAX_RADIUS * MAX_RADIUS && partAt(level, up) != null) {
                    visited.add(up);
                    queue.add(up);
                }
            }
        }

        // A circuit is complete when nothing in it is a dead end: every mark touches at least two
        // others. This is the whole completeness rule, and it is visible at a glance.
        boolean closed = !found.isEmpty();
        for (BlockPos pos : found) {
            int neighbours = 0;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos next = pos.relative(side).offset(0, dy, 0);
                    if (found.contains(next) || next.equals(corePos)) {
                        neighbours++;
                        break;
                    }
                }
            }
            if (neighbours < 2) {
                closed = false;
                break;
            }
        }

        int banners = 0;
        List<BlockPos> edge = new ArrayList<>(found);
        for (BlockPos pos : edge) {
            for (int dy = 0; dy <= 2; dy++) {
                BlockState above = level.getBlockState(pos.above(dy + 1));
                if (above.getBlock() instanceof FormationBannerBlock) {
                    banners++;
                    Phase phase = above.getValue(FormationBannerBlock.PHASE);
                    inkPhases.add(phase);
                    inkBlend = inkBlend.with(phase, 0.5F);
                    break;
                }
            }
            if (level.getBlockEntity(pos.above()) instanceof PlacedTalismanBlockEntity talisman) {
                SealRule rule = talisman.sealRule();
                if (rule != null) seals.add(rule);
            }
        }

        return new FormationSurvey(
            Set.copyOf(found),
            Map.copyOf(counts),
            inkBlend.normalised(),
            Set.copyOf(inkPhases),
            Set.copyOf(seals),
            banners,
            closed,
            maxX - minX + 1,
            maxZ - minZ + 1,
            conductance
        );
    }

    private static FormationPart partAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FormationTileBlock tile) return tile.part();
        if (state.getBlock() instanceof FormationInkBlock) return FormationPart.INK;
        return null;
    }

    private static Phase inkPhaseAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof FormationInkBlock ? state.getValue(FormationInkBlock.PHASE) : null;
    }
}

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
import org.jspecify.annotations.Nullable;

/**
 * What a formation core can read of the statement written around it.
 *
 * <p>Formation marks are not another Qi cable. Flues carry continuous power and Meridian Thread
 * carries logic pulses; marks describe <em>what separately supplied Qi is ordered to do</em>. The
 * survey therefore reads them as grammar: a governing seal to begin from, Lines and Arcs carrying
 * the clause onward, Nodes separating clauses, Trigrams modifying direction and phase, and a path
 * that must eventually return to the rule it started from.
 *
 * @param governingSeal the stamped rule the statement is written under
 * @param inwardMarks   marks whose facing points back toward the core
 * @param outwardMarks  marks whose facing points away from the core
 * @param closed        whether the statement returns to its governing rule
 */
public record FormationSurvey(
    Set<BlockPos> parts,
    Map<FormationPart, Integer> counts,
    PhaseBlend inkBlend,
    Set<Phase> inkPhases,
    Set<SealRule> seals,
    @Nullable SealRule governingSeal,
    int banners,
    boolean closed,
    int inwardMarks,
    int outwardMarks,
    int width,
    int depth,
    float conductance
) {
    /** How far from the core a statement may reach. */
    public static final int MAX_RADIUS = 12;
    /** Hard cap on parts, so a griefed floor of ink cannot stall the server. */
    private static final int MAX_PARTS = 512;

    public int size() {
        return this.parts.size();
    }

    public int count(FormationPart part) {
        return this.counts.getOrDefault(part, 0);
    }

    /** Longest side of the statement, which formations use for their area of effect. */
    public double radius() {
        return Math.max(1.5, Math.max(this.width, this.depth) / 2.0);
    }

    /** True when most directional marks are turned toward the core, as a gathering clause is. */
    public boolean readsInward() {
        return this.inwardMarks > this.outwardMarks;
    }

    /** True when most directional marks are turned away, as a repelling clause is. */
    public boolean readsOutward() {
        return this.outwardMarks > this.inwardMarks;
    }

    /** Trigrams are the only marks that carry meaning rather than shape. */
    public int trigrams() {
        return this.count(FormationPart.TRIGRAM);
    }

    /** Nodes end clauses; a statement needs them to be more than one long run-on line. */
    public int clauses() {
        return this.count(FormationPart.NODE);
    }

    /**
     * Reads the statement outward from the core. Tiles and ink continue a clause to their orthogonal
     * neighbours, and a one-block step is allowed so writing can climb a stair or cross a threshold.
     */
    public static FormationSurvey scan(Level level, BlockPos corePos) {
        Set<BlockPos> found = new HashSet<>();
        Map<FormationPart, Integer> counts = new EnumMap<>(FormationPart.class);
        Set<Phase> inkPhases = EnumSet.noneOf(Phase.class);
        Set<SealRule> seals = EnumSet.noneOf(SealRule.class);
        PhaseBlend inkBlend = PhaseBlend.EMPTY;
        float conductance = 0.0F;
        int inward = 0;
        int outward = 0;

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

                Direction facing = facingAt(level, pos);
                if (facing != null) {
                    if (pointsToward(pos, facing, corePos)) {
                        inward++;
                    } else {
                        outward++;
                    }
                }

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

        // A statement returns to its rule when nothing in it is a dead end: every mark continues
        // into at least two others, so the reading has somewhere to go and somewhere to come back
        // from. An unresolved branch is exactly what makes a formation inert.
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
        SealRule governing = null;
        double governingDistance = Double.MAX_VALUE;
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
                if (rule == null) continue;
                seals.add(rule);
                // The seal nearest the core is the one the statement is written under.
                double distance = pos.distSqr(corePos);
                if (distance < governingDistance) {
                    governingDistance = distance;
                    governing = rule;
                }
            }
        }

        return new FormationSurvey(
            Set.copyOf(found),
            Map.copyOf(counts),
            inkBlend.normalised(),
            Set.copyOf(inkPhases),
            Set.copyOf(seals),
            governing,
            banners,
            closed,
            inward,
            outward,
            maxX - minX + 1,
            maxZ - minZ + 1,
            conductance
        );
    }

    /** Whether a mark's carved direction points back toward the core it belongs to. */
    private static boolean pointsToward(BlockPos pos, Direction facing, BlockPos corePos) {
        BlockPos ahead = pos.relative(facing);
        return ahead.distSqr(corePos) < pos.distSqr(corePos);
    }

    private static @Nullable Direction facingAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FormationTileBlock)) return null;
        if (!state.hasProperty(FormationTileBlock.FACING)) return null;
        return state.getValue(FormationTileBlock.FACING);
    }

    private static @Nullable FormationPart partAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FormationTileBlock tile) return tile.part();
        if (state.getBlock() instanceof FormationInkBlock) return FormationPart.INK;
        return null;
    }

    private static @Nullable Phase inkPhaseAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof FormationInkBlock ? state.getValue(FormationInkBlock.PHASE) : null;
    }
}

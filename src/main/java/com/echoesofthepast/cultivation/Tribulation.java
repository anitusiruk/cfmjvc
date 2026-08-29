package com.echoesofthepast.cultivation;

import com.echoesofthepast.EOTPConfig;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPTags;
import com.echoesofthepast.sound.Resonance;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

/**
 * Heaven's Contradiction. Rather than throwing progressively more lightning at a health bar, the
 * formation reads what the cultivator claims to understand and argues with that claim directly: Qi
 * runs backwards, one phase floods, bells go quiet, echoes replay themselves, returns stop
 * completing.
 *
 * <p>Lightning remains the signature trial rather than the whole system. When it is selected the
 * bolts still seek the tallest conductor first, so rods, bells, banners and spirit stones are
 * deliberate infrastructure, and a Middle Spirit Stone that takes a routed strike can be quenched
 * into a High one.
 */
public final class Tribulation {
    /** Bolts in a full tribulation. */
    private static final int BOLTS = 7;
    /** Ticks between bolts. */
    private static final int INTERVAL = 40;
    /** How far out the sky looks for something worth hitting. */
    private static final int SEARCH_RADIUS = 10;
    /** Qi a bolt delivers into whatever it lands on. */
    private static final float BOLT_QI = 400.0F;
    /** Minimum length of a trial, so a contradiction-only tribulation still has to be endured. */
    private static final int TRIAL_TICKS = BOLTS * INTERVAL;

    private static final Map<UUID, State> RUNNING = new HashMap<>();

    private Tribulation() {}

    private static final class State {
        private final BlockPos center;
        private final List<Contradiction> contradictions;
        private int boltsLeft;
        private int countdown = INTERVAL;
        private int routed;
        private int elapsed;

        private State(BlockPos center, List<Contradiction> contradictions) {
            this.center = center;
            this.contradictions = contradictions;
            this.boltsLeft = contradictions.contains(Contradiction.LIGHTNING) ? BOLTS : 0;
        }
    }

    /**
     * Heaven reads what the cultivator claims to understand and argues with it specifically. Two or
     * three contradictions are chosen, weighted toward the systems the player leans on hardest, so a
     * bell-driven workshop really can be silenced and a Return-shaped cultivator really can be told
     * that nothing is coming back.
     */
    public static void begin(ServerLevel level, ServerPlayer player, BlockPos center, Cultivator cultivator) {
        List<Contradiction> chosen = choose(level, cultivator);
        RUNNING.put(player.getUUID(), new State(center, chosen));

        if (chosen.contains(Contradiction.LIGHTNING) && EOTPConfig.tribulationBreaksBlocks()) {
            level.getServer().setWeatherParameters(0, BOLTS * INTERVAL + 200, true, true);
        }

        Tell.chat(player, Component.translatable("eotp.message.tribulation_gathers"));
        for (Contradiction contradiction : chosen) {
            Tell.chat(player, Component.translatable("eotp.message.contradiction_named",
                Component.translatable(contradiction.translationKey())));
        }
        EchoLog.record(level, center, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.tribulation", player.getName()));
    }

    /** Picks the contradictions most relevant to this cultivator, with a little chance mixed in. */
    private static List<Contradiction> choose(ServerLevel level, Cultivator cultivator) {
        List<Contradiction> ordered = new ArrayList<>(List.of(Contradiction.VALUES));
        ordered.sort((a, b) -> Float.compare(
            b.relevanceTo(cultivator) + level.getRandom().nextFloat() * 0.5F,
            a.relevanceTo(cultivator) + level.getRandom().nextFloat() * 0.5F
        ));

        List<Contradiction> chosen = new ArrayList<>(ordered.subList(0, 2));
        // Lightning is the signature trial; it shows up often but is no longer the whole system.
        if (!chosen.contains(Contradiction.LIGHTNING) && level.getRandom().nextFloat() < 0.6F) {
            chosen.add(Contradiction.LIGHTNING);
        }
        return List.copyOf(chosen);
    }

    /** Which contradictions are currently being enforced against this player. */
    public static List<Contradiction> activeFor(ServerPlayer player) {
        State state = RUNNING.get(player.getUUID());
        return state == null ? List.of() : state.contradictions;
    }

    /** Used by devices to ask whether Heaven is currently arguing with them. */
    public static boolean isEnforcing(ServerPlayer player, Contradiction contradiction) {
        return activeFor(player).contains(contradiction);
    }

    public static boolean isRunning(ServerPlayer player) {
        return RUNNING.containsKey(player.getUUID());
    }

    /** Called every tick for players with a tribulation over their head. */
    public static void tick(ServerPlayer player) {
        State state = RUNNING.get(player.getUUID());
        if (state == null) return;
        ServerLevel level = player.level();
        state.elapsed++;

        if (state.elapsed % 20 == 0) {
            enforce(level, player, state);
        }

        if (state.boltsLeft > 0) {
            if (--state.countdown > 0) {
                if (state.countdown % 10 == 0) {
                    QiVisuals.line(level,
                        Vec3.atCenterOf(state.center).add(0.0, 12.0, 0.0),
                        Vec3.atCenterOf(state.center).add(0.0, 3.0, 0.0),
                        0xBFD4F5, 2);
                }
                return;
            }
            state.countdown = INTERVAL;
            strike(level, player, chooseTarget(level, state.center, player), state);
            state.boltsLeft--;
        }

        boolean lightningDone = state.boltsLeft <= 0;
        boolean enduredLongEnough = state.elapsed >= TRIAL_TICKS;
        if (!lightningDone || !enduredLongEnough) return;

        RUNNING.remove(player.getUUID());
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator != null) {
            cultivator.path().noteSurvivedTribulation();
            awardScar(player, cultivator, state);
            CultivationStore.touch(player);
        }

        Tell.chat(player, Component.translatable("eotp.message.tribulation_passes", state.routed));
        if (state.routed >= 3) {
            Cultivation.teach(player, Discovery.TRIBULATION_ROUTING);
        }
    }

    /**
     * Applies the non-lightning contradictions. Each one attacks the system the cultivator has been
     * relying on rather than their health.
     */
    private static void enforce(ServerLevel level, ServerPlayer player, State state) {
        for (Contradiction contradiction : state.contradictions) {
            switch (contradiction) {
                case REVERSAL -> reverseNearbyQi(level, state.center);
                case EXCESS -> floodNearbyPhase(level, state.center);
                case WITHERING -> QiVisuals.leak(level, state.center, PhaseBlend.of(Phase.WOOD), 1.0F);
                case SILENCE, FRACTURE, FALSE_ECHO, RETURN_DENIED ->
                    QiVisuals.echo(level, Vec3.atCenterOf(state.center).add(0.0, 1.0, 0.0), 3);
                case LIGHTNING -> { }
            }
        }
    }

    /** Reversal: stored Qi is pushed back the way it came and roughened. */
    private static void reverseNearbyQi(ServerLevel level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
            QiNode node = QiNet.nodeAt(level, pos);
            if (node == null) continue;
            QiStorage storage = node.qiStorage(null);
            if (storage == null || storage.isEmpty()) continue;
            storage.addTurbulence(0.05F);
        }
    }

    /** Excess: one phase floods everything nearby and has to be spent or converted away. */
    private static void floodNearbyPhase(ServerLevel level, BlockPos center) {
        Phase flooded = DragonVeins.phaseOf(level, center);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
            QiNode node = QiNet.nodeAt(level, pos);
            if (node == null) continue;
            QiStorage storage = node.qiStorage(null);
            if (storage == null) continue;
            storage.insert(6.0F, PhaseBlend.of(flooded), false);
        }
    }

    /** Protecting a living Landscape through Heaven's argument leaves a permanent mark on it. */
    private static void awardScar(ServerPlayer player, Cultivator cultivator, State state) {
        if (cultivator.path().landscape() == null) return;

        HeavenScar scar = null;
        for (Contradiction contradiction : state.contradictions) {
            scar = switch (contradiction) {
                case LIGHTNING -> HeavenScar.LIGHTNING;
                case WITHERING -> HeavenScar.DROUGHT;
                case REVERSAL -> HeavenScar.REVERSAL;
                case EXCESS -> HeavenScar.ASH;
                default -> scar;
            };
            if (scar != null) break;
        }
        if (scar == null) return;

        cultivator.path().addScar(scar);
        Tell.chat(player, Component.translatable("eotp.message.scar_earned",
            Component.translatable(scar.translationKey())));
    }

    /**
     * What the sky picks. Tagged conductors first, then anything holding Qi, then - if the cultivator
     * has prepared nothing at all - the cultivator.
     */
    private static BlockPos chooseTarget(ServerLevel level, BlockPos center, ServerPlayer player) {
        List<BlockPos> conductors = new ArrayList<>();
        List<BlockPos> devices = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-SEARCH_RADIUS, -3, -SEARCH_RADIUS),
            center.offset(SEARCH_RADIUS, 8, SEARCH_RADIUS))) {
            if (level.getBlockState(pos).is(EOTPTags.Blocks.TRIBULATION_CONDUCTOR)) {
                conductors.add(pos.immutable());
            } else if (QiNet.nodeAt(level, pos) != null) {
                devices.add(pos.immutable());
            }
        }

        if (!conductors.isEmpty()) {
            // The highest conductor wins, exactly as a lightning rod should.
            conductors.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
            return conductors.get(0);
        }
        if (!devices.isEmpty()) {
            return devices.get(level.getRandom().nextInt(devices.size()));
        }
        return player.blockPosition();
    }

    private static void strike(ServerLevel level, ServerPlayer player, BlockPos target, State state) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(Vec3.atBottomCenterOf(target));
            bolt.setVisualOnly(!EOTPConfig.tribulationBreaksBlocks());
            level.addFreshEntity(bolt);
        }
        Resonance.emit(level, target, Resonance.Tone.THUNDER, 60.0F);

        // A Middle stone left tempering in a struck disc takes the charge and can then be quenched.
        if (level.getBlockEntity(target) instanceof com.echoesofthepast.block.qi.JadeBiReservoirBlockEntity reservoir) {
            reservoir.takeTribulationStrike(level.getGameTime());
            Tell.overlay(player, "eotp.message.stone_charged");
        }

        QiNode node = QiNet.nodeAt(level, target);
        if (node != null) {
            QiStorage storage = node.qiStorage(null);
            if (storage != null) {
                // Tribulation Qi is enormous, metallic and rough. Devices that take it need headroom
                // or they will simply overflow and bleed the rest away.
                float accepted = storage.insert(BOLT_QI, PhaseBlend.of(Phase.METAL), false);
                storage.addTurbulence(0.4F);
                state.routed++;
                QiVisuals.bloom(level, Vec3.atCenterOf(target), PhaseBlend.of(Phase.METAL));
                if (accepted < BOLT_QI * 0.25F) {
                    QiVisuals.leak(level, target, PhaseBlend.of(Phase.METAL), BOLT_QI - accepted);
                }
                return;
            }
        }

        if (target.equals(player.blockPosition())) {
            player.hurtServer(level, level.damageSources().lightningBolt(), 6.0F);
            Tell.overlay(player, "eotp.message.tribulation_strikes_you");
        }
    }

    public static void abandon(ServerPlayer player) {
        RUNNING.remove(player.getUUID());
    }
}

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
 * Heavenly tribulation. The sky takes an interest in anybody stepping past foundation, and it does
 * not aim politely at the cultivator: it strikes the tallest, most conductive thing it can find.
 *
 * <p>That is the mechanic worth building around. A cultivator who has set up lightning rods, spirit
 * stones and reservoirs around their formation will watch the bolts land on those instead, and every
 * bolt that lands on a device dumps an enormous amount of Qi into it. Tribulation becomes a power
 * source for anybody willing to plan for it.
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

    private static final Map<UUID, State> RUNNING = new HashMap<>();

    private Tribulation() {}

    private static final class State {
        private final BlockPos center;
        private int boltsLeft = BOLTS;
        private int countdown = INTERVAL;
        private int routed;

        private State(BlockPos center) {
            this.center = center;
        }
    }

    public static void begin(ServerLevel level, ServerPlayer player, BlockPos center) {
        RUNNING.put(player.getUUID(), new State(center));
        if (EOTPConfig.tribulationBreaksBlocks()) {
            level.getServer().setWeatherParameters(0, BOLTS * INTERVAL + 200, true, true);
        }
        Tell.chat(player, Component.translatable("eotp.message.tribulation_gathers"));
        EchoLog.record(level, center, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.tribulation", player.getName()));
    }

    public static boolean isRunning(ServerPlayer player) {
        return RUNNING.containsKey(player.getUUID());
    }

    /** Called every tick for players with a tribulation over their head. */
    public static void tick(ServerPlayer player) {
        State state = RUNNING.get(player.getUUID());
        if (state == null) return;
        ServerLevel level = player.level();

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

        BlockPos target = chooseTarget(level, state.center, player);
        strike(level, player, target, state);

        if (--state.boltsLeft <= 0) {
            RUNNING.remove(player.getUUID());
            Tell.chat(player, Component.translatable("eotp.message.tribulation_passes", state.routed));
            if (state.routed >= 3) {
                Cultivation.teach(player, Discovery.TRIBULATION_ROUTING);
            }
        }
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

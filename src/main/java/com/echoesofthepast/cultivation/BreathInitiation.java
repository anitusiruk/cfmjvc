package com.echoesofthepast.cultivation;

import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * The Ninefold Returning Breath: the pre-formation initiation into cultivation.
 *
 * <p>Once a Mortal has gathered enough insight, they crouch without moving on a strong Dragon Vein.
 * Each breath alternates between facing downstream and upstream. The compass reveals the current,
 * so the ritual asks the player to read the land rather than click an advancement button.
 */
public final class BreathInitiation {
    private static final float MINIMUM_VEIN = 0.35F;
    private static final int BREATHS_REQUIRED = 9;
    private static final int TICKS_PER_BREATH = 40;
    private static final double ALIGNMENT = 0.65;

    private static final Map<UUID, Attempt> ATTEMPTS = new HashMap<>();

    private BreathInitiation() {}

    private static final class Attempt {
        private int breaths;
        private int heldTicks;
    }

    public static void tick(ServerPlayer player, Cultivator cultivator) {
        if (cultivator.realm() != Realm.MORTAL || !cultivator.readyToBreakThrough()) {
            ATTEMPTS.remove(player.getUUID());
            return;
        }

        ServerLevel level = player.level();
        float vein = DragonVeins.strength(level, player.blockPosition());
        boolean still = player.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4;
        if (!player.isShiftKeyDown() || !still || vein < MINIMUM_VEIN) {
            Attempt interrupted = ATTEMPTS.remove(player.getUUID());
            if (interrupted != null && interrupted.breaths > 0) {
                Tell.overlay(player, "eotp.message.breath_interrupted");
            }
            return;
        }

        Attempt attempt = ATTEMPTS.computeIfAbsent(player.getUUID(), ignored -> {
            Tell.chat(player, Component.translatable("eotp.message.breath_ritual_begins"));
            return new Attempt();
        });

        if (++attempt.heldTicks < TICKS_PER_BREATH) return;
        attempt.heldTicks = 0;

        Vec3 flow = DragonVeins.flow(level, player.blockPosition());
        Vec3 facing = player.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (facing.lengthSqr() < 1.0E-4) return;
        facing = facing.normalize();

        boolean downstream = attempt.breaths % 2 == 0;
        Vec3 wanted = downstream ? flow : flow.scale(-1.0);
        if (facing.dot(wanted) < ALIGNMENT) {
            Tell.overlay(player, Component.translatable(
                downstream ? "eotp.message.face_downstream" : "eotp.message.face_upstream",
                Component.translatable(directionKey(wanted))
            ));
            QiVisuals.line(
                level,
                player.position().add(0.0, 0.1, 0.0),
                player.position().add(0.0, 0.1, 0.0).add(wanted.scale(1.5)),
                DragonVeins.phaseOf(level, player.blockPosition()).color(),
                4
            );
            return;
        }

        attempt.breaths++;
        PhaseBlend veinBlend = PhaseBlend.of(DragonVeins.phaseOf(level, player.blockPosition()));
        QiVisuals.ring(level, player.position().add(0.0, 0.1, 0.0), 0.5 + attempt.breaths * 0.12, veinBlend.color(), 12);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.7F + attempt.breaths * 0.05F);
        Tell.overlay(player, Component.translatable("eotp.message.breath_count", attempt.breaths, BREATHS_REQUIRED));

        if (attempt.breaths < BREATHS_REQUIRED) return;

        ATTEMPTS.remove(player.getUUID());
        cultivator.advanceRealm();
        cultivator.addQi(cultivator.qiCapacity() * 0.25F, veinBlend);
        CultivationStore.touch(player);
        Cultivation.teach(player, Discovery.QI_SENSE);
        QiVisuals.bloom(level, player.position().add(0.0, 1.0, 0.0), veinBlend);
        Tell.chat(player, Component.translatable(
            "eotp.message.breakthrough",
            Component.translatable(cultivator.realm().translationKey())
        ));
        EchoLog.record(
            level,
            player.blockPosition(),
            EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.first_breath", player.getName())
        );
    }

    private static String directionKey(Vec3 direction) {
        String name = Math.abs(direction.x) > Math.abs(direction.z)
            ? (direction.x > 0.0 ? "east" : "west")
            : (direction.z > 0.0 ? "south" : "north");
        return "eotp.direction." + name;
    }
}

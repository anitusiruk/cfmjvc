package com.echoesofthepast.cultivation;

import com.echoesofthepast.EOTPConfig;
import com.echoesofthepast.block.formation.FormationCoreBlockEntity;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.entity.HeartDemonEntity;
import com.echoesofthepast.formation.FormationType;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPEntities;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.echoesofthepast.util.Tell;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Advancing a realm is a thing you do in a room, not in a menu.
 *
 * <p>The cultivator sits still inside a running cultivation formation while the circuit is fed. Every
 * second the ritual takes a reading: how balanced the Qi being supplied is, whether the formation is
 * still running, and whether the cultivator has moved. Good readings build stability, bad ones eat
 * it, and if stability runs out the attempt collapses with a backlash.
 *
 * <p>Automation can do all the feeding. It cannot do the sitting.
 */
public final class BreakthroughRitual {
    /** Readings needed to complete an attempt: one every second. */
    private static final int READINGS_REQUIRED = 30;
    /** Starting stability, and the most an attempt can bank. */
    private static final float MAX_STABILITY = 20.0F;

    private static final Map<UUID, Attempt> ATTEMPTS = new HashMap<>();

    private BreakthroughRitual() {}

    private static final class Attempt {
        private final BlockPos corePos;
        private int readings;
        private float stability = MAX_STABILITY * 0.6F;
        private boolean tribulationCalled;
        private boolean demonCalled;
        private @Nullable UUID demonId;

        private Attempt(BlockPos corePos) {
            this.corePos = corePos;
        }
    }

    /**
     * Called once a second for a still cultivator who is ready to advance. Everything about the
     * attempt lives here, including starting it.
     */
    public static void tick(ServerPlayer player, Cultivator cultivator) {
        ServerLevel level = player.level();
        UUID id = player.getUUID();

        FormationCoreBlockEntity core = FormationCoreBlockEntity.findNear(level, player.blockPosition(), 6);
        boolean supported = core != null
            && core.isRunning(FormationType.CULTIVATION)
            && core.covers(player);

        if (!supported) {
            Attempt abandoned = ATTEMPTS.remove(id);
            if (abandoned != null) {
                Tell.chat(player, Component.translatable("eotp.message.ritual_lapsed"));
            }
            return;
        }

        Attempt attempt = ATTEMPTS.computeIfAbsent(id, ignored -> {
            Tell.chat(player, Component.translatable("eotp.message.ritual_begins",
                Component.translatable(cultivator.realm().next().translationKey())));
            EchoLog.record(level, core.getBlockPos(), EchoLog.Kind.RITUAL,
                Component.translatable("eotp.echo.ritual_begun", player.getName()));
            return new Attempt(core.getBlockPos());
        });

        // Phase balance is the whole skill of the thing: the formation wants all five in relation,
        // and a circuit being fed nothing but one phase will not hold a cultivator together.
        PhaseBlend supplied = core.storage().blend();
        float balance = supplied.similarity(PhaseBlend.BALANCED);
        float harmony = supplied.harmony();
        float reading = (balance * 0.6F + harmony * 0.4F) * core.strength();

        // A clear head is worth as much as good plumbing.
        if (player.hasEffect(EOTPMobEffects.holder(EOTPMobEffects.CLEAR_HEART))) {
            reading += 0.25F;
        }
        if (cultivator.failedBreakthroughs() > 0) {
            reading -= 0.1F * cultivator.failedBreakthroughs();
        }
        reading -= cultivator.root().deviationRisk() * 0.3F;

        if (reading >= 0.55F) {
            attempt.stability = Math.min(MAX_STABILITY, attempt.stability + 1.0F);
            attempt.readings++;
        } else {
            attempt.stability -= (0.55F - reading) * 6.0F;
            // Unstable Qi is loud and ugly, and the particles say so before the numbers would.
            QiVisuals.backlash(level, player.position().add(0.0, 1.0, 0.0), supplied);
        }

        Vec3 center = Vec3.atCenterOf(attempt.corePos);
        QiVisuals.ring(level, center.add(0.0, 0.2, 0.0), 1.6 + attempt.readings * 0.05, supplied.color(), 18);

        if (attempt.stability <= 0.0F) {
            fail(player, cultivator, level, attempt);
            return;
        }

        Realm realm = cultivator.realm();
        // The dangerous parts of an attempt arrive partway through, not at the end, so there is time
        // to deal with them.
        if (!attempt.demonCalled && attempt.readings >= READINGS_REQUIRED / 3 && realm.drawsHeartDemon()
            && EOTPConfig.heartDemonEnabled()) {
            attempt.demonCalled = true;
            summonHeartDemon(level, player, attempt);
        }
        if (!attempt.tribulationCalled && attempt.readings >= READINGS_REQUIRED / 2 && realm.drawsTribulation()) {
            attempt.tribulationCalled = true;
            Tribulation.begin(level, player, attempt.corePos);
        }

        if (attempt.demonId != null) {
            HeartDemonEntity demon = findDemon(level, attempt.demonId);
            if (demon != null) {
                if (demon.landedHits() >= 4) {
                    fail(player, cultivator, level, attempt);
                    demon.discard();
                    return;
                }
                // The demon holds the attempt open until it is dealt with.
                return;
            }
            attempt.demonId = null;
        }

        if (Tribulation.isRunning(player)) return;

        if (attempt.readings >= READINGS_REQUIRED) {
            succeed(player, cultivator, level, attempt);
        }
    }

    private static void summonHeartDemon(ServerLevel level, ServerPlayer player, Attempt attempt) {
        HeartDemonEntity demon = EOTPEntities.HEART_DEMON.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (demon == null) return;
        Vec3 at = HeartDemonEntity.risingPosition(player);
        demon.snapTo(at.x, at.y, at.z, player.getYRot(), 0.0F);
        demon.bindTo(player);
        level.addFreshEntity(demon);
        attempt.demonId = demon.getUUID();

        Tell.chat(player, Component.translatable("eotp.message.heart_demon_rises"));
        EchoLog.record(level, player.blockPosition(), EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.heart_demon", player.getName()));
    }

    private static @Nullable HeartDemonEntity findDemon(ServerLevel level, UUID id) {
        return level.getEntity(id) instanceof HeartDemonEntity demon ? demon : null;
    }

    private static void succeed(ServerPlayer player, Cultivator cultivator, ServerLevel level, Attempt attempt) {
        ATTEMPTS.remove(player.getUUID());
        Realm before = cultivator.realm();
        cultivator.advanceRealm();
        CultivationStore.touch(player);

        QiVisuals.bloom(level, player.position().add(0.0, 1.0, 0.0), PhaseBlend.BALANCED);
        Tell.chat(player, Component.translatable("eotp.message.breakthrough",
            Component.translatable(cultivator.realm().translationKey())));
        EchoLog.record(level, attempt.corePos, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.breakthrough", player.getName(),
                Component.translatable(cultivator.realm().translationKey())));

        if (cultivator.realm() == Realm.NASCENT_SPIRIT) {
            Cultivation.teach(player, Discovery.NASCENT_PROJECTION);
        }
        if (before == Realm.MORTAL) {
            Cultivation.teach(player, Discovery.QI_SENSE);
        }
    }

    private static void fail(ServerPlayer player, Cultivator cultivator, ServerLevel level, Attempt attempt) {
        ATTEMPTS.remove(player.getUUID());
        cultivator.loseProgress(0.35F);
        cultivator.destabiliseCore(3000);
        CultivationStore.touch(player);

        player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.QI_DEVIATION, 600, 1));
        QiVisuals.backlash(level, player.position().add(0.0, 1.0, 0.0), cultivator.qiBlend());
        Tell.chat(player, Component.translatable("eotp.message.breakthrough_failed"));
        EchoLog.record(level, attempt.corePos, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.breakthrough_failed", player.getName()));
    }

    /** True while this cultivator has an attempt open, which several other systems check. */
    public static boolean isAttempting(ServerPlayer player) {
        return ATTEMPTS.containsKey(player.getUUID());
    }

    public static void abandon(ServerPlayer player) {
        ATTEMPTS.remove(player.getUUID());
    }
}

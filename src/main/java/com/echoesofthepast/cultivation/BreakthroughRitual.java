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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The rituals that carry a cultivator past Breath Gathering. All three are held inside a running
 * Cultivation Formation and all three test something the player has actually done, but they ask for
 * different proofs:
 *
 * <ul>
 *   <li><b>Foundation</b> shows the player their three strongest tendencies as glyphs and asks them
 *       to perform each one again before the circle loses patience. The result is a Self-Script.</li>
 *   <li><b>Golden Core</b> reads three mastered Verse scrolls laid around the circle and requires all
 *       three relationships to remain true at once, condensing them into a Core Thesis.</li>
 *   <li><b>Nascent Spirit</b> requires Script, Thesis and Landscape to hold together simultaneously.</li>
 * </ul>
 *
 * <p>Collapse never deletes demonstrated progress. It leaves Core Discord on whichever principle was
 * failing, which the cultivator repairs by demonstrating that principle again in ordinary play.
 */
public final class BreakthroughRitual {
    private static final int READINGS_REQUIRED = 30;
    private static final float MAX_STABILITY = 20.0F;
    /** How long the Foundation ritual waits for each tendency to be shown again, in readings. */
    private static final int GLYPH_PATIENCE = 30;

    private static final Map<UUID, Attempt> ATTEMPTS = new HashMap<>();

    private BreakthroughRitual() {}

    private static final class Attempt {
        private final BlockPos corePos;
        private final Realm from;
        private int readings;
        private float stability = MAX_STABILITY * 0.6F;
        private boolean tribulationCalled;
        private boolean demonCalled;
        private @Nullable UUID demonId;
        /** Foundation: the tendencies being asked for, and which have been shown again. */
        private final List<Tendency> glyphs = new ArrayList<>();
        private final List<Tendency> shown = new ArrayList<>();
        /** Golden Core: the Verses read off the scrolls laid around the circle. */
        private final List<Verse> verses = new ArrayList<>();

        private Attempt(BlockPos corePos, Realm from) {
            this.corePos = corePos;
            this.from = from;
        }
    }

    /** Called once a second for a still cultivator who is eligible to advance. */
    public static void tick(ServerPlayer player, Cultivator cultivator) {
        ServerLevel level = player.level();
        UUID id = player.getUUID();

        FormationCoreBlockEntity core = FormationCoreBlockEntity.findNear(level, player.blockPosition(), 6);
        boolean supported = core != null && core.isRunning(FormationType.CULTIVATION) && core.covers(player);

        if (!supported) {
            Attempt abandoned = ATTEMPTS.remove(id);
            if (abandoned != null) {
                Tell.chat(player, Component.translatable("eotp.message.ritual_lapsed"));
            }
            return;
        }

        Attempt attempt = ATTEMPTS.computeIfAbsent(id, ignored -> begin(player, cultivator, core));

        PhaseBlend supplied = core.storage().blend();
        float reading = readCircle(player, cultivator, core, supplied);
        if (reading >= 0.55F) {
            attempt.stability = Math.min(MAX_STABILITY, attempt.stability + 1.0F);
            attempt.readings++;
        } else {
            attempt.stability -= (0.55F - reading) * 6.0F;
            QiVisuals.backlash(level, player.position().add(0.0, 1.0, 0.0), supplied);
        }

        QiVisuals.ring(level, Vec3.atCenterOf(attempt.corePos).add(0.0, 0.2, 0.0),
            1.6 + attempt.readings * 0.05, supplied.color(), 18);

        if (attempt.stability <= 0.0F) {
            fail(player, cultivator, level, attempt, failingPrinciple(attempt, supplied));
            return;
        }

        if (attempt.from == Realm.BREATH_GATHERING) {
            promptGlyphs(player, attempt);
        }

        Realm realm = cultivator.realm();
        if (!attempt.demonCalled && attempt.readings >= READINGS_REQUIRED / 3
            && realm.drawsHeartDemon() && EOTPConfig.heartDemonEnabled()) {
            attempt.demonCalled = true;
            summonHeartDemon(level, player, attempt);
        }
        if (!attempt.tribulationCalled && attempt.readings >= READINGS_REQUIRED / 2 && realm.drawsTribulation()) {
            attempt.tribulationCalled = true;
            Tribulation.begin(level, player, attempt.corePos, cultivator);
        }

        if (attempt.demonId != null) {
            HeartDemonEntity demon = level.getEntity(attempt.demonId) instanceof HeartDemonEntity found ? found : null;
            if (demon != null) {
                if (demon.landedHits() >= 4) {
                    fail(player, cultivator, level, attempt, Principle.STILLNESS);
                    demon.discard();
                }
                return;
            }
            attempt.demonId = null;
        }

        if (Tribulation.isRunning(player)) return;

        if (attempt.readings >= READINGS_REQUIRED && requirementsMet(player, cultivator, attempt)) {
            succeed(player, cultivator, level, attempt);
        }
    }

    private static Attempt begin(ServerPlayer player, Cultivator cultivator, FormationCoreBlockEntity core) {
        Attempt attempt = new Attempt(core.getBlockPos(), cultivator.realm());
        ServerLevel level = player.level();

        switch (cultivator.realm()) {
            case BREATH_GATHERING -> {
                attempt.glyphs.addAll(cultivator.path().strongestTendencies(3));
                Tell.chat(player, Component.translatable("eotp.message.self_script_begins"));
                for (Tendency tendency : attempt.glyphs) {
                    Tell.chat(player, Component.translatable(
                        "eotp.message.self_script_glyph", Component.translatable(tendency.translationKey())
                    ));
                }
            }
            case FOUNDATION -> {
                attempt.verses.addAll(VerseScrolls.readAround(level, core.getBlockPos()));
                Tell.chat(player, Component.translatable("eotp.message.core_thesis_begins", attempt.verses.size()));
            }
            case GOLDEN_CORE -> Tell.chat(player, Component.translatable("eotp.message.nascent_begins"));
            default -> Tell.chat(player, Component.translatable(
                "eotp.message.ritual_begins", Component.translatable(cultivator.realm().next().translationKey())
            ));
        }

        EchoLog.record(level, core.getBlockPos(), EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.ritual_begun", player.getName()));
        return attempt;
    }

    /**
     * How well the circle is holding. Phase balance and harmony still matter, and so does everything
     * the cultivator has already established: a coherent Script, a settled Landscape and lessons
     * learned from earlier collapses all steady the ritual.
     */
    private static float readCircle(ServerPlayer player, Cultivator cultivator, FormationCoreBlockEntity core, PhaseBlend supplied) {
        float balance = supplied.similarity(PhaseBlend.BALANCED);
        float harmony = supplied.harmony();
        float reading = (balance * 0.6F + harmony * 0.4F) * core.strength();

        if (player.hasEffect(EOTPMobEffects.holder(EOTPMobEffects.CLEAR_HEART))) reading += 0.25F;

        SelfScript script = cultivator.path().selfScript();
        if (script != null) reading += 0.1F;

        InnerLandscape landscape = cultivator.path().landscape();
        if (landscape != null) reading += landscape.stability() * 0.15F;

        // Previous failures teach something rather than only punishing.
        reading += Math.min(0.15F, cultivator.failedBreakthroughs() * 0.03F);
        reading -= cultivator.path().discord().size() * 0.12F;
        return reading;
    }

    /** Foundation only: nudge the player toward whichever tendency is still unproven. */
    private static void promptGlyphs(ServerPlayer player, Attempt attempt) {
        if (attempt.glyphs.isEmpty()) return;
        if (attempt.readings % 10 != 0) return;
        for (Tendency tendency : attempt.glyphs) {
            if (attempt.shown.contains(tendency)) continue;
            Tell.overlay(player, Component.translatable(
                "eotp.message.self_script_waiting", Component.translatable(tendency.translationKey())
            ));
            return;
        }
    }

    /**
     * Called by the tendency tracker whenever the player does something meaningful, so a Foundation
     * ritual can watch them reproduce their own history.
     */
    public static void witnessTendency(ServerPlayer player, Tendency tendency) {
        Attempt attempt = ATTEMPTS.get(player.getUUID());
        if (attempt == null || attempt.from != Realm.BREATH_GATHERING) return;
        if (!attempt.glyphs.contains(tendency) || attempt.shown.contains(tendency)) return;

        attempt.shown.add(tendency);
        attempt.stability = Math.min(MAX_STABILITY, attempt.stability + 3.0F);
        Tell.chat(player, Component.translatable(
            "eotp.message.self_script_shown", Component.translatable(tendency.translationKey()),
            attempt.shown.size(), attempt.glyphs.size()
        ));
    }

    private static boolean requirementsMet(ServerPlayer player, Cultivator cultivator, Attempt attempt) {
        return switch (attempt.from) {
            case BREATH_GATHERING -> {
                boolean complete = attempt.shown.size() >= attempt.glyphs.size() && !attempt.glyphs.isEmpty();
                if (!complete && attempt.readings > READINGS_REQUIRED + GLYPH_PATIENCE) {
                    Tell.chat(player, Component.translatable("eotp.message.self_script_incomplete"));
                }
                yield complete;
            }
            case FOUNDATION -> {
                if (attempt.verses.size() >= 3) yield true;
                if (attempt.readings % 10 == 0) {
                    Tell.overlay(player, Component.translatable("eotp.message.need_three_verses", attempt.verses.size()));
                }
                yield false;
            }
            case GOLDEN_CORE -> {
                List<String> missing = cultivator.path().missingForNascentSpirit(cultivator.openMeridianCount());
                if (missing.isEmpty()) yield true;
                if (attempt.readings % 10 == 0) {
                    Tell.overlay(player, Component.translatable(missing.get(0)));
                }
                yield false;
            }
            default -> true;
        };
    }

    private static void summonHeartDemon(ServerLevel level, ServerPlayer player, Attempt attempt) {
        HeartDemonEntity demon = EOTPEntities.HEART_DEMON.get()
            .create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
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

    private static void succeed(ServerPlayer player, Cultivator cultivator, ServerLevel level, Attempt attempt) {
        ATTEMPTS.remove(player.getUUID());

        switch (attempt.from) {
            case BREATH_GATHERING -> {
                SelfScript script = SelfScript.from(attempt.shown);
                cultivator.path().setSelfScript(script);
                Tell.chat(player, Component.translatable("eotp.message.self_script_formed", script.describe()));
                EchoLog.record(level, attempt.corePos, EchoLog.Kind.RITUAL,
                    Component.translatable("eotp.echo.self_script", player.getName(), script.describe()));
            }
            case FOUNDATION -> {
                CoreThesis thesis = CoreThesis.condense(attempt.verses);
                if (thesis != null) {
                    cultivator.path().setThesis(thesis);
                    Tell.chat(player, Component.translatable(
                        "eotp.message.thesis_formed", Component.translatable(thesis.translationKey())
                    ));
                    EchoLog.record(level, attempt.corePos, EchoLog.Kind.RITUAL,
                        Component.translatable("eotp.echo.core_thesis", player.getName(),
                            Component.translatable(thesis.translationKey())));
                }
            }
            default -> { }
        }

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
    }

    /** Whichever principle the circle was failing on when it collapsed. */
    private static Principle failingPrinciple(Attempt attempt, PhaseBlend supplied) {
        if (attempt.from == Realm.BREATH_GATHERING && attempt.shown.size() < attempt.glyphs.size()) {
            return Principle.ECHO;
        }
        if (supplied.turbulence() > 0.4F) return Principle.FLOW;
        if (supplied.isEmpty()) return Principle.PRESERVATION;
        return Principle.STILLNESS;
    }

    private static void fail(ServerPlayer player, Cultivator cultivator, ServerLevel level, Attempt attempt, Principle principle) {
        ATTEMPTS.remove(player.getUUID());
        cultivator.recordDiscord(principle);
        cultivator.destabiliseCore(3000);
        CultivationStore.touch(player);

        player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.QI_DEVIATION, 600, 1));
        QiVisuals.backlash(level, player.position().add(0.0, 1.0, 0.0), cultivator.qiBlend());
        Tell.chat(player, Component.translatable("eotp.message.core_discord",
            Component.translatable(principle.translationKey())));
        EchoLog.record(level, attempt.corePos, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.breakthrough_failed", player.getName()));
    }

    public static boolean isAttempting(ServerPlayer player) {
        return ATTEMPTS.containsKey(player.getUUID());
    }

    public static void abandon(ServerPlayer player) {
        ATTEMPTS.remove(player.getUUID());
    }
}

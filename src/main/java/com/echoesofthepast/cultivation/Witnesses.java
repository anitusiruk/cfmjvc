package com.echoesofthepast.cultivation;

import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Watches for the three demonstrations that let a mortal begin cultivating at all.
 *
 * <p>None of these can be bought, waited out or ground: each one asks the player to make the world's
 * own systems behave, which is the entrance requirement replacing a meditation experience bar.
 */
public final class Witnesses {
    /** Seconds a reservoir must stay drawn-from and calm for the Witness of Earth. */
    private static final int EARTH_SECONDS = 60;
    /** Vein strength that counts as a live current worth drawing from. */
    private static final float EARTH_VEIN = 0.25F;
    /** Turbulence above this counts as roughening what you are drawing. */
    private static final float EARTH_TURBULENCE = 0.35F;
    /** How far from the player a qualifying reservoir may sit. */
    private static final int EARTH_RADIUS = 6;

    private static final Map<UUID, Integer> EARTH_PROGRESS = new HashMap<>();

    private Witnesses() {}

    /**
     * Called once a second for a mortal. Holding a connected reservoir steady on a live vein without
     * roughening its Qi eventually gives the Witness of Earth.
     */
    public static void tickEarth(ServerPlayer player, Cultivator cultivator) {
        if (cultivator.path().hasWitness(Witness.EARTH)) return;

        ServerLevel level = player.level();
        BlockPos standing = player.blockPosition();
        if (DragonVeins.strength(level, standing) < EARTH_VEIN) {
            EARTH_PROGRESS.remove(player.getUUID());
            return;
        }

        QiStorage steadied = findSteadyStorage(level, standing);
        if (steadied == null) {
            EARTH_PROGRESS.remove(player.getUUID());
            return;
        }

        int held = EARTH_PROGRESS.merge(player.getUUID(), 1, Integer::sum);
        if (held % 15 == 0 && held < EARTH_SECONDS) {
            Tell.overlay(player, Component.translatable("eotp.message.witness_earth_holding", held, EARTH_SECONDS));
        }
        if (held < EARTH_SECONDS) return;

        EARTH_PROGRESS.remove(player.getUUID());
        award(player, cultivator, Witness.EARTH);
    }

    /** A reservoir near the player that is holding Qi drawn from the ground without going rough. */
    private static QiStorage findSteadyStorage(ServerLevel level, BlockPos around) {
        for (BlockPos pos : BlockPos.betweenClosed(
            around.offset(-EARTH_RADIUS, -EARTH_RADIUS, -EARTH_RADIUS),
            around.offset(EARTH_RADIUS, EARTH_RADIUS, EARTH_RADIUS)
        )) {
            QiNode node = QiNet.nodeAt(level, pos);
            if (node == null) continue;
            QiStorage storage = node.qiStorage(null);
            if (storage == null) continue;
            if (storage.fillRatio() < 0.05F) continue;
            if (storage.turbulence() > EARTH_TURBULENCE) continue;
            return storage;
        }
        return null;
    }

    /**
     * Called by a Moon Lotus that has finished a whole night of production and closed at dawn while
     * still plumbed into something. Any mortal nearby has seen a complete spiritual cycle.
     */
    public static void completeHeaven(ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(pos) > 48.0 * 48.0) continue;
            Cultivator cultivator = Cultivation.of(player);
            if (cultivator == null || cultivator.realm() != Realm.MORTAL) continue;
            if (cultivator.path().hasWitness(Witness.HEAVEN)) continue;
            award(player, cultivator, Witness.HEAVEN);
        }
    }

    /** Called whenever practice is added: real work in one channel is the Witness of Self. */
    public static void checkSelf(ServerPlayer player, Cultivator cultivator) {
        if (cultivator.path().hasWitness(Witness.SELF)) return;
        if (!cultivator.hasSubstantialPractice()) return;
        award(player, cultivator, Witness.SELF);
    }

    private static void award(ServerPlayer player, Cultivator cultivator, Witness witness) {
        if (!cultivator.path().giveWitness(witness)) return;
        CultivationStore.touch(player);

        ServerLevel level = player.level();
        QiVisuals.bloom(level, player.position().add(0.0, 1.0, 0.0), cultivator.naturalBlend());
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9F, 1.1F);
        Tell.chat(player, Component.translatable(
            "eotp.message.witness_given", Component.translatable(witness.translationKey())
        ));

        if (cultivator.path().hasAllWitnesses()) {
            Tell.chat(player, Component.translatable("eotp.message.witnesses_complete"));
        }
    }

    /** Used by the compass so a mortal can see which proofs they have already given. */
    public static Component describe(Cultivator cultivator) {
        Component text = null;
        for (Witness witness : Witness.VALUES) {
            if (!cultivator.path().hasWitness(witness)) continue;
            Component name = Component.translatable(witness.translationKey());
            text = text == null ? name : Component.translatable("eotp.verse.join", text, name);
        }
        return text == null ? Component.translatable("eotp.message.witness_none") : text;
    }

    public static void forget(ServerPlayer player) {
        EARTH_PROGRESS.remove(player.getUUID());
    }
}

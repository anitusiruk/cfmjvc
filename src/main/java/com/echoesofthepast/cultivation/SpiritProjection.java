package com.echoesofthepast.cultivation;

import com.echoesofthepast.entity.MeditatingBodyEntity;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPEntities;
import com.echoesofthepast.util.Tell;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The nascent spirit stepping out of the body.
 *
 * <p>A projection can fly, look, and touch seals. It cannot mine, cannot pick anything up, and cannot
 * fight - those restrictions are enforced by cancelling the relevant events while a projection is
 * out, rather than by pretending to be a different game mode.
 *
 * <p>The body stays where it was sitting. Wander too far from it, run out of Qi, or let the body be
 * disturbed, and the spirit snaps back.
 */
public final class SpiritProjection {
    /** How far a spirit may stray from its body. */
    private static final double LEASH = 64.0;
    /** Qi drained per second while out. */
    private static final float UPKEEP = 2.0F;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private SpiritProjection() {}

    private static final class Session {
        private final BlockPos anchor;
        private final UUID bodyId;
        private final boolean couldFly;

        private Session(BlockPos anchor, UUID bodyId, boolean couldFly) {
            this.anchor = anchor;
            this.bodyId = bodyId;
            this.couldFly = couldFly;
        }
    }

    public static boolean isProjecting(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    /** Steps out. Returns false with a reason already shown if the cultivator cannot. */
    public static boolean begin(ServerPlayer player) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return false;
        if (!cultivator.realm().canProject() || !cultivator.knows(Discovery.NASCENT_PROJECTION)) {
            Tell.overlay(player, "eotp.message.cannot_project");
            return false;
        }
        if (isProjecting(player)) {
            end(player, true);
            return true;
        }
        if (cultivator.qi() < cultivator.qiCapacity() * 0.3F) {
            Tell.overlay(player, "eotp.message.not_enough_qi");
            return false;
        }

        ServerLevel level = player.level();
        MeditatingBodyEntity body = EOTPEntities.MEDITATING_BODY.get().create(level, EntitySpawnReason.EVENT);
        if (body == null) return false;
        body.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        body.setOwner(player.getUUID());
        level.addFreshEntity(body);

        SESSIONS.put(player.getUUID(), new Session(player.blockPosition(), body.getUUID(), player.getAbilities().mayfly));

        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.setInvulnerable(true);

        QiVisuals.bloom(level, player.position().add(0.0, 1.0, 0.0), PhaseBlend.BALANCED);
        Tell.chat(player, Component.translatable("eotp.message.projection_begins"));
        return true;
    }

    /** Called once a second while a projection is out. */
    public static void tick(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) return;
        ServerLevel level = player.level();

        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null || !cultivator.spendQi(UPKEEP)) {
            end(player, false);
            return;
        }
        CultivationStore.touch(player);

        if (level.getEntity(session.bodyId) == null) {
            // Something happened to the body. The spirit is yanked home whether it likes it or not.
            end(player, false);
            return;
        }

        if (player.blockPosition().distSqr(session.anchor) > LEASH * LEASH) {
            Tell.overlay(player, "eotp.message.silver_cord_pulls");
            end(player, false);
            return;
        }

        QiVisuals.echo(level, player.position().add(0.0, 1.0, 0.0), 2);
    }

    /** Returns the spirit to its body. */
    public static void end(ServerPlayer player, boolean willing) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) return;
        ServerLevel level = player.level();

        if (level.getEntity(session.bodyId) instanceof MeditatingBodyEntity body) {
            player.teleportTo(body.getX(), body.getY(), body.getZ());
            body.discard();
        } else {
            Vec3 anchor = Vec3.atBottomCenterOf(session.anchor);
            player.teleportTo(anchor.x, anchor.y, anchor.z);
        }

        player.getAbilities().mayfly = session.couldFly;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setInvulnerable(false);

        QiVisuals.echo(level, player.position().add(0.0, 1.0, 0.0), 8);
        Tell.chat(player, willing
            ? Component.translatable("eotp.message.projection_ends")
            : Component.translatable("eotp.message.projection_snaps"));
    }

    /** Where a projecting player's body is, for anything that needs to know. */
    public static @Nullable BlockPos anchorOf(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        return session == null ? null : session.anchor;
    }
}

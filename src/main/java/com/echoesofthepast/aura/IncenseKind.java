package com.echoesofthepast.aura;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * What a censer is burning. Incense is slow, area-wide and atmospheric: none of these do anything
 * sharp, they change what the room is like to be in.
 */
public enum IncenseKind implements StringRepresentable {
    /** Settles minds and animals alike. The smell of a room where breakthroughs happen. */
    CALMING("calming", 10.0) {
        @Override
        public void breathe(ServerLevel level, BlockPos pos, double radius, float strength, @Nullable Phase phase) {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, area(pos, radius))) {
                mob.setTarget(null);
            }
            for (Player player : level.getEntitiesOfClass(Player.class, area(pos, radius))) {
                player.addEffect(EOTPMobEffects.quiet(EOTPMobEffects.CLEAR_HEART, 120, 0));
            }
        }
    },
    /** Makes the past visible: recent echoes show themselves without a mirror. */
    REVEALING("revealing", 12.0) {
        @Override
        public void breathe(ServerLevel level, BlockPos pos, double radius, float strength, @Nullable Phase phase) {
            for (Player player : level.getEntitiesOfClass(Player.class, area(pos, radius))) {
                player.addEffect(EOTPMobEffects.quiet(EOTPMobEffects.SPIRIT_SIGHT, 120, 0));
            }
            for (EchoLog.Echo echo : EchoLog.near(level, pos, (int) radius, 6)) {
                QiVisuals.echo(level, Vec3.atCenterOf(echo.pos()), 2);
            }
        }
    },
    /** Unpleasant to things that should be dead. */
    SUPPRESSING("suppressing", 10.0) {
        @Override
        public void breathe(ServerLevel level, BlockPos pos, double radius, float strength, @Nullable Phase phase) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area(pos, radius))) {
                if (entity.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                    entity.hurtServer(level, level.damageSources().magic(), 1.0F + strength);
                }
            }
        }
    },
    /** Speeds up cultivation for anybody sitting still in it. */
    KINDLING("kindling", 8.0) {
        @Override
        public void breathe(ServerLevel level, BlockPos pos, double radius, float strength, @Nullable Phase phase) {
            for (Player player : level.getEntitiesOfClass(Player.class, area(pos, radius))) {
                if (player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) continue;
                Cultivation.insight(player, 0.25F * strength);
            }
        }
    },
    /**
     * Slowly pushes the Qi in every nearby device towards one phase. The cheapest way to fix a
     * workshop that has drifted, and a nuisance if you forget it is burning.
     */
    ATTUNING("attuning", 6.0) {
        @Override
        public void breathe(ServerLevel level, BlockPos pos, double radius, float strength, @Nullable Phase phase) {
            Phase chosen = phase == null ? Phase.EARTH : phase;
            for (BlockPos target : BlockPos.betweenClosed(
                pos.offset((int) -radius, -2, (int) -radius), pos.offset((int) radius, 2, (int) radius))) {
                QiNode node = QiNet.nodeAt(level, target);
                if (node == null) continue;
                QiStorage storage = node.qiStorage(null);
                if (storage == null || storage.isEmpty()) continue;
                storage.setBlend(storage.blend().lerp(PhaseBlend.of(chosen), 0.02F * strength));
                storage.calmTurbulence(0.005F * strength);
            }
        }
    };

    public static final IncenseKind[] VALUES = values();
    public static final Codec<IncenseKind> CODEC = StringRepresentable.fromEnum(IncenseKind::values);

    private final String name;
    private final double radius;

    IncenseKind(String name, double radius) {
        this.name = name;
        this.radius = radius;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public double radius() {
        return this.radius;
    }

    public String itemName() {
        return this.name + "_incense";
    }

    public String translationKey() {
        return "eotp.incense." + this.name;
    }

    protected static AABB area(BlockPos pos, double radius) {
        return new AABB(pos).inflate(radius, Math.min(6.0, radius), radius);
    }

    /** Called once a second while the censer burns. */
    public abstract void breathe(ServerLevel level, BlockPos pos, double radius, float strength, @Nullable Phase phase);
}

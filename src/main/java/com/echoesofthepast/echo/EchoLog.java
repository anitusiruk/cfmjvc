package com.echoesofthepast.echo;

import com.echoesofthepast.EOTPConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The world's memory of what has recently happened where.
 *
 * <p>This is what gives the mod's theme a verb: an Echo Mirror does not invent ghostly scenes, it
 * reads this log. Entries expire, so the past genuinely fades, and the whole thing is capped so that
 * a busy server cannot grow it without limit.
 */
public final class EchoLog extends SavedData {
    private static final int MAX_ENTRIES = 4096;

    public enum Kind implements StringRepresentable {
        BLOCK_BROKEN("block_broken"),
        BLOCK_PLACED("block_placed"),
        ITEM_DROPPED("item_dropped"),
        DEATH("death"),
        RITUAL("ritual"),
        TECHNIQUE("technique"),
        PASSAGE("passage");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String translationKey() {
            return "eotp.echo_kind." + this.name;
        }
    }

    /**
     * @param tick        when it happened
     * @param pos         where it happened
     * @param kind        what sort of event it was
     * @param description a short line the mirror can show
     */
    public record Echo(long tick, BlockPos pos, Kind kind, Component description) {
        public static final Codec<Echo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("tick").forGetter(Echo::tick),
            BlockPos.CODEC.fieldOf("pos").forGetter(Echo::pos),
            Kind.CODEC.fieldOf("kind").forGetter(Echo::kind),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(Echo::description)
        ).apply(instance, Echo::new));
    }

    private final List<Echo> echoes;

    public EchoLog() {
        this.echoes = new ArrayList<>();
    }

    private EchoLog(List<Echo> echoes) {
        this.echoes = new ArrayList<>(echoes);
    }

    public static final Codec<EchoLog> CODEC = Echo.CODEC.listOf()
        .xmap(EchoLog::new, log -> List.copyOf(log.echoes));

    /** One log per dimension, since an echo belongs to a place. */
    private static final Map<Identifier, SavedDataType<EchoLog>> TYPES = new HashMap<>();

    private static SavedDataType<EchoLog> typeFor(ServerLevel level) {
        return TYPES.computeIfAbsent(level.dimension().identifier(), id -> new SavedDataType<>(
            Identifier.fromNamespaceAndPath("eotp", "echoes_" + id.getNamespace() + "_" + id.getPath()),
            EchoLog::new, CODEC, null
        ));
    }

    public static EchoLog of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(typeFor(level));
    }

    public static void record(ServerLevel level, BlockPos pos, Kind kind, Component description) {
        EchoLog log = of(level);
        log.echoes.add(new Echo(level.getGameTime(), pos.immutable(), kind, description));
        log.prune(level.getGameTime());
        log.setDirty();
    }

    private void prune(long now) {
        long cutoff = now - EOTPConfig.echoMemoryTicks();
        this.echoes.removeIf(echo -> echo.tick() < cutoff);
        while (this.echoes.size() > MAX_ENTRIES) {
            this.echoes.remove(0);
        }
    }

    /** Everything the world still remembers near a point, most recent first. */
    public static List<Echo> near(ServerLevel level, BlockPos center, int radius, int limit) {
        EchoLog log = of(level);
        long now = level.getGameTime();
        log.prune(now);
        List<Echo> found = new ArrayList<>();
        double radiusSqr = (double) radius * radius;
        for (Echo echo : log.echoes) {
            if (echo.pos().distSqr(center) <= radiusSqr) {
                found.add(echo);
            }
        }
        found.sort(Comparator.comparingLong(Echo::tick).reversed());
        return found.size() > limit ? List.copyOf(found.subList(0, limit)) : List.copyOf(found);
    }

    /** How thick the echoes are around a point, which lanterns and censers make stronger. */
    public static float density(ServerLevel level, BlockPos center, int radius) {
        return Math.min(1.0F, near(level, center, radius, 64).size() / 24.0F);
    }
}

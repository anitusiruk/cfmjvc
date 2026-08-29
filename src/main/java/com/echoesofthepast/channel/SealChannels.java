package com.echoesofthepast.channel;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Which containers and devices have been stamped with which imperial seal.
 *
 * <p>This is the mod's answer to numbered network channels: a mark is a short word carved on a seal
 * face, and everything stamped with the same mark is treated as one household. Cranes, tablets and
 * spirits all navigate by these marks, and a player can read a stamp off a chest by looking at it
 * with the seal in hand.
 */
public final class SealChannels extends SavedData {
    public static final Codec<SealChannels> CODEC = Codec
        .unboundedMap(Codec.STRING, Codec.STRING)
        .xmap(SealChannels::fromRaw, SealChannels::toRaw);

    private static final Map<Identifier, SavedDataType<SealChannels>> TYPES = new HashMap<>();

    private final Map<BlockPos, String> stamps;

    public SealChannels() {
        this.stamps = new HashMap<>();
    }

    private static SealChannels fromRaw(Map<String, String> raw) {
        SealChannels channels = new SealChannels();
        raw.forEach((key, channel) -> channels.stamps.put(BlockPos.of(Long.parseLong(key)), channel));
        return channels;
    }

    private static Map<String, String> toRaw(SealChannels channels) {
        Map<String, String> raw = new HashMap<>();
        channels.stamps.forEach((pos, channel) -> raw.put(String.valueOf(pos.asLong()), channel));
        return raw;
    }

    private static SavedDataType<SealChannels> typeFor(ServerLevel level) {
        return TYPES.computeIfAbsent(level.dimension().identifier(), id -> new SavedDataType<>(
            Identifier.fromNamespaceAndPath("eotp", "seal_channels_" + id.getNamespace() + "_" + id.getPath()),
            SealChannels::new, CODEC, null
        ));
    }

    public static SealChannels of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(typeFor(level));
    }

    public static void stamp(ServerLevel level, BlockPos pos, String channel) {
        SealChannels channels = of(level);
        channels.stamps.put(pos.immutable(), channel);
        channels.setDirty();
    }

    public static void clear(ServerLevel level, BlockPos pos) {
        SealChannels channels = of(level);
        if (channels.stamps.remove(pos) != null) {
            channels.setDirty();
        }
    }

    public static @Nullable String at(ServerLevel level, BlockPos pos) {
        return of(level).stamps.get(pos);
    }

    /**
     * The nearest place on a channel that can actually take an item. Stamps whose block has been
     * broken are forgotten as they are found, so the registry cleans itself up.
     */
    public static @Nullable BlockPos findNearest(ServerLevel level, String channel, BlockPos from, int maxDistance) {
        SealChannels channels = of(level);
        List<BlockPos> stale = new ArrayList<>();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Map.Entry<BlockPos, String> entry : channels.stamps.entrySet()) {
            if (!entry.getValue().equals(channel)) continue;
            BlockPos pos = entry.getKey();
            if (!level.isLoaded(pos)) continue;
            if (!(level.getBlockEntity(pos) instanceof Container)) {
                stale.add(pos);
                continue;
            }
            double distance = pos.distSqr(from);
            if (distance > (double) maxDistance * maxDistance) continue;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos;
            }
        }

        if (!stale.isEmpty()) {
            stale.forEach(channels.stamps::remove);
            channels.setDirty();
        }
        return best;
    }

    /** Every stamped position on a channel, used when a tablet is looking for its household. */
    public static List<BlockPos> all(ServerLevel level, String channel) {
        List<BlockPos> found = new ArrayList<>();
        of(level).stamps.forEach((pos, stamped) -> {
            if (stamped.equals(channel)) found.add(pos);
        });
        return found;
    }
}

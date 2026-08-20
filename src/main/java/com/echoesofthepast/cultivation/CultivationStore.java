package com.echoesofthepast.cultivation;

import com.echoesofthepast.EchoesOfThePast;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/** The world's record of who has been cultivating and how far they have got. */
public final class CultivationStore extends SavedData {
    public static final Codec<CultivationStore> CODEC = Codec
        .unboundedMap(UUIDUtil.STRING_CODEC, Cultivator.CODEC)
        .xmap(CultivationStore::new, store -> store.cultivators);

    public static final SavedDataType<CultivationStore> TYPE = new SavedDataType<>(
        EchoesOfThePast.id("cultivation"), CultivationStore::new, CODEC, null
    );

    private final Map<UUID, Cultivator> cultivators;

    public CultivationStore() {
        this.cultivators = new HashMap<>();
    }

    private CultivationStore(Map<UUID, Cultivator> cultivators) {
        this.cultivators = new HashMap<>(cultivators);
    }

    public static CultivationStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Cultivator of(UUID id) {
        Cultivator cultivator = this.cultivators.computeIfAbsent(id, ignored -> new Cultivator());
        this.setDirty();
        return cultivator;
    }

    public @Nullable Cultivator peek(UUID id) {
        return this.cultivators.get(id);
    }

    /**
     * The usual entry point. Returns null on the client, where cultivation state is deliberately
     * not mirrored - the player is meant to read their state from the world, not from a HUD.
     */
    public static @Nullable Cultivator of(Player player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return null;
        return get(server).of(player.getUUID());
    }

    /** Marks the record dirty after a change. Call this whenever a cultivator is mutated. */
    public static void touch(Player player) {
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            get(server).setDirty();
        }
    }
}

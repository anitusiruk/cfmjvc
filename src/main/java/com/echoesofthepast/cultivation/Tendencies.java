package com.echoesofthepast.cultivation;

import net.minecraft.server.level.ServerPlayer;

/**
 * The quiet bookkeeping behind the Self-Script. Ordinary play files a little weight under whichever
 * tendency an action belongs to, so by the time a cultivator reaches Foundation the world already
 * knows what kind of person they have been.
 *
 * <p>Every note is also offered to an in-progress Foundation ritual, which is how a player proves a
 * glyph by doing the thing rather than by selecting it from a list.
 */
public final class Tendencies {
    private Tendencies() {}

    public static void note(ServerPlayer player, Tendency tendency, float weight) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;

        cultivator.path().noteTendency(tendency, weight);
        CultivationStore.touch(player);
        BreakthroughRitual.witnessTendency(player, tendency);
        Verses.witnessTendency(player, tendency);
    }
}

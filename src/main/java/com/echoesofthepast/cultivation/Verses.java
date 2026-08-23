package com.echoesofthepast.cultivation;

import com.echoesofthepast.util.Tell;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Watches whether a cultivator is currently demonstrating the principles written on a Verse Scroll.
 *
 * <p>A Verse is only mastered when the world sees every principle on the scroll enacted inside the
 * same short window, which is what makes it proof of a relationship rather than a checklist. The
 * evidence is deliberately not persisted: a Verse has to be shown as one continuous piece of work.
 */
public final class Verses {
    /** How long a demonstrated principle stays fresh, in ticks. */
    private static final int MEMORY_TICKS = 1200;

    private static final Map<UUID, Map<Principle, Long>> RECENT = new HashMap<>();

    private Verses() {}

    /** Records that the player has just enacted a principle. */
    public static void demonstrate(ServerPlayer player, Principle principle) {
        RECENT.computeIfAbsent(player.getUUID(), ignored -> new EnumMap<>(Principle.class))
            .put(principle, player.level().getGameTime());

        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;

        // Demonstrating a principle again is exactly how outstanding Discord is repaired.
        if (cultivator.path().resolveDiscord(principle)) {
            CultivationStore.touch(player);
            Tell.chat(player, Component.translatable(
                "eotp.message.discord_settled", Component.translatable(principle.translationKey())
            ));
        }
    }

    /** Tendencies imply principles, so ordinary play feeds the Verse system too. */
    static void witnessTendency(ServerPlayer player, Tendency tendency) {
        Principle implied = switch (tendency) {
            case PROTECTING -> Principle.PRESERVATION;
            case RETURNING -> Principle.RETURN;
            case TRANSFORMING -> Principle.TRANSFORMATION;
            case WANDERING -> Principle.MOTION;
            case ENDURING -> Principle.EARTH;
            case CREATING -> Principle.GROWTH;
            case CUTTING -> Principle.EDGE;
            case TENDING -> Principle.WATER;
            case COMMANDING -> Principle.COMMAND;
            case OBSERVING -> Principle.ECHO;
            case STILLNESS -> Principle.STILLNESS;
        };
        demonstrate(player, implied);
    }

    /** Which principles this cultivator is holding true right now. */
    public static Set<Principle> currentlyDemonstrated(ServerPlayer player) {
        Map<Principle, Long> recent = RECENT.get(player.getUUID());
        if (recent == null) return Set.of();

        long now = player.level().getGameTime();
        recent.entrySet().removeIf(entry -> now - entry.getValue() > MEMORY_TICKS);
        return EnumSet.copyOf(recent.isEmpty() ? EnumSet.noneOf(Principle.class) : EnumSet.copyOf(recent.keySet()));
    }

    /**
     * Tests a written Verse against what the player is currently proving.
     *
     * @return true if every principle on it is being held true at once
     */
    public static boolean isDemonstrating(ServerPlayer player, Verse verse) {
        Set<Principle> shown = currentlyDemonstrated(player);
        return shown.containsAll(verse.principles());
    }

    public static void forget(ServerPlayer player) {
        RECENT.remove(player.getUUID());
    }
}

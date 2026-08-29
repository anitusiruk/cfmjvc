package com.echoesofthepast.cultivation;

import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.util.Tell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** Convenience wrappers over {@link CultivationStore} for the rest of the mod to use. */
public final class Cultivation {
    private Cultivation() {}

    public static @Nullable Cultivator of(Player player) {
        return CultivationStore.of(player);
    }

    public static Realm realmOf(Player player) {
        Cultivator cultivator = of(player);
        return cultivator == null ? Realm.MORTAL : cultivator.realm();
    }

    public static boolean hasOpen(Player player, Meridian meridian) {
        Cultivator cultivator = of(player);
        return cultivator != null && cultivator.isUsable(meridian, (int) player.level().getGameTime());
    }

    /**
     * Pays a technique's cost out of the cultivator's own Qi.
     *
     * @return true if the technique may fire.
     */
    public static boolean spend(Player player, float cost) {
        Cultivator cultivator = of(player);
        if (cultivator == null) return false;
        if (!cultivator.spendQi(cost)) {
            Tell.overlay(player, "eotp.message.not_enough_qi");
            return false;
        }
        CultivationStore.touch(player);
        return true;
    }

    /**
     * Practice towards opening a channel, announcing the moment it gives way. Practice accrues even
     * for a mortal who cannot open anything yet, so nothing done early is wasted, and enough of it
     * in any one channel is the Witness of Self.
     */
    public static void practise(Player player, Meridian meridian, float amount) {
        Cultivator cultivator = of(player);
        if (cultivator == null) return;
        if (cultivator.practise(meridian, amount)) {
            Tell.chat(player, Component.translatable("eotp.message.meridian_opened",
                Component.translatable(meridian.translationKey())));
            checkReadiness(player);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Witnesses.checkSelf(serverPlayer, cultivator);
        }
        CultivationStore.touch(player);
    }

    /**
     * Announces when the next realm has become attemptable. There is no bar being filled here: each
     * realm asks for a different proof, and this only reports that the proof is now in hand.
     */
    public static void checkReadiness(Player player) {
        Cultivator cultivator = of(player);
        if (cultivator == null || !cultivator.readyToBreakThrough()) return;
        Tell.chat(player, Component.translatable("eotp.message.breakthrough_ready",
            Component.translatable(cultivator.realm().next().translationKey())));
    }

    public static void grantQi(Player player, float amount, PhaseBlend blend) {
        Cultivator cultivator = of(player);
        if (cultivator == null) return;
        cultivator.addQi(amount, blend);
        CultivationStore.touch(player);
    }

    /** Teaches a discovery, telling the player only if it is new to them. */
    public static boolean teach(Player player, String discovery) {
        Cultivator cultivator = of(player);
        if (cultivator == null) return false;
        if (!cultivator.learn(discovery)) return false;
        CultivationStore.touch(player);
        Tell.chat(player, Component.translatable("eotp.message.learned",
            Component.translatable(Discovery.translationKey(discovery))));
        return true;
    }

    public static boolean knows(Player player, String discovery) {
        Cultivator cultivator = of(player);
        return cultivator != null && cultivator.knows(discovery);
    }

    /** The one-line status a compass or a tablet reports. */
    public static Component describe(ServerPlayer player) {
        Cultivator cultivator = of(player);
        if (cultivator == null) return Component.empty();
        SelfScript script = cultivator.path().selfScript();
        CoreThesis thesis = cultivator.path().thesis();
        InnerLandscape landscape = cultivator.path().landscape();

        return Component.translatable(
            "eotp.message.cultivation_status",
            Component.translatable(cultivator.realm().translationKey()),
            Math.round(cultivator.qi()),
            Math.round(cultivator.qiCapacity()),
            cultivator.openMeridianCount(),
            script == null ? Component.translatable("eotp.message.none") : script.describe(),
            thesis == null ? Component.translatable("eotp.message.none") : Component.translatable(thesis.translationKey()),
            landscape == null ? Component.translatable("eotp.message.none") : landscape.name(),
            cultivator.path().masteredVerses().size()
        );
    }
}

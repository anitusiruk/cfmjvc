package com.echoesofthepast.cultivation;

import com.echoesofthepast.block.formation.FormationCoreBlockEntity;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.formation.FormationType;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.util.Tell;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Changing a spiritual root.
 *
 * <p>A cultivator stands in a running attunement circuit and feeds it a five-phase essence. The
 * phase in that essence is added to their root; feeding the same essence again removes it. Roots are
 * therefore built up deliberately over several offerings, and a player who wants the dangerous
 * conflicted combination has to choose it on purpose.
 *
 * <p>Rewriting a root is not free: it costs the circuit a great deal of Qi and rattles a golden core,
 * which is what stops it from being something you flip between fights.
 */
public final class RootAttunement {
    /** Qi the circuit spends to rewrite a root. */
    private static final float COST = 240.0F;

    private RootAttunement() {}

    /**
     * @return true if the essence was consumed.
     */
    public static boolean offer(ServerLevel level, ServerPlayer player, FormationCoreBlockEntity core, ItemStack essence, Phase phase) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return false;

        if (!core.isRunning(FormationType.ATTUNEMENT)) {
            Tell.overlay(player, "eotp.message.attunement_not_running");
            return false;
        }
        if (!core.covers(player)) {
            Tell.overlay(player, "eotp.message.stand_in_the_circle");
            return false;
        }
        if (!core.consume(COST)) {
            Tell.overlay(player, "eotp.message.circuit_too_weak");
            return false;
        }

        SpiritualRoot current = cultivator.root();
        List<Phase> phases = new ArrayList<>(current.phases());
        boolean removing = phases.contains(phase);
        if (removing) {
            phases.remove(phase);
        } else if (phases.size() >= SpiritualRoot.MAX_PHASES) {
            Tell.overlay(player, "eotp.message.root_full");
            return false;
        } else {
            phases.add(phase);
        }

        SpiritualRoot updated = new SpiritualRoot(phases);
        cultivator.setRoot(updated);
        // Rewriting what you are made of shakes a core loose for a while.
        cultivator.destabiliseCore(2400);
        CultivationStore.touch(player);
        essence.shrink(1);

        QiVisuals.bloom(level, player.position().add(0.0, 1.0, 0.0), PhaseBlend.of(phase));
        Tell.chat(player, Component.translatable(
            removing ? "eotp.message.root_severed" : "eotp.message.root_attuned",
            Component.translatable(phase.translationKey()),
            updated.describe(),
            Math.round(updated.coherence() * 100.0F)));

        if (updated.isConflicted()) {
            Tell.chat(player, Component.translatable("eotp.message.root_conflicted",
                Math.round((updated.powerMultiplier() - 1.0F) * 100.0F),
                Math.round(updated.deviationRisk() * 100.0F)));
        }

        Cultivation.teach(player, Discovery.ROOT_ATTUNEMENT);
        EchoLog.record(level, core.getBlockPos(), EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.attunement", player.getName(), Component.translatable(phase.translationKey())));
        return true;
    }
}

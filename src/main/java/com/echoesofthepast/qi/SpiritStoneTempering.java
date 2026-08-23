package com.echoesofthepast.qi;

import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.registry.EOTPItems;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Spirit stone grades are earned, not compressed.
 *
 * <p>A Low stone is ordinary storage. It becomes a <b>Middle</b> stone only after genuinely
 * participating in cultivation: it has to carry at least three different phases through itself
 * without ever being allowed to go badly turbulent, and it remembers those phases as inclusions.
 *
 * <p>A Middle stone becomes <b>High</b> only after surviving routed tribulation lightning and being
 * quenched in Spirit Spring Water before the charge settles. A High stone is therefore a piece of
 * history rather than nine batteries welded together.
 */
public final class SpiritStoneTempering {
    /** Distinct phases a Low stone must carry before it can be cut into a Middle stone. */
    public static final int PHASES_REQUIRED = 3;
    /** Turbulence that ruins the tempering and empties the stone's memory. */
    public static final float SPOIL_TURBULENCE = 0.5F;
    /** How long a tribulation charge stays quenchable, in ticks. */
    public static final int CHARGE_WINDOW = 600;

    private SpiritStoneTempering() {}

    public static List<Phase> historyOf(ItemStack stack) {
        List<Phase> history = stack.get(EOTPComponents.PHASE_HISTORY.get());
        return history == null ? List.of() : history;
    }

    /**
     * Records that a phase has passed through the stone.
     *
     * @return true if this phase was new to it
     */
    public static boolean recordPhase(ItemStack stack, Phase phase) {
        EnumSet<Phase> carried = EnumSet.noneOf(Phase.class);
        carried.addAll(historyOf(stack));
        if (!carried.add(phase)) return false;
        stack.set(EOTPComponents.PHASE_HISTORY.get(), List.copyOf(new ArrayList<>(carried)));
        return true;
    }

    /** Roughening the stone loses what it had learned; tempering asks for care, not just time. */
    public static void spoil(ItemStack stack) {
        stack.remove(EOTPComponents.PHASE_HISTORY.get());
    }

    public static boolean isTempered(ItemStack stack) {
        return historyOf(stack).size() >= PHASES_REQUIRED;
    }

    /** A Middle stone that lightning has just passed through, still hot enough to quench. */
    public static boolean isTribulationCharged(ItemStack stack, long now) {
        Integer chargedAt = stack.get(EOTPComponents.TRIBULATION_CHARGE.get());
        return chargedAt != null && now - chargedAt <= CHARGE_WINDOW;
    }

    public static void markTribulationCharged(ItemStack stack, long now) {
        stack.set(EOTPComponents.TRIBULATION_CHARGE.get(), (int) now);
    }

    /** Cuts a fully tempered Low stone into a Middle stone, carrying nothing else over. */
    public static ItemStack cutToMiddle(ItemStack tempered) {
        tempered.shrink(1);
        return new ItemStack(EOTPItems.MIDDLE_SPIRIT_STONE.get());
    }

    /** Quenches a charged Middle stone into a High stone. */
    public static ItemStack quenchToHigh(ItemStack charged) {
        charged.shrink(1);
        return new ItemStack(EOTPItems.HIGH_SPIRIT_STONE.get());
    }

    /** Tooltip-style summary the reservoir reports when a stone is put in or taken out. */
    public static Component describe(ItemStack stack) {
        List<Phase> history = historyOf(stack);
        if (history.isEmpty()) return Component.translatable("eotp.message.stone_unmarked");

        Component text = Component.translatable(history.get(0).translationKey());
        for (int index = 1; index < history.size(); index++) {
            text = Component.translatable("eotp.verse.join", text,
                Component.translatable(history.get(index).translationKey()));
        }
        return Component.translatable("eotp.message.stone_history", text, history.size(), PHASES_REQUIRED);
    }
}

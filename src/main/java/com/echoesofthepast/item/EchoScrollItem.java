package com.echoesofthepast.item;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.CultivationStore;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.cultivation.Principle;
import com.echoesofthepast.cultivation.Realm;
import com.echoesofthepast.cultivation.Verse;
import com.echoesofthepast.cultivation.Verses;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.util.Tell;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Paper that holds understanding. A scroll can carry three different things:
 *
 * <ul>
 *   <li>a <b>teaching</b>, which permanently grants one discovery to whoever reads it;</li>
 *   <li>a <b>draft Verse</b>, which is a set of principles the writer intends to prove together;</li>
 *   <li>a <b>mastered Verse</b>, which is a draft the world has actually watched hold true.</li>
 * </ul>
 *
 * <p>A mastered Verse can be handed to another cultivator, but they cannot simply consume it: the
 * scroll shows them the relationship, and they still have to reproduce it once themselves before it
 * counts toward their own Golden Core.
 */
public class EchoScrollItem extends Item {
    public EchoScrollItem(Properties properties) {
        super(properties);
    }

    public static @Nullable String teachingOf(ItemStack stack) {
        return stack.get(EOTPComponents.TEACHING.get());
    }

    public static @Nullable Verse draftVerseOf(ItemStack stack) {
        return stack.get(EOTPComponents.DRAFT_VERSE.get());
    }

    public static @Nullable Verse masteredVerseOf(ItemStack stack) {
        return stack.get(EOTPComponents.MASTERED_VERSE.get());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        Verse mastered = masteredVerseOf(stack);
        if (mastered != null) {
            return this.readMastered(serverPlayer, mastered);
        }

        Verse draft = draftVerseOf(stack);
        if (draft != null) {
            return this.proveDraft(serverPlayer, stack, draft);
        }

        String teaching = teachingOf(stack);
        if (teaching != null) {
            return this.readTeaching(serverPlayer, stack, teaching);
        }

        return this.writeBlank(serverPlayer, stack);
    }

    /** A finished Verse shows its relationship; proving it once makes it the reader's own. */
    private InteractionResult readMastered(ServerPlayer player, Verse verse) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return InteractionResult.PASS;

        if (cultivator.path().hasMastered(verse)) {
            Tell.overlay(player, Component.translatable("eotp.message.verse_known", verse.describe()));
            return InteractionResult.SUCCESS;
        }
        if (!Verses.isDemonstrating(player, verse)) {
            Tell.chat(player, Component.translatable("eotp.message.verse_must_reproduce", verse.describe()));
            return InteractionResult.SUCCESS;
        }

        cultivator.path().masterVerse(verse);
        CultivationStore.touch(player);
        Tell.chat(player, Component.translatable("eotp.message.verse_mastered", verse.describe()));
        return InteractionResult.SUCCESS;
    }

    /** A draft awakens the moment the world sees every principle on it holding at once. */
    private InteractionResult proveDraft(ServerPlayer player, ItemStack stack, Verse draft) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return InteractionResult.PASS;

        if (!Verses.isDemonstrating(player, draft)) {
            Tell.chat(player, Component.translatable("eotp.message.verse_incomplete", draft.describe()));
            return InteractionResult.SUCCESS;
        }

        stack.remove(EOTPComponents.DRAFT_VERSE.get());
        stack.set(EOTPComponents.MASTERED_VERSE.get(), draft);
        cultivator.path().masterVerse(draft);
        CultivationStore.touch(player);
        Tell.chat(player, Component.translatable("eotp.message.verse_awakened", draft.describe()));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult readTeaching(ServerPlayer player, ItemStack stack, String teaching) {
        if (Cultivation.knows(player, teaching)) {
            Tell.overlay(player, Component.translatable("eotp.message.scroll_already_known",
                Component.translatable(Discovery.translationKey(teaching))));
            return InteractionResult.SUCCESS;
        }
        Cultivation.teach(player, teaching);
        Tell.chat(player, Component.translatable("eotp.message.scroll_read",
            Component.translatable(Discovery.translationKey(teaching))));
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    /**
     * A blank sheet. At Foundation it drafts a Verse from whatever the cultivator is proving right
     * now; before that it can only copy down a discovery they already hold.
     */
    private InteractionResult writeBlank(ServerPlayer player, ItemStack stack) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return InteractionResult.PASS;

        if (cultivator.realm().atLeast(Realm.FOUNDATION)) {
            List<Principle> shown = new ArrayList<>(Verses.currentlyDemonstrated(player));
            if (shown.size() >= Verse.MINIMUM) {
                if (shown.size() > Verse.MAXIMUM) {
                    shown = new ArrayList<>(shown.subList(0, Verse.MAXIMUM));
                }
                Verse draft = new Verse(shown);
                stack.set(EOTPComponents.DRAFT_VERSE.get(), draft);
                Tell.chat(player, Component.translatable("eotp.message.verse_drafted", draft.describe()));
                return InteractionResult.SUCCESS;
            }
            Tell.chat(player, Component.translatable("eotp.message.verse_needs_more", Verse.MINIMUM));
        }

        for (String discovery : Discovery.ALL) {
            if (!Cultivation.knows(player, discovery)) continue;
            stack.set(EOTPComponents.TEACHING.get(), discovery);
            Tell.chat(player, Component.translatable("eotp.message.scroll_written",
                Component.translatable(Discovery.translationKey(discovery))));
            return InteractionResult.SUCCESS;
        }

        Tell.overlay(player, "eotp.message.nothing_to_write");
        return InteractionResult.FAIL;
    }
}

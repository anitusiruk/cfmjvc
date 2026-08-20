package com.echoesofthepast.item;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.util.Tell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A fragment of somebody else's understanding. Reading one grants the teaching it carries, which
 * unlocks the corresponding technique or formation.
 *
 * <p>A scroll is always a shortcut and never a requirement: every teaching on it can also be worked
 * out by doing the thing in front of a witness. A blank scroll picks up whichever teaching the reader
 * already knows and does not yet have written down, which is how a player copies notes for a friend.
 */
public class EchoScrollItem extends Item {
    public EchoScrollItem(Properties properties) {
        super(properties);
    }

    public static @Nullable String teachingOf(ItemStack stack) {
        return stack.get(EOTPComponents.TEACHING.get());
    }

    public static ItemStack of(net.minecraft.world.item.Item item, String teaching) {
        ItemStack stack = new ItemStack(item);
        stack.set(EOTPComponents.TEACHING.get(), teaching);
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel)) return InteractionResult.PASS;

        String teaching = teachingOf(stack);
        if (teaching == null) {
            return this.copyKnownTeaching(player, stack);
        }

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

    /** Writing down something you know, so the knowledge can be handed on. */
    private InteractionResult copyKnownTeaching(Player player, ItemStack stack) {
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

package com.echoesofthepast.item;

import com.echoesofthepast.alchemy.PillKind;
import com.echoesofthepast.alchemy.PillQuality;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Realm;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.util.Tell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A finished pill. Its grade lives on the stack, so a cracked batch is visibly worse forever. */
public class PillItem extends Item {
    private final PillKind kind;

    public PillItem(Properties properties, PillKind kind) {
        super(properties);
        this.kind = kind;
    }

    public PillKind kind() {
        return this.kind;
    }

    public static PillQuality qualityOf(ItemStack stack) {
        PillQuality quality = stack.get(EOTPComponents.PILL_QUALITY.get());
        return quality == null ? PillQuality.ORDINARY : quality;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        if (Cultivation.realmOf(player) == Realm.MORTAL) {
            // A mortal body cannot absorb a pill; it just tastes bitter.
            Tell.overlay(player, "eotp.message.body_cannot_absorb");
            return InteractionResult.FAIL;
        }

        PillQuality quality = qualityOf(stack);
        this.kind.consume(serverPlayer, quality);
        Tell.overlay(player, Component.translatable("eotp.message.pill_taken",
            Component.translatable(quality.translationKey()),
            Component.translatable(this.kind.translationKey())));
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}

package com.echoesofthepast.item;

import com.echoesofthepast.block.talisman.PlacedTalismanBlockEntity;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.seal.SealRule;
import com.echoesofthepast.util.Tell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A seal holds a rule. Stamping it onto a pasted talisman changes what that paper is being asked to
 * do, which is how one talisman gets reused for a different job on a formation.
 */
public class CarvedSealItem extends Item {
    public CarvedSealItem(Properties properties) {
        super(properties);
    }

    public static @Nullable SealRule ruleOf(ItemStack stack) {
        return stack.get(EOTPComponents.SEAL_RULE.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        SealRule rule = ruleOf(context.getItemInHand());
        if (rule == null) return InteractionResult.PASS;

        if (level.getBlockEntity(context.getClickedPos()) instanceof PlacedTalismanBlockEntity talisman) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            talisman.configure(talisman.type(), rule, talisman.charges());
            Tell.overlay(player, Component.translatable("eotp.message.seal_stamped",
                Component.translatable(rule.translationKey())));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        SealRule rule = ruleOf(player.getItemInHand(hand));
        Tell.overlay(player, rule == null
            ? Component.translatable("eotp.message.seal_blank")
            : Component.translatable("eotp.message.seal_reads", Component.translatable(rule.translationKey())));
        return InteractionResult.SUCCESS;
    }
}

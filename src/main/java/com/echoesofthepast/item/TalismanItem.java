package com.echoesofthepast.item;

import com.echoesofthepast.block.talisman.PlacedTalismanBlock;
import com.echoesofthepast.block.talisman.PlacedTalismanBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.registry.EOTPBlocks;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.seal.SealRule;
import com.echoesofthepast.talisman.TalismanType;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Paper in the hand. Pressed onto a surface it becomes a pasted talisman waiting for Qi; slapped
 * onto a creature it fires at once, paid for out of the cultivator's own Qi through the hand
 * channel.
 */
public class TalismanItem extends Item {
    private static final int DEFAULT_CHARGES = 8;

    private final TalismanType type;

    public TalismanItem(Properties properties, TalismanType type) {
        super(properties);
        this.type = type;
    }

    public TalismanType type() {
        return this.type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos against = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos target = against.relative(face);
        if (!level.getBlockState(target).canBeReplaced()) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockState placed = EOTPBlocks.PLACED_TALISMAN.get().defaultBlockState()
            .setValue(PlacedTalismanBlock.FACING, face.getOpposite());
        if (!placed.canSurvive(level, target)) return InteractionResult.FAIL;

        level.setBlockAndUpdate(target, placed);
        ItemStack stack = context.getItemInHand();
        if (level.getBlockEntity(target) instanceof PlacedTalismanBlockEntity talisman) {
            talisman.configure(this.type, stampedRule(stack), charges(stack));
        }
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player.level() instanceof ServerLevel level)) return InteractionResult.PASS;

        if (!Cultivation.hasOpen(player, Meridian.HAND)) {
            Tell.overlay(player, "eotp.message.hand_meridian_closed");
            return InteractionResult.FAIL;
        }
        if (!Cultivation.spend(player, this.type.cost() * 2.0F)) {
            return InteractionResult.FAIL;
        }

        this.type.trigger(level, target.blockPosition(), Direction.UP, 1.0F);
        Cultivation.practise(player, Meridian.HAND, 1.0F);
        stack.shrink(1);
        Tell.overlay(player, Component.translatable("eotp.message.talisman_fired",
            Component.translatable(this.type.translationKey())));
        return InteractionResult.SUCCESS;
    }

    private static int charges(ItemStack stack) {
        Integer stored = stack.get(EOTPComponents.CHARGES.get());
        return stored == null ? DEFAULT_CHARGES : stored;
    }

    private static @Nullable SealRule stampedRule(ItemStack stack) {
        return stack.get(EOTPComponents.SEAL_RULE.get());
    }
}

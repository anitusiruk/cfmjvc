package com.echoesofthepast.item;

import com.echoesofthepast.block.formation.FormationInkBlock;
import com.echoesofthepast.ink.InkType;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.registry.EOTPBlocks;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The brush is how formations actually get drawn. Every stroke is a block placed by hand, so laying
 * out a large circuit is a real piece of work and the result is unmistakably yours.
 */
public class SpiritBrushItem extends Item {
    public SpiritBrushItem(Properties properties) {
        super(properties);
    }

    public static void load(ItemStack brush, InkType ink, int strokes) {
        brush.set(EOTPComponents.INK_TYPE.get(), ink);
        brush.set(EOTPComponents.INK_STROKES.get(), strokes);
    }

    public static @Nullable InkType inkOf(ItemStack brush) {
        return brush.get(EOTPComponents.INK_TYPE.get());
    }

    public static int strokesOf(ItemStack brush) {
        Integer strokes = brush.get(EOTPComponents.INK_STROKES.get());
        return strokes == null ? 0 : strokes;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack brush = context.getItemInHand();
        if (player == null) return InteractionResult.PASS;

        InkType ink = inkOf(brush);
        int strokes = strokesOf(brush);
        if (ink == null || strokes <= 0) {
            if (!level.isClientSide()) {
                Tell.overlay(player, "eotp.message.brush_dry");
            }
            return InteractionResult.FAIL;
        }

        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        BlockPos target = context.getClickedPos().above();
        BlockState existing = level.getBlockState(target);

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // Painting over your own line changes its shape rather than wasting ink.
        if (existing.getBlock() instanceof FormationInkBlock) {
            level.setBlockAndUpdate(target, existing.setValue(FormationInkBlock.STROKE,
                existing.getValue(FormationInkBlock.STROKE).next()));
            return InteractionResult.SUCCESS;
        }

        if (!existing.canBeReplaced()) return InteractionResult.PASS;

        Phase phase = ink.phase();
        BlockState line = EOTPBlocks.FORMATION_INK.get().defaultBlockState()
            .setValue(FormationInkBlock.PHASE, phase == null ? Phase.EARTH : phase);
        if (!line.canSurvive(level, target)) return InteractionResult.FAIL;

        level.setBlockAndUpdate(target, line);
        level.playSound(null, target, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 0.5F, 1.1F);

        int remaining = strokes - 1;
        if (remaining <= 0) {
            brush.remove(EOTPComponents.INK_TYPE.get());
            brush.set(EOTPComponents.INK_STROKES.get(), 0);
            Tell.overlay(player, "eotp.message.brush_dry");
        } else {
            brush.set(EOTPComponents.INK_STROKES.get(), remaining);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack brush = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        InkType ink = inkOf(brush);
        if (ink == null) {
            Tell.overlay(player, "eotp.message.brush_dry");
        } else {
            Tell.overlay(player, Component.translatable("eotp.message.brush_state",
                Component.translatable(ink.translationKey()), strokesOf(brush)));
        }
        return InteractionResult.SUCCESS;
    }
}

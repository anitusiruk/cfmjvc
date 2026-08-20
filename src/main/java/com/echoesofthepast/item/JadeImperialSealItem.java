package com.echoesofthepast.item;

import com.echoesofthepast.channel.SealChannels;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Authorisation, not addressing. Stamping several containers with the same seal face tells spirits
 * and cranes that they belong to one household, and that is the whole of the routing system - no
 * numbered channels, no frequency dials.
 *
 * <p>The face of the seal is a short word. Sneaking with it in hand turns to the next face.
 */
public class JadeImperialSealItem extends Item {
    /** The faces a seal can be turned to. Adding more is a matter of adding words. */
    private static final String[] FACES = {
        "hall", "kitchen", "store", "kiln", "garden", "study", "gate", "workshop"
    };

    public JadeImperialSealItem(Properties properties) {
        super(properties);
    }

    public static String faceOf(ItemStack stack) {
        String channel = stack.get(EOTPComponents.SEAL_CHANNEL.get());
        return channel == null ? FACES[0] : channel;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        String current = faceOf(stack);

        if (player.isShiftKeyDown()) {
            int index = 0;
            for (int i = 0; i < FACES.length; i++) {
                if (FACES[i].equals(current)) {
                    index = i;
                    break;
                }
            }
            String next = FACES[(index + 1) % FACES.length];
            stack.set(EOTPComponents.SEAL_CHANNEL.get(), next);
            Tell.overlay(player, Component.translatable("eotp.message.seal_face_turned",
                Component.translatable("eotp.channel." + next)));
        } else {
            Tell.overlay(player, Component.translatable("eotp.message.seal_face_reads",
                Component.translatable("eotp.channel." + current)));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof Container)) {
            Tell.overlay(player, "eotp.message.cannot_stamp");
            return InteractionResult.FAIL;
        }

        String face = faceOf(context.getItemInHand());
        String existing = SealChannels.at(serverLevel, pos);
        if (face.equals(existing)) {
            SealChannels.clear(serverLevel, pos);
            Tell.overlay(player, "eotp.message.stamp_removed");
        } else {
            SealChannels.stamp(serverLevel, pos, face);
            Tell.overlay(player, Component.translatable("eotp.message.stamped",
                Component.translatable("eotp.channel." + face)));
        }
        level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.8F, 0.8F);
        return InteractionResult.SUCCESS;
    }
}

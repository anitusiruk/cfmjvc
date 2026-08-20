package com.echoesofthepast.item;

import com.echoesofthepast.channel.SealChannels;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.entity.PaperCraneEntity;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.registry.EOTPEntities;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * A folded crane, waiting to be sent. Stamp it with a seal face, then use it on a container: it
 * takes one stack out and flies it to the nearest place stamped with the same face.
 *
 * <p>Sending things this way is slow and completely visible. That is the trade: a crane costs paper
 * and patience, and in exchange the automation looks like magic rather than like plumbing.
 */
public class PaperCraneItem extends Item {
    private static final float COST = 5.0F;

    public PaperCraneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        ItemStack crane = context.getItemInHand();
        String destination = crane.get(EOTPComponents.SEAL_CHANNEL.get());
        if (destination == null) {
            Tell.overlay(player, "eotp.message.crane_unaddressed");
            return InteractionResult.FAIL;
        }

        BlockPos source = context.getClickedPos();
        if (!(level.getBlockEntity(source) instanceof Container container)) {
            Tell.overlay(player, "eotp.message.crane_needs_container");
            return InteractionResult.FAIL;
        }

        BlockPos target = SealChannels.findNearest(serverLevel, destination, source, 64);
        if (target == null || target.equals(source)) {
            Tell.overlay(player, Component.translatable("eotp.message.crane_no_destination",
                Component.translatable("eotp.channel." + destination)));
            return InteractionResult.FAIL;
        }

        ItemStack cargo = takeOneStack(container);
        if (cargo.isEmpty()) {
            Tell.overlay(player, "eotp.message.crane_nothing_to_carry");
            return InteractionResult.FAIL;
        }
        if (!Cultivation.spend(player, COST)) {
            container.setItem(0, cargo);
            return InteractionResult.FAIL;
        }

        PaperCraneEntity bird = EOTPEntities.PAPER_CRANE.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (bird == null) return InteractionResult.FAIL;
        bird.snapTo(source.getX() + 0.5, source.getY() + 1.2, source.getZ() + 0.5);
        bird.load(cargo, destination);
        serverLevel.addFreshEntity(bird);

        crane.shrink(1);
        Tell.overlay(player, Component.translatable("eotp.message.crane_sent",
            cargo.getHoverName(), Component.translatable("eotp.channel." + destination)));
        return InteractionResult.SUCCESS;
    }

    /** Takes the first non-empty stack out of a container; a crane carries one load. */
    private static ItemStack takeOneStack(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;
            return container.removeItemNoUpdate(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack crane = player.getItemInHand(hand);

        // A crane is addressed by holding it together with an imperial seal.
        ItemStack other = player.getItemInHand(hand == net.minecraft.world.InteractionHand.MAIN_HAND
            ? net.minecraft.world.InteractionHand.OFF_HAND
            : net.minecraft.world.InteractionHand.MAIN_HAND);
        if (other.getItem() instanceof JadeImperialSealItem) {
            String face = JadeImperialSealItem.faceOf(other);
            crane.set(EOTPComponents.SEAL_CHANNEL.get(), face);
            Tell.overlay(player, Component.translatable("eotp.message.crane_addressed",
                Component.translatable("eotp.channel." + face)));
            return InteractionResult.SUCCESS;
        }

        String destination = crane.get(EOTPComponents.SEAL_CHANNEL.get());
        Tell.overlay(player, destination == null
            ? Component.translatable("eotp.message.crane_unaddressed")
            : Component.translatable("eotp.message.crane_addressed",
                Component.translatable("eotp.channel." + destination)));
        return InteractionResult.SUCCESS;
    }
}

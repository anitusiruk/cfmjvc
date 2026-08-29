package com.echoesofthepast.event;

import com.echoesofthepast.block.alchemy.DingCauldronBlockEntity;
import com.echoesofthepast.block.craft.HerbDryingRackBlockEntity;
import com.echoesofthepast.block.craft.IncenseCenserBlockEntity;
import com.echoesofthepast.block.echo.AncestralTabletBlockEntity;
import com.echoesofthepast.block.qi.BaguaDistributorBlockEntity;
import com.echoesofthepast.block.qi.BronzeSpiritBellBlockEntity;
import com.echoesofthepast.block.qi.ConversionWheelBlockEntity;
import com.echoesofthepast.channel.SealChannels;
import com.echoesofthepast.cultivation.Tendencies;
import com.echoesofthepast.cultivation.Tendency;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;

/**
 * Two jobs, both about memory: writing down what happens in the world so an Echo Mirror has
 * something to show, and letting ancestral tablets watch a player work.
 */
public final class EchoEvents {
    private EchoEvents() {}

    public static void register() {
        BlockEvent.BreakEvent.BUS.addListener(EchoEvents::onBreak);
        BlockEvent.EntityPlaceEvent.BUS.addListener(EchoEvents::onPlace);
        LivingDeathEvent.BUS.addListener(EchoEvents::onDeath);
        PlayerInteractEvent.RightClickBlock.BUS.addListener(EchoEvents::onRightClickBlock);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        EchoLog.record(level, pos, EchoLog.Kind.BLOCK_BROKEN,
            Component.translatable("eotp.echo.broken",
                event.getState().getBlock().getName(), event.getPlayer().getName()));
        // A stamped container that gets broken should stop attracting cranes.
        SealChannels.clear(level, pos);

        if (event.getPlayer() instanceof ServerPlayer player) {
            Tendencies.note(player, Tendency.CUTTING, 0.05F);
        }
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        EchoLog.record(level, event.getPos(), EchoLog.Kind.BLOCK_PLACED,
            Component.translatable("eotp.echo.placed",
                event.getPlacedBlock().getBlock().getName(), player.getName()));

        if (player instanceof ServerPlayer serverPlayer) {
            Tendencies.note(serverPlayer, Tendency.CREATING, 0.05F);
        }
    }

    private static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        EchoLog.record(level, event.getEntity().blockPosition(), EchoLog.Kind.DEATH,
            Component.translatable("eotp.echo.died", event.getEntity().getName()));

        // Somebody surviving what killed something else is enduring; the killer is cutting.
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            Tendencies.note(killer, Tendency.CUTTING, 0.2F);
        }
    }

    /**
     * Every interaction with a device that can be imprinted is offered to the tablets that can see
     * it. The gesture is inferred from what the player did, which is why the tablet's vocabulary is
     * small: it only knows things that are obvious from the outside.
     */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ImprintTarget)) {
            // Bells and rotating devices are worth learning too, even though they hold no items.
            if (!(blockEntity instanceof BronzeSpiritBellBlockEntity
                || blockEntity instanceof BaguaDistributorBlockEntity
                || blockEntity instanceof ConversionWheelBlockEntity)) {
                return;
            }
        }

        ImprintAction action = classify(blockEntity, event.getItemStack());
        if (action == null) return;

        for (AncestralTabletBlockEntity tablet : AncestralTabletBlockEntity.watching(level, pos)) {
            tablet.observe(pos, action);
        }

        // Working a device by hand is the world watching you tend, transform or command something.
        if (player instanceof ServerPlayer serverPlayer) {
            Tendency tendency = switch (action) {
                case FEED, STIR -> Tendency.TENDING;
                case TURN -> Tendency.TRANSFORMING;
                case STRIKE -> Tendency.COMMANDING;
                case HARVEST -> Tendency.CREATING;
            };
            Tendencies.note(serverPlayer, tendency, 0.15F);
        }
    }

    private static ImprintAction classify(BlockEntity device, ItemStack held) {
        if (device instanceof BronzeSpiritBellBlockEntity) {
            return ImprintAction.STRIKE;
        }
        if (device instanceof BaguaDistributorBlockEntity || device instanceof ConversionWheelBlockEntity) {
            return ImprintAction.TURN;
        }
        if (device instanceof DingCauldronBlockEntity || device instanceof IncenseCenserBlockEntity) {
            return held.isEmpty() ? ImprintAction.STIR : ImprintAction.FEED;
        }
        if (device instanceof HerbDryingRackBlockEntity) {
            return held.isEmpty() ? ImprintAction.HARVEST : ImprintAction.FEED;
        }
        return held.isEmpty() ? ImprintAction.STIR : ImprintAction.FEED;
    }
}

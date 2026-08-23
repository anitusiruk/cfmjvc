package com.echoesofthepast.item;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.cultivation.SpiritProjection;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.util.Tell;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Holding the mirror up shows what has recently happened here: blocks broken, things dropped,
 * creatures that died, rituals performed. The scenes are ghostly repetitions drawn where they
 * happened, and the mirror says how long ago each one was.
 *
 * <p>This is the mod's central verb - look at the past to work out the present - so it is cheap to
 * use and needs no cultivation beyond an open crown channel.
 */
public class EchoMirrorItem extends Item {
    private static final int RADIUS = 12;
    private static final int MOST_SHOWN = 6;
    private static final float COST = 4.0F;

    public EchoMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        // At Nascent Spirit the mirror stops showing somebody else's past and becomes the polished
        // threshold through which the cultivator separates their own reflection from the body.
        if (player.isShiftKeyDown()) {
            return SpiritProjection.begin(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        if (!Cultivation.hasOpen(player, Meridian.CROWN)) {
            Tell.overlay(player, "eotp.message.crown_meridian_closed");
            return InteractionResult.FAIL;
        }
        if (!Cultivation.spend(player, COST)) return InteractionResult.FAIL;

        List<EchoLog.Echo> echoes = EchoLog.near(serverLevel, player.blockPosition(), RADIUS, MOST_SHOWN);
        if (echoes.isEmpty()) {
            Tell.overlay(player, "eotp.message.nothing_remembered");
            return InteractionResult.SUCCESS;
        }

        long now = serverLevel.getGameTime();
        for (EchoLog.Echo echo : echoes) {
            Vec3 at = Vec3.atCenterOf(echo.pos()).add(0.0, 0.4, 0.0);
            QiVisuals.echo(serverLevel, at, 8);
            QiVisuals.line(serverLevel, player.getEyePosition(), at, 0x8E7FC4, 2);
            Tell.chat(player, Component.translatable("eotp.message.echo_line",
                Component.translatable(echo.kind().translationKey()),
                echo.description(),
                secondsAgo(now, echo.tick())));
        }

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.7F);
        Cultivation.practise(serverPlayer, Meridian.CROWN, 1.0F);
        Cultivation.teach(serverPlayer, Discovery.QI_SENSE);
        return InteractionResult.SUCCESS;
    }

    private static int secondsAgo(long now, long then) {
        return (int) Math.max(0L, (now - then) / 20L);
    }
}

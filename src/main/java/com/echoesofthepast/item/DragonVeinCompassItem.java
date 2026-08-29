package com.echoesofthepast.item;

import com.echoesofthepast.block.formation.FormationCoreBlockEntity;
import com.echoesofthepast.channel.SealChannels;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.cultivation.Tendencies;
import com.echoesofthepast.cultivation.Tendency;
import com.echoesofthepast.formation.FormationSurvey;
import com.echoesofthepast.formation.FormationType;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The inspection tool. Everything the mod hides is legible through this: the dragon vein under your
 * feet and which way it runs, how full a device is and what it is holding, what a circuit has been
 * identified as, and what mark a container is stamped with.
 *
 * <p>It reports in particles first and words second, which keeps the readout in the world.
 */
public class DragonVeinCompassItem extends Item {
    /** How far the compass paints the vein lines. */
    private static final int VEIN_RADIUS = 7;

    public DragonVeinCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockPos standing = player.blockPosition();
        float strength = DragonVeins.strength(level, standing);
        this.paintVeins(serverLevel, standing);

        if (strength <= 0.05F) {
            Tell.overlay(player, "eotp.message.no_vein");
        } else {
            Vec3 flow = DragonVeins.flow(level, standing);
            Tell.chat(player, Component.translatable("eotp.message.vein_reading",
                Math.round(strength * 100.0F),
                Component.translatable(DragonVeins.phaseOf(level, standing).translationKey()),
                describeDirection(flow),
                DragonVeins.isIntersection(level, standing) ? 1 : 0));
            Cultivation.teach(serverPlayer, Discovery.QI_SENSE);
            // Watching Qi phenomena is exactly what the crown channel is for.
            Cultivation.practise(serverPlayer, Meridian.CROWN, 0.5F);
            Tendencies.note(serverPlayer, Tendency.OBSERVING, 0.1F);
        }

        Tell.chat(player, Cultivation.describe(serverPlayer));
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
        boolean reported = false;

        QiNode node = QiNet.nodeAt(level, pos);
        if (node != null) {
            QiStorage storage = node.qiStorage(context.getClickedFace());
            if (storage != null) {
                Tell.chat(player, Component.translatable("eotp.message.device_reading", storage.describe()));
                QiVisuals.resting(serverLevel, pos, storage);
                reported = true;
            }
        }

        if (level.getBlockEntity(pos) instanceof FormationCoreBlockEntity core) {
            // Inspecting written formations is crown work too.
            Cultivation.practise(player, Meridian.CROWN, 0.8F);
            if (player instanceof ServerPlayer serverPlayer) {
                Tendencies.note(serverPlayer, Tendency.OBSERVING, 0.15F);
            }

            FormationType type = core.type();
            FormationSurvey survey = core.survey();
            Tell.chat(player, type == null
                ? Component.translatable("eotp.message.formation_unread")
                : Component.translatable("eotp.message.formation_reading",
                    Component.translatable(type.translationKey()),
                    Math.round(core.strength() * 100.0F),
                    survey == null ? 0 : survey.size()));
            reported = true;
        }

        String channel = SealChannels.at(serverLevel, pos);
        if (channel != null) {
            Tell.chat(player, Component.translatable("eotp.message.stamped_with", channel));
            reported = true;
        }

        if (!reported) {
            Tell.overlay(player, "eotp.message.nothing_to_read");
        }
        return InteractionResult.SUCCESS;
    }

    /** Draws the current running through the ground as short flowing lines. */
    private void paintVeins(ServerLevel level, BlockPos center) {
        for (int dx = -VEIN_RADIUS; dx <= VEIN_RADIUS; dx += 2) {
            for (int dz = -VEIN_RADIUS; dz <= VEIN_RADIUS; dz += 2) {
                BlockPos sample = center.offset(dx, 0, dz);
                float strength = DragonVeins.strength(level, sample);
                if (strength < 0.25F) continue;
                Vec3 flow = DragonVeins.flow(level, sample).scale(0.9);
                Vec3 from = Vec3.atCenterOf(sample).add(0.0, 0.1, 0.0);
                QiVisuals.line(level, from, from.add(flow), DragonVeins.phaseOf(level, sample).color(), 2);
            }
        }
    }

    private static @Nullable Component describeDirection(Vec3 flow) {
        String name = Math.abs(flow.x) > Math.abs(flow.z)
            ? (flow.x > 0 ? "east" : "west")
            : (flow.z > 0 ? "south" : "north");
        return Component.translatable("eotp.direction." + name);
    }
}

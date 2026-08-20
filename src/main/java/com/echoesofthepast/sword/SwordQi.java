package com.echoesofthepast.sword;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.item.FlyingSwordItem;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.util.Tell;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A crescent of Qi thrown off a blade. It is not an enchantment and not a projectile entity: it is a
 * short arc in front of the wielder, resolved immediately.
 *
 * <p>The damage is modest. What makes it worth the Qi is everything else it does: it clears
 * vegetation, wakes formation lines it passes over, and strips lingering magical effects off what it
 * cuts.
 */
public final class SwordQi {
    /** How far the arc reaches. */
    private static final double REACH = 5.0;
    private static final float COST = 8.0F;
    private static final float BASE_DAMAGE = 5.0F;

    private SwordQi() {}

    /**
     * @return true if the technique fired.
     */
    public static boolean release(ServerLevel level, ServerPlayer player, ItemStack sword) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return false;
        if (!cultivator.isUsable(Meridian.HAND, (int) level.getGameTime())) {
            Tell.overlay(player, "eotp.message.hand_meridian_closed");
            return false;
        }
        if (!Cultivation.spend(player, COST)) return false;

        SwordIntentData intent = FlyingSwordItem.intentOf(sword);
        SwordIntent dominant = intent.dominant();
        float channel = cultivator.channelStrength(Meridian.HAND, (int) level.getGameTime());
        float damage = BASE_DAMAGE * channel * (1.0F + intent.mastery() * 0.6F);

        // Falling-star intent turns height into force, which rewards attacking from above.
        if (dominant == SwordIntent.FALLING_STAR && !player.onGround()) {
            damage *= 1.5F;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 tip = eye.add(look.scale(REACH));

        int cut = 0;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
            new AABB(eye, tip).inflate(1.6), candidate -> candidate != player && candidate.isAlive())) {
            victim.hurtServer(level, level.damageSources().playerAttack(player), damage);
            // Severing effects is the arc's real trick: it cuts what is clinging to a thing, too.
            for (MobEffectInstance effect : List.copyOf(victim.getActiveEffects())) {
                if (effect.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL) continue;
                victim.removeEffect(effect.getEffect());
            }
            cut++;
        }

        int mown = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(eye.subtract(REACH, 2.0, REACH)),
            BlockPos.containing(eye.add(REACH, 2.0, REACH)))) {
            if (Vec3.atCenterOf(pos).distanceToSqr(tip) > REACH * REACH) continue;
            var state = level.getBlockState(pos);
            if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN) || state.is(Blocks.VINE)) {
                level.destroyBlock(pos, true);
                mown++;
            }

            // Formation lines and threads the arc sweeps over take the leftover Qi as a pulse, which
            // is how a swordsman triggers their own defences without touching a lever.
            if (QiNet.receiverAt(level, pos) != null) {
                QiNet.sendPulse(level, pos.above(), Direction.DOWN, QiPulse.create(6.0F, PhaseBlend.of(Phase.METAL)));
            }
        }

        QiVisuals.line(level, eye.add(look.scale(1.0)).add(-look.z * 1.4, 0.0, look.x * 1.4),
            eye.add(look.scale(1.0)).add(look.z * 1.4, 0.0, -look.x * 1.4), 0xE8F0FF, 6);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.9F);

        Cultivation.practise(player, Meridian.HAND, 1.0F);
        Cultivation.teach(player, Discovery.SWORD_QI);
        if (cut > 0) {
            FlyingSwordItem.addIntent(sword, cut > 1 ? SwordIntent.FLOWING_RIVER : SwordIntent.STILL_WATER, cut);
        }
        if (mown > 6) {
            EchoLog.record(level, player.blockPosition(), EchoLog.Kind.TECHNIQUE,
                Component.translatable("eotp.echo.sword_qi", player.getName()));
        }
        return true;
    }
}

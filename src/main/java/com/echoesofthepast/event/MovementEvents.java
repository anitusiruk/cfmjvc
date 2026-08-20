package com.echoesofthepast.event;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.CultivationStore;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.item.CloudstepShoesItem;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * Cloudstepping.
 *
 * <p>A dedicated keybind would be nicer, but this deliberately stays server-side: the step fires at
 * the top of a jump and again whenever the cultivator is falling fast while sprinting, which is
 * exactly the moment a wuxia character would kick off the air. Each step costs Qi, cancels the fall,
 * and leaves a cloud under the foot.
 */
public final class MovementEvents {
    /** Upward push a step gives. */
    private static final double STEP_LIFT = 0.42;
    /** Forward push, so a step carries you somewhere rather than just up. */
    private static final double STEP_PUSH = 0.28;
    private static final float STEP_COST = 6.0F;
    /** Fall speed past which a sprinting cultivator will kick off the air again. */
    private static final double FALL_TRIGGER = -0.55;

    private MovementEvents() {}

    public static void register() {
        LivingFallEvent.BUS.addListener(MovementEvents::onFall);
    }

    /** Called from the player tick so it can watch the whole arc of a jump. */
    public static void tick(ServerPlayer player, LogicalSide side) {
        if (side != LogicalSide.SERVER) return;
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;

        if (player.onGround() || player.isInWater() || player.getAbilities().flying) {
            if (cultivator.cloudstepsUsed() != 0) {
                cultivator.setCloudstepsUsed(0);
            }
            return;
        }

        int allowance = allowanceFor(player, cultivator);
        if (allowance <= 0 || cultivator.cloudstepsUsed() >= allowance) return;

        Vec3 motion = player.getDeltaMovement();
        boolean atApex = motion.y <= 0.0 && motion.y > -0.08 && cultivator.cloudstepsUsed() == 0;
        boolean fallingFast = motion.y < FALL_TRIGGER && player.isSprinting();
        if (!atApex && !fallingFast) return;

        if (!cultivator.spendQi(STEP_COST)) return;
        CultivationStore.touch(player);

        Vec3 facing = player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
        player.setDeltaMovement(motion.x + facing.x * STEP_PUSH, STEP_LIFT, motion.z + facing.z * STEP_PUSH);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        cultivator.setCloudstepsUsed(cultivator.cloudstepsUsed() + 1);

        ServerLevel level = player.level();
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 8, 0.25, 0.02, 0.25, 0.01);
        QiVisuals.ring(level, player.position(), 0.5, PhaseBlend.of(Phase.WATER).color(), 8);

        Cultivation.practise(player, Meridian.FOOT, 0.8F);
        Cultivation.teach(player, Discovery.CLOUDSTEP);
    }

    /**
     * How many steps this cultivator gets: shoes are the usual source, a pill grants some without
     * them, and an open foot channel adds one on top. There is never enough for real flight.
     */
    private static int allowanceFor(Player player, Cultivator cultivator) {
        int allowance = 0;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof CloudstepShoesItem shoes) {
            allowance += shoes.steps();
        }
        if (player.hasEffect(EOTPMobEffects.holder(EOTPMobEffects.CLOUDSTEP))) {
            allowance += 1;
        }
        if (allowance > 0 && cultivator.isUsable(Meridian.FOOT, (int) player.level().getGameTime())) {
            allowance += 1;
        }
        return Math.min(3, allowance);
    }

    /** A cultivator who has been stepping on air lands lightly. */
    private static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;
        if (cultivator.cloudstepsUsed() > 0) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.35F);
            Cultivation.practise(player, Meridian.FOOT, 0.4F);
        }
        cultivator.setCloudstepsUsed(0);
    }
}

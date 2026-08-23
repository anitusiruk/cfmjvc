package com.echoesofthepast.event;

import com.echoesofthepast.cultivation.BreakthroughRitual;
import com.echoesofthepast.cultivation.BreathInitiation;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.CultivationStore;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.cultivation.Realm;
import com.echoesofthepast.cultivation.SpiritProjection;
import com.echoesofthepast.cultivation.Tendencies;
import com.echoesofthepast.cultivation.Tendency;
import com.echoesofthepast.cultivation.Tribulation;
import com.echoesofthepast.cultivation.Witnesses;
import com.echoesofthepast.fluid.SpiritSpringEffects;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * The slow background of cultivation: Qi recovering, held Qi seeping away, tolerance to pills
 * fading, and the body learning from what happens to it.
 */
public final class CultivationEvents {
    /** How long a golden core stays rattled after its owner dies. */
    private static final int DEATH_INSTABILITY = 6000;

    private CultivationEvents() {}

    public static void register() {
        TickEvent.PlayerTickEvent.Post.BUS.addListener(CultivationEvents::onPlayerTick);
        LivingDeathEvent.BUS.addListener(CultivationEvents::onDeath);
        LivingDamageEvent.BUS.addListener(CultivationEvents::onDamage);
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (event.side() != LogicalSide.SERVER) return;
        if (!(event.player() instanceof ServerPlayer player)) return;

        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;

        long time = player.level().getGameTime();
        BlockPos pos = player.blockPosition();

        if (cultivator.realm() == Realm.MORTAL) {
            cultivateFirstBreath(player, cultivator, time, pos);
            return;
        }

        // Things that have to watch every tick: footwork and lightning.
        MovementEvents.tick(player, event.side());
        Tribulation.tick(player);

        if (time % 20L == 0L) {
            boolean still = player.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4;
            SpiritProjection.tick(player);
            if (cultivator.readyToBreakThrough() && still && !SpiritProjection.isProjecting(player)) {
                BreakthroughRitual.tick(player, cultivator);
            }

            boolean inSpiritSpring = SpiritSpringEffects.nearby(player.level(), pos, 1);
            float ambient = DragonVeins.ambientQi(player.level(), pos) + (inSpiritSpring ? 0.55F : 0.0F);
            // Sitting still on a vein is the cheapest cultivation there is, and it looks the part.
            boolean resting = still && !player.isSprinting();
            cultivator.regenerateQi(ambient, resting);
            cultivator.seepQi();

            if (resting) {
                // Sitting still is not experience any more. It is how a cultivator holds a
                // principle true, which is what Verses and Discord repair are watching for.
                Tendencies.note(player, Tendency.STILLNESS, 0.05F + ambient * 0.15F);
                if (DragonVeins.isIntersection(player.level(), pos) || inSpiritSpring) {
                    Tendencies.note(player, Tendency.OBSERVING, 0.05F);
                }
            }

            if (cultivator.coreInstability() > 0) {
                cultivator.settleCore(20);
            }
            CultivationStore.touch(player);
        }

        // Tolerance to pills fades over roughly quarter of an hour of play.
        if (time % 200L == 0L) {
            cultivator.decayTolerance(0.05F);
        }

        // An open crown meridian means the world stays legible without a tool in hand.
        if (time % 100L == 0L && cultivator.isUsable(Meridian.CROWN, (int) time)) {
            player.addEffect(EOTPMobEffects.quiet(EOTPMobEffects.SPIRIT_SIGHT, 140, 0));
        }

        // Holding a settled mind is heart work, and so is recovering from a rough patch.
        if (time % 60L == 0L) {
            if (player.hasEffect(EOTPMobEffects.holder(EOTPMobEffects.CLEAR_HEART))) {
                Cultivation.practise(player, Meridian.HEART, 0.4F);
                Tendencies.note(player, Tendency.PROTECTING, 0.03F);
            }
            if (cultivator.coreInstability() > 0 || cultivator.path().hasDiscord()) {
                Cultivation.practise(player, Meridian.HEART, 0.3F);
                Tendencies.note(player, Tendency.ENDURING, 0.05F);
            }
        }

        // Covering ground is what makes a wandering cultivator, and it is foot work besides.
        if (time % 100L == 0L && player.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
            Tendencies.note(player, Tendency.WANDERING, 0.04F);
        }

        if (player.onGround()) {
            cultivator.setCloudstepsUsed(0);
        }
    }

    /**
     * A mortal's whole loop. There is nothing to grind: they are working toward the Three Witnesses,
     * and only once all three are given does the First Breath Ritual become possible.
     */
    private static void cultivateFirstBreath(ServerPlayer player, Cultivator cultivator, long time, BlockPos pos) {
        if (time % 20L == 0L) {
            Witnesses.tickEarth(player, cultivator);
            Witnesses.checkSelf(player, cultivator);

            boolean still = player.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && !player.isSprinting();
            if (still && DragonVeins.strength(player.level(), pos) >= 0.15F && time % 40L == 0L) {
                QiVisuals.ring(
                    player.level(),
                    player.position().add(0.0, 0.05, 0.0),
                    0.45,
                    DragonVeins.phaseOf(player.level(), pos).color(),
                    8
                );
            }
        }

        if (cultivator.readyToBreakThrough()) {
            BreathInitiation.tick(player, cultivator);
        }
        CultivationStore.touch(player);
    }

    private static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;

        // A core is not lost on death, only shaken loose.
        if (cultivator.coreFormed()) {
            cultivator.destabiliseCore(DEATH_INSTABILITY);
            Tell.chat(player, Component.translatable("eotp.message.core_unstable"));
        }
        cultivator.spendQi(cultivator.qi());
        CultivationStore.touch(player);
    }

    private static void onDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null || cultivator.realm() == Realm.MORTAL) return;

        // Taking a hit and staying upright is how the heart channel opens.
        Cultivation.practise(player, Meridian.HEART, Math.min(4.0F, event.getAmount() * 0.25F));
        Tendencies.note(player, Tendency.ENDURING, Math.min(0.3F, event.getAmount() * 0.02F));

        // Qi in the body cushions the blow a little, spending itself to do it.
        if (cultivator.isUsable(Meridian.HEART, (int) player.level().getGameTime()) && cultivator.qi() > 4.0F) {
            float absorbed = Math.min(event.getAmount() * 0.2F, cultivator.qi() * 0.1F);
            if (absorbed > 0.1F && cultivator.spendQi(absorbed * 3.0F)) {
                event.setAmount(event.getAmount() - absorbed);
                CultivationStore.touch(player);
            }
        }
    }

    /** Called by pills, tea and rituals that hand out Qi of a particular character. */
    public static void feedQi(Player player, float amount, PhaseBlend blend) {
        Cultivation.grantQi(player, amount, blend);
    }
}

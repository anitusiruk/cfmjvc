package com.echoesofthepast.event;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.entity.FlyingSwordEntity;
import com.echoesofthepast.item.ArmorSets;
import com.echoesofthepast.item.FlyingSwordItem;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.sword.SwordIntent;
import com.echoesofthepast.util.Tell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/**
 * Where a sword learns what it is for.
 *
 * <p>Intent is only ever earned from real fights, and the tally is written onto the blade rather than
 * onto the player, so a sword can be handed on and will still remember.
 */
public final class CombatEvents {
    /** Qi spent by the set effect to knock a projectile out of the air. */
    private static final float INTERCEPT_COST = 5.0F;

    private CombatEvents() {}

    public static void register() {
        AttackEntityEvent.BUS.addListener(CombatEvents::onAttack);
        LivingDamageEvent.BUS.addListener(CombatEvents::onDamage);
    }

    private static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof FlyingSwordItem)) return;
        if (!FlyingSwordItem.isBoundTo(held, player)) return;

        Entity target = event.getTarget();
        SwordIntent earned = classify(serverPlayer, target);
        FlyingSwordItem.addIntent(held, earned, 1);
        Cultivation.practise(player, Meridian.HAND, 0.3F);

        if (player.level() instanceof ServerLevel level) {
            QiVisuals.line(level, player.getEyePosition(), target.position().add(0.0, target.getBbHeight() * 0.5, 0.0),
                PhaseBlend.of(Phase.METAL).color(), 2);
        }
    }

    /**
     * The classification is the mechanic: a player who fights from the air will end up with a
     * falling-star blade whether they meant to or not.
     */
    private static SwordIntent classify(ServerPlayer player, Entity target) {
        if (target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
            return SwordIntent.PURIFYING;
        }
        if (!player.onGround() && player.getDeltaMovement().y < -0.1) {
            return SwordIntent.FALLING_STAR;
        }
        int crowd = player.level().getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(5.0), candidate -> candidate != player && candidate.isAlive()).size();
        if (crowd >= 3) {
            return SwordIntent.FLOWING_RIVER;
        }
        if (player.isShiftKeyDown()) {
            return SwordIntent.MOUNTAIN;
        }
        return SwordIntent.STILL_WATER;
    }

    private static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        ItemStack held = player.getMainHandItem();
        boolean boundSword = held.getItem() instanceof FlyingSwordItem && FlyingSwordItem.isBoundTo(held, player);

        // Full set: the blade steps in front of one projectile, spending its own Qi to do it.
        if (boundSword && ArmorSets.wearsSwordSet(player) && !event.getSource().isDirect()) {
            if (Cultivation.spend(player, INTERCEPT_COST)) {
                event.setAmount(0.0F);
                FlyingSwordEntity sword = FlyingSwordEntity.of(level, player);
                if (sword != null) sword.recall();
                QiVisuals.ring(level, player.position().add(0.0, 1.0, 0.0), 1.2, PhaseBlend.of(Phase.METAL).color(), 14);
                Tell.overlay(player, "eotp.message.sword_intercepts");
                return;
            }
        }

        // Mountain intent steadies its wielder rather than adding damage.
        if (boundSword && FlyingSwordItem.intentOf(held).dominant() == SwordIntent.MOUNTAIN) {
            event.setAmount(event.getAmount() * 0.85F);
            FlyingSwordItem.addIntent(held, SwordIntent.MOUNTAIN, 1);
        }

        // Alchemist robes are made for a workshop, not a battlefield.
        if (ArmorSets.wearsAlchemistSet(player)) {
            var source = event.getSource();
            if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)) {
                event.setAmount(event.getAmount() * 0.6F);
            }
        }
    }
}

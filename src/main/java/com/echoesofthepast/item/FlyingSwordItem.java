package com.echoesofthepast.item;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.cultivation.Realm;
import com.echoesofthepast.entity.FlyingSwordEntity;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.registry.EOTPEntities;
import com.echoesofthepast.sword.SwordIntent;
import com.echoesofthepast.sword.SwordIntentData;
import com.echoesofthepast.sword.SwordQi;
import com.echoesofthepast.util.Tell;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A good sword that becomes something else once it is bound.
 *
 * <ul>
 *   <li>sneak-right-click on nothing binds the blade to you;</li>
 *   <li>right-click sends it at whatever you are looking at, or recalls it;</li>
 *   <li>right-click with nothing in front of you and the hand channel open releases sword Qi.</li>
 * </ul>
 *
 * <p>What the blade is <em>good</em> at is not chosen here - see {@link SwordIntent}.
 */
public class FlyingSwordItem extends Item {
    private static final float BASE_DAMAGE = 7.0F;
    private static final float SEND_COST = 12.0F;

    public FlyingSwordItem(Properties properties) {
        super(properties);
    }

    public static @Nullable UUID ownerOf(ItemStack stack) {
        return stack.get(EOTPComponents.BOUND_OWNER.get());
    }

    public static boolean isBoundTo(ItemStack stack, Player player) {
        UUID owner = ownerOf(stack);
        return owner != null && owner.equals(player.getUUID());
    }

    public static SwordIntentData intentOf(ItemStack stack) {
        SwordIntentData data = stack.get(EOTPComponents.SWORD_INTENT.get());
        return data == null ? SwordIntentData.EMPTY : data;
    }

    public static void addIntent(ItemStack stack, SwordIntent intent, int amount) {
        stack.set(EOTPComponents.SWORD_INTENT.get(), intentOf(stack).plus(intent, amount));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return this.bind(serverPlayer, stack);
        }
        if (!isBoundTo(stack, player)) {
            Tell.overlay(player, "eotp.message.sword_not_bound");
            return InteractionResult.FAIL;
        }

        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return InteractionResult.PASS;

        LivingEntity target = this.lookedAtCreature(serverLevel, player);
        if (target != null) {
            return this.send(serverLevel, serverPlayer, stack, cultivator, target);
        }
        return SwordQi.release(serverLevel, serverPlayer, stack) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private InteractionResult bind(ServerPlayer player, ItemStack stack) {
        if (Cultivation.realmOf(player) == Realm.MORTAL) {
            Tell.overlay(player, "eotp.message.no_qi_to_bind_with");
            return InteractionResult.FAIL;
        }
        UUID existing = ownerOf(stack);
        if (existing != null && existing.equals(player.getUUID())) {
            SwordIntentData intent = intentOf(stack);
            SwordIntent dominant = intent.dominant();
            Tell.chat(player, dominant == null
                ? Component.translatable("eotp.message.sword_undecided", intent.total())
                : Component.translatable("eotp.message.sword_intent",
                    Component.translatable(dominant.translationKey()),
                    Math.round(intent.mastery() * 100.0F)));
            return InteractionResult.SUCCESS;
        }
        stack.set(EOTPComponents.BOUND_OWNER.get(), player.getUUID());
        QiVisuals.bloom(player.level(), player.position().add(0.0, 1.0, 0.0), PhaseBlend.of(Phase.METAL));
        Tell.chat(player, Component.translatable("eotp.message.sword_bound"));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult send(ServerLevel level, ServerPlayer player, ItemStack stack, Cultivator cultivator, LivingEntity target) {
        if (!cultivator.isUsable(Meridian.HAND, (int) level.getGameTime())) {
            Tell.overlay(player, "eotp.message.hand_meridian_closed");
            return InteractionResult.FAIL;
        }

        FlyingSwordEntity existing = FlyingSwordEntity.of(level, player);
        if (existing != null) {
            existing.sendAt(target);
            return InteractionResult.SUCCESS;
        }

        if (!Cultivation.spend(player, SEND_COST)) return InteractionResult.FAIL;

        FlyingSwordEntity sword = EOTPEntities.FLYING_SWORD.get().create(level, EntitySpawnReason.TRIGGERED);
        if (sword == null) return InteractionResult.FAIL;

        SwordIntentData intent = intentOf(stack);
        SwordIntent dominant = intent.dominant();
        // A golden core lets the blade keep going through more than one enemy, and flowing-river
        // intent adds another cut on top of that.
        int cuts = cultivator.realm().atLeast(Realm.GOLDEN_CORE) ? 2 : 1;
        if (dominant == SwordIntent.FLOWING_RIVER) cuts++;

        float damage = BASE_DAMAGE * cultivator.channelStrength(Meridian.HAND, (int) level.getGameTime())
            * (1.0F + intent.mastery() * 0.5F);

        sword.snapTo(player.getX(), player.getEyeY(), player.getZ());
        sword.configure(player, damage, dominant, cuts);
        sword.sendAt(target);
        level.addFreshEntity(sword);

        Cultivation.practise(player, Meridian.HAND, 1.5F);
        Cultivation.teach(player, Discovery.SWORD_QI);
        return InteractionResult.SUCCESS;
    }

    /**
     * The creature the wielder is looking at, chosen by how closely it lines up with their gaze
     * rather than by a raycast, so a sword can be sent at something behind a fence. Nothing in
     * front means the gesture is read as sword Qi instead.
     */
    private @Nullable LivingEntity lookedAtCreature(ServerLevel level, Player player) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24.0))) {
            if (candidate == player || !candidate.isAlive()) continue;
            Vec3 toTarget = candidate.position().add(0.0, candidate.getBbHeight() * 0.5, 0.0)
                .subtract(player.getEyePosition()).normalize();
            if (toTarget.dot(player.getLookAngle()) < 0.65) continue;
            double distance = candidate.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }
}

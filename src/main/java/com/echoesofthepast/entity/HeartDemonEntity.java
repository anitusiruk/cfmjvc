package com.echoesofthepast.entity;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.echoesofthepast.util.Tell;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The shadow that comes up out of a cultivator during a dangerous breakthrough. It wears the
 * cultivator's own strength, and it cannot be killed with a weapon: hitting it only feeds it.
 *
 * <p>What actually beats a heart demon is composure - a clear heart, calming incense, a talisman
 * stamped to quiet things. Its {@code resolve} falls while the cultivator is in that kind of state,
 * and when its resolve runs out it simply stops existing.
 */
public class HeartDemonEntity extends Monster {
    /** How much composure it takes to dispel one. */
    private static final float MAX_RESOLVE = 100.0F;

    private float resolve = MAX_RESOLVE;
    private @Nullable UUID victim;
    /** Hits the demon has landed. Enough of them and the breakthrough fails. */
    private int landedHits;

    public HeartDemonEntity(EntityType<? extends HeartDemonEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public void bindTo(Player player) {
        this.victim = player.getUUID();
        this.setTarget(player);
    }

    public @Nullable UUID victim() {
        return this.victim;
    }

    public float resolveFraction() {
        return this.resolve / MAX_RESOLVE;
    }

    public int landedHits() {
        return this.landedHits;
    }

    /** Composure eats at the demon. Called by clear-heart effects, incense and talismans. */
    public void erode(float amount) {
        this.resolve -= amount;
        if (this.level() instanceof ServerLevel level) {
            QiVisuals.echo(level, this.position().add(0.0, 1.0, 0.0), 4);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        // A demon standing in composure is already losing.
        Player target = this.victim == null ? null : level.getPlayerByUUID(this.victim);
        if (target != null) {
            if (target.hasEffect(EOTPMobEffects.holder(EOTPMobEffects.CLEAR_HEART))) {
                this.erode(0.55F);
            }
            if (target.distanceToSqr(this) > 48.0 * 48.0) {
                // Running away from your own mind does not work.
                this.teleportTo(target.getX(), target.getY(), target.getZ());
            }
        }

        if (this.tickCount % 10 == 0) {
            QiVisuals.line(level, this.position().add(0.0, 0.2, 0.0), this.position().add(0.0, 1.9, 0.0), 0x2B1B33, 4);
        }

        if (this.resolve <= 0.0F) {
            this.dissolve(level, target);
        }
    }

    private void dissolve(ServerLevel level, @Nullable Player target) {
        QiVisuals.bloom(level, this.position().add(0.0, 1.0, 0.0), PhaseBlend.of(Phase.WATER));
        if (target != null) {
            Tell.chat(target, Component.translatable("eotp.message.heart_demon_dispelled"));
            Cultivation.teach(target, Discovery.HEART_DEMON_LORE);
        }
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // Weapons feed it. This is the mechanic, not a bug: you cannot stab your own doubts.
        if (source.getEntity() instanceof Player player) {
            this.resolve = Math.min(MAX_RESOLVE, this.resolve + damage * 0.4F);
            Tell.overlay(player, "eotp.message.heart_demon_feeds");
            QiVisuals.backlash(level, this.position().add(0.0, 1.0, 0.0), PhaseBlend.of(Phase.FIRE));
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt) {
            this.landedHits++;
        }
        return hurt;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** Where the demon should stand when it rises: just behind its host. */
    public static Vec3 risingPosition(Player player) {
        return player.position().add(player.getLookAngle().scale(-1.5)).add(0.0, 0.0, 0.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("resolve", this.resolve);
        output.putInt("landed", this.landedHits);
        if (this.victim != null) {
            output.store("victim", UUIDUtil.STRING_CODEC, this.victim);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.resolve = input.getFloatOr("resolve", MAX_RESOLVE);
        this.landedHits = input.getIntOr("landed", 0);
        this.victim = input.read("victim", UUIDUtil.STRING_CODEC).orElse(null);
    }
}

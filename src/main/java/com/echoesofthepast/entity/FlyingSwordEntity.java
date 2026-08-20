package com.echoesofthepast.entity;

import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.sword.SwordIntent;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A sword flying on its own. It has three moods, and which one it is in is the whole of its
 * behaviour:
 *
 * <ul>
 *   <li>{@code ORBIT} - hanging beside its owner, waiting;</li>
 *   <li>{@code STRIKE} - going for a target, hitting it once;</li>
 *   <li>{@code RETURN} - coming home, after which it orbits again.</li>
 * </ul>
 *
 * <p>The sword is not a projectile. It never despawns from missing, it just comes back, which is
 * what makes it read as a companion rather than an arrow.
 */
public class FlyingSwordEntity extends Entity {
    private static final double ORBIT_RADIUS = 1.4;
    private static final double STRIKE_SPEED = 1.1;
    private static final double RETURN_SPEED = 0.8;
    /** Ticks the sword can stay out before its Qi runs down and it returns. */
    private static final int ENDURANCE = 600;

    private enum Mood { ORBIT, STRIKE, RETURN }

    private Mood mood = Mood.ORBIT;
    private @Nullable UUID ownerId;
    private @Nullable UUID targetId;
    private @Nullable SwordIntent intent;
    private float damage = 6.0F;
    private int life;
    /** Targets already cut on this sortie, so a river-intent sword does not hit one thing twice. */
    private int cuts;
    private int maxCuts = 1;

    public FlyingSwordEntity(EntityType<? extends FlyingSwordEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    public void configure(Player owner, float damage, @Nullable SwordIntent intent, int maxCuts) {
        this.ownerId = owner.getUUID();
        this.damage = damage;
        this.intent = intent;
        this.maxCuts = Math.max(1, maxCuts);
    }

    public void sendAt(LivingEntity target) {
        this.targetId = target.getUUID();
        this.mood = Mood.STRIKE;
        this.cuts = 0;
    }

    public void recall() {
        this.mood = Mood.RETURN;
        this.targetId = null;
    }

    public boolean isOrbiting() {
        return this.mood == Mood.ORBIT;
    }

    public @Nullable UUID ownerId() {
        return this.ownerId;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        Player owner = this.ownerId == null ? null : level.getPlayerByUUID(this.ownerId);
        if (owner == null || ++this.life > ENDURANCE) {
            this.dissipate(level);
            return;
        }

        switch (this.mood) {
            case ORBIT -> this.orbit(owner);
            case STRIKE -> this.strike(level, owner);
            case RETURN -> this.returnHome(owner);
        }

        this.setPos(this.position().add(this.getDeltaMovement()));
        if (this.tickCount % 2 == 0) {
            QiVisuals.line(level, this.position(), this.position().subtract(this.getDeltaMovement()), this.trailColor(), 3);
        }
    }

    private void orbit(Player owner) {
        double angle = this.tickCount * 0.12;
        Vec3 wanted = owner.position()
            .add(Math.cos(angle) * ORBIT_RADIUS, owner.getBbHeight() * 0.75, Math.sin(angle) * ORBIT_RADIUS);
        this.setDeltaMovement(wanted.subtract(this.position()).scale(0.35));
    }

    private void strike(ServerLevel level, Player owner) {
        LivingEntity target = this.targetId == null ? null : this.findTarget(level);
        if (target == null || !target.isAlive()) {
            this.recall();
            return;
        }

        Vec3 toTarget = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(this.position());
        if (toTarget.length() < 1.2) {
            this.cut(level, owner, target);
            return;
        }
        this.setDeltaMovement(toTarget.normalize().scale(STRIKE_SPEED));

        // A sword in flight also cuts whatever it happens to pass through.
        for (LivingEntity bystander : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.6))) {
            if (bystander == owner || bystander == target) continue;
            if (this.cuts >= this.maxCuts) break;
            this.cut(level, owner, bystander);
        }
    }

    private void cut(ServerLevel level, Player owner, LivingEntity target) {
        float dealt = this.damage;
        if (this.intent == SwordIntent.PURIFYING
            && target.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
            dealt *= 1.6F;
        }
        target.hurtServer(level, level.damageSources().playerAttack(owner), dealt);
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.4F);
        QiVisuals.bloom(level, target.position().add(0.0, 1.0, 0.0), PhaseBlend.of(Phase.METAL));

        this.cuts++;
        if (this.cuts >= this.maxCuts) {
            this.recall();
        } else {
            this.targetId = null;
            // Look for the next thing worth cutting, which is what flowing-river intent is for.
            for (LivingEntity next : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0))) {
                if (next == owner || next == target || !next.isAlive()) continue;
                this.sendAt(next);
                this.cuts = Math.max(1, this.cuts);
                break;
            }
            if (this.targetId == null) this.recall();
        }
    }

    private void returnHome(Player owner) {
        Vec3 toOwner = owner.position().add(0.0, owner.getBbHeight() * 0.75, 0.0).subtract(this.position());
        if (toOwner.length() < 1.6) {
            this.mood = Mood.ORBIT;
            this.cuts = 0;
            return;
        }
        this.setDeltaMovement(toOwner.normalize().scale(RETURN_SPEED));
    }

    private @Nullable LivingEntity findTarget(ServerLevel level) {
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.blockPosition()).inflate(32.0))) {
            if (candidate.getUUID().equals(this.targetId)) return candidate;
        }
        return null;
    }

    private int trailColor() {
        if (this.intent == null) return 0xD8DCE0;
        return switch (this.intent) {
            case PURIFYING -> 0xFFF3C4;
            case FALLING_STAR -> 0xB9A6E8;
            case MOUNTAIN -> 0xC2A265;
            case FLOWING_RIVER -> 0x4C7FB5;
            case STILL_WATER -> 0x9FD8E0;
        };
    }

    private void dissipate(ServerLevel level) {
        QiVisuals.leak(level, this.blockPosition(), PhaseBlend.of(Phase.METAL), 1.0F);
        this.discard();
    }

    /** Finds the sword already out for a given owner, so a second cast controls it instead. */
    public static @Nullable FlyingSwordEntity of(ServerLevel level, Player owner) {
        for (FlyingSwordEntity sword : level.getEntitiesOfClass(FlyingSwordEntity.class,
            owner.getBoundingBox().inflate(48.0))) {
            if (owner.getUUID().equals(sword.ownerId)) return sword;
        }
        return null;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    /** A sword in flight cannot be swatted out of the air. */
    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putString("mood", this.mood.name());
        output.putFloat("damage", this.damage);
        output.putInt("life", this.life);
        output.putInt("max_cuts", this.maxCuts);
        if (this.ownerId != null) output.store("owner", UUIDUtil.STRING_CODEC, this.ownerId);
        output.storeNullable("intent", SwordIntent.CODEC, this.intent);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.mood = Mood.valueOf(input.getStringOr("mood", Mood.ORBIT.name()));
        this.damage = input.getFloatOr("damage", 6.0F);
        this.life = input.getIntOr("life", 0);
        this.maxCuts = input.getIntOr("max_cuts", 1);
        this.ownerId = input.read("owner", UUIDUtil.STRING_CODEC).orElse(null);
        this.intent = input.read("intent", SwordIntent.CODEC).orElse(null);
    }

    /** Entity type helper so the sword can be used as a brief platform at high cultivation. */
    public boolean canCarry(Entity rider) {
        return this.mood == Mood.ORBIT && rider.getUUID().equals(this.ownerId);
    }
}

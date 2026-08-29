package com.echoesofthepast.talisman;

import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.echoesofthepast.seal.SealRule;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A talisman is a short written instruction, not a spell. Each one is a seal rule expressed on paper
 * in a particular ink, and each does one small, obvious thing when Qi runs through it.
 */
public enum TalismanType implements StringRepresentable {
    REPULSION("repulsion", SealRule.REPEL, PhaseBlend.of(Phase.METAL), 4) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            AABB area = new AABB(pos).inflate(3.0 + strength);
            Vec3 center = Vec3.atCenterOf(pos);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                Vec3 push = entity.position().subtract(center).normalize().scale(0.6 + strength * 0.2);
                entity.push(push.x, 0.35, push.z);
                entity.hurtMarked = true;
            }
            QiVisuals.ring(level, center, 2.0, this.blend().color(), 20);
        }
    },
    EMBER("ember", SealRule.BIND, PhaseBlend.of(Phase.FIRE), 3) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            BlockPos target = pos.relative(facing.getOpposite());
            if (level.getBlockState(target).isAir() && BaseFireBlock.canBePlacedAt(level, target, Direction.UP)) {
                level.setBlockAndUpdate(target, Blocks.FIRE.defaultBlockState());
            }
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(2.0))) {
                entity.igniteForSeconds(2 + (int) strength);
            }
        }
    },
    BINDING("binding", SealRule.BIND, PhaseBlend.of(Phase.EARTH), 5) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            int duration = 40 + (int) (strength * 40.0F);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3.5))) {
                if (entity instanceof Player) continue;
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 5, false, true));
                entity.setDeltaMovement(Vec3.ZERO);
            }
        }
    },
    PRESERVATION("preservation", SealRule.PRESERVE, PhaseBlend.of(Phase.EARTH), 8) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4.0 + strength))) {
                item.setUnlimitedLifetime();
            }
        }
    },
    CLEAR_HEART("clear_heart", SealRule.SILENCE, PhaseBlend.of(Phase.WATER), 6) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            int duration = 200 + (int) (strength * 200.0F);
            for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(6.0))) {
                player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.CLEAR_HEART, duration, 0));
            }
            QiVisuals.bloom(level, Vec3.atCenterOf(pos), PhaseBlend.of(Phase.WATER));
        }
    },
    GATHER("gather", SealRule.GATHER, PhaseBlend.of(Phase.WOOD), 4) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            Vec3 center = Vec3.atCenterOf(pos);
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(6.0 + strength))) {
                Vec3 pull = center.subtract(item.position()).normalize().scale(0.25);
                item.setDeltaMovement(item.getDeltaMovement().add(pull));
            }
        }
    },
    SILENCE("silence", SealRule.SILENCE, PhaseBlend.of(Phase.WATER), 6) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(8.0 + strength))) {
                mob.setTarget(null);
            }
        }
    },
    RETURN("return", SealRule.RETURN, PhaseBlend.of(Phase.METAL), 5) {
        @Override
        public void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength) {
            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, new AABB(pos).inflate(4.0))) {
                projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-1.2));
                projectile.hurtMarked = true;
            }
            QiVisuals.ring(level, Vec3.atCenterOf(pos), 1.5, this.blend().color(), 12);
        }
    };

    public static final TalismanType[] VALUES = values();
    public static final Codec<TalismanType> CODEC = StringRepresentable.fromEnum(TalismanType::values);

    private final String name;
    private final SealRule rule;
    private final PhaseBlend blend;
    private final float cost;

    TalismanType(String name, SealRule rule, PhaseBlend blend, float cost) {
        this.name = name;
        this.rule = rule;
        this.blend = blend;
        this.cost = cost;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** The seal rule this talisman is an expression of. */
    public SealRule rule() {
        return this.rule;
    }

    /** The character of Qi the paper wants; feeding it something else weakens the effect. */
    public PhaseBlend blend() {
        return this.blend;
    }

    /** Qi needed to fire the talisman once. */
    public float cost() {
        return this.cost;
    }

    public String translationKey() {
        return "eotp.talisman." + this.name;
    }

    public String itemName() {
        return this.name + "_talisman";
    }

    /** Does the thing. Called with the strength of the Qi that set it off. */
    public abstract void trigger(ServerLevel level, BlockPos pos, Direction facing, float strength);
}

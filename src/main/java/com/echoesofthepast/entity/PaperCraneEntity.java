package com.echoesofthepast.entity;

import com.echoesofthepast.channel.SealChannels;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A folded paper bird carrying one item to wherever the matching seal is stamped. Slow, visible,
 * and physically present - which is the point. A workshop automated with cranes looks like a
 * workshop with things being carried around it, not like a floor full of pipes.
 *
 * <p>A crane that cannot find its destination lands, unfolds and drops what it was carrying.
 */
public class PaperCraneEntity extends Entity {
    private static final double SPEED = 0.22;
    /** Ticks before a lost crane gives up. */
    private static final int PATIENCE = 1200;
    /** How high above its route a crane flies, so it clears walls. */
    private static final double CRUISE_HEIGHT = 2.5;

    private ItemStack cargo = ItemStack.EMPTY;
    private @Nullable String channel;
    private @Nullable BlockPos destination;
    private int age;
    private boolean wetted;

    public PaperCraneEntity(EntityType<? extends PaperCraneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    public void load(ItemStack cargo, String channel) {
        this.cargo = cargo.copy();
        this.channel = channel;
    }

    public ItemStack cargo() {
        return this.cargo;
    }

    public @Nullable String channel() {
        return this.channel;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        this.age++;
        if (this.isInWater() || this.level().isRainingAt(this.blockPosition())) {
            this.wetted = true;
        }
        if (this.wetted) {
            // Wet paper does not fly.
            this.unfold(level);
            return;
        }

        if (this.destination == null || this.age % 40 == 0) {
            this.destination = this.channel == null ? null : SealChannels.findNearest(level, this.channel, this.blockPosition(), 64);
        }
        if (this.destination == null || this.age > PATIENCE) {
            this.unfold(level);
            return;
        }

        Vec3 target = Vec3.atCenterOf(this.destination);
        double horizontal = target.subtract(this.position()).horizontalDistance();
        // Climb to cruise height first, then descend onto the destination.
        double wantedY = horizontal > 2.0
            ? Math.max(target.y, this.position().y) + (this.position().y < target.y + CRUISE_HEIGHT ? CRUISE_HEIGHT : 0.0)
            : target.y + 0.5;
        Vec3 waypoint = new Vec3(target.x, wantedY, target.z);
        Vec3 step = waypoint.subtract(this.position());
        if (step.length() > 1.0E-3) {
            this.setDeltaMovement(step.normalize().scale(SPEED));
            this.setPos(this.position().add(this.getDeltaMovement()));
            this.setYRot((float) (Math.atan2(step.z, step.x) * (180.0 / Math.PI)) - 90.0F);
        }

        if (this.position().distanceToSqr(target) < 1.4) {
            this.deliver(level, this.destination);
            return;
        }

        if (this.tickCount % 6 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private void deliver(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            ItemStack remainder = insert(container, this.cargo);
            this.cargo = remainder;
        }
        if (!this.cargo.isEmpty()) {
            // Nowhere to put it: the crane sets it down neatly rather than losing it.
            level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(), this.cargo.copy()));
        }
        QiVisuals.bloom(level, this.position(), PhaseBlend.of(Phase.WOOD));
        this.discard();
    }

    private void unfold(ServerLevel level) {
        if (!this.cargo.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(), this.cargo.copy()));
        }
        QiVisuals.leak(level, this.blockPosition(), PhaseBlend.of(Phase.WOOD), 1.0F);
        this.discard();
    }

    private static ItemStack insert(Container container, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty()) {
                container.setItem(slot, remaining.copy());
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int room = Math.min(existing.getMaxStackSize(), container.getMaxStackSize()) - existing.getCount();
                int moved = Math.min(room, remaining.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
        }
        return remaining;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("age", this.age);
        output.putBoolean("wet", this.wetted);
        if (this.channel != null) output.putString("channel", this.channel);
        if (!this.cargo.isEmpty()) output.store("cargo", ItemStack.CODEC, this.cargo);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.age = input.getIntOr("age", 0);
        this.wetted = input.getBooleanOr("wet", false);
        this.channel = input.getString("channel").orElse(null);
        this.cargo = input.read("cargo", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /** Cranes are paper: anything that hits them destroys them. */
    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        this.unfold(level);
        return true;
    }

    /** Used by the loom and by tablets to check whether a crane is already on this errand. */
    public static boolean anyHeadingTo(ServerLevel level, Entity near, String channel) {
        for (PaperCraneEntity crane : level.getEntitiesOfClass(PaperCraneEntity.class, near.getBoundingBox().inflate(24.0))) {
            if (channel.equals(crane.channel)) return true;
        }
        return false;
    }
}

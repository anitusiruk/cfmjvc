package com.echoesofthepast.entity;

import com.echoesofthepast.qi.QiVisuals;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The body a cultivator leaves sitting behind while their nascent spirit is out walking. It is not a
 * decoration: it marks where the spirit has to return to, and if it is destroyed the spirit is
 * dragged back immediately.
 */
public class MeditatingBodyEntity extends Entity {
    private @Nullable UUID owner;

    public MeditatingBodyEntity(EntityType<? extends MeditatingBodyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.tickCount % 8 == 0) {
            QiVisuals.echo(level, this.position().add(0.0, 0.8, 0.0), 2);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    /** Disturbing the body is how a spirit gets pulled back: any hit destroys it. */
    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        this.discard();
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.owner != null) {
            output.store("owner", UUIDUtil.STRING_CODEC, this.owner);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.owner = input.read("owner", UUIDUtil.STRING_CODEC).orElse(null);
    }
}

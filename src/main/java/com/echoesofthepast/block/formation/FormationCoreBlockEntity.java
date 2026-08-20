package com.echoesofthepast.block.formation;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Realm;
import com.echoesofthepast.formation.FormationSurvey;
import com.echoesofthepast.formation.FormationType;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPacket;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiPulseReceiver;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Reads the circuit, works out what it is, and runs it while there is Qi to pay for it.
 *
 * <p>Two things throttle a formation: the Qi in the core, and how well what it is being fed matches
 * what the formation wants. Feeding a cultivation circuit nothing but fire Qi will run it, badly.
 */
public class FormationCoreBlockEntity extends QiDeviceBlockEntity implements QiPulseReceiver {
    /** The circuit is re-read this often; laying more tiles takes effect within a few seconds. */
    private static final int RESURVEY_INTERVAL = 60;

    private @Nullable FormationSurvey survey;
    private @Nullable FormationType type;
    private @Nullable UUID owner;
    private float strength;

    public FormationCoreBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.FORMATION_CORE.get(), pos, state, 240.0F);
    }

    public @Nullable FormationType type() {
        return this.type;
    }

    public float strength() {
        return this.strength;
    }

    public @Nullable FormationSurvey survey() {
        return this.survey;
    }

    /** True while the circuit is genuinely running, which rituals ask about. */
    public boolean isActive() {
        return this.type != null && this.strength > 0.15F;
    }

    public boolean isRunning(FormationType wanted) {
        return this.isActive() && this.type == wanted;
    }

    @Override
    protected int idleParticleInterval() {
        return 40;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.age % RESURVEY_INTERVAL == 0 || this.survey == null) {
            this.survey = FormationSurvey.scan(level, this.worldPosition);
            this.type = FormationType.identify(this.survey);
            // A bigger, better conducting circuit holds more Qi, which is the real reward for
            // building something elaborate.
            this.storage.setCapacityMultiplier(1.0F + this.survey.conductance() * 0.05F);
        }

        FormationSurvey currentSurvey = this.survey;
        FormationType currentType = this.type;
        if (currentType == null || currentSurvey == null) {
            this.strength = 0.0F;
            return;
        }

        if (!this.ownerUnderstands(level, currentType)) {
            this.strength = 0.0F;
            return;
        }

        if (this.age % 20 != 0) {
            if (this.strength > 0.15F) {
                this.runEffect(level, currentType, currentSurvey);
            }
            return;
        }

        float upkeep = currentType.upkeep();
        QiPacket paid = this.storage.extract(upkeep, false);
        if (paid.amount() < upkeep * 0.25F) {
            this.strength = Math.max(0.0F, this.strength - 0.25F);
            return;
        }

        // Match between what the formation wants and what it is being fed.
        float match = currentType.preferredBlend().isEmpty()
            ? 1.0F
            : 0.4F + 0.6F * paid.blend().similarity(currentType.preferredBlend());
        float supply = paid.amount() / upkeep;
        float banners = 1.0F + Math.min(1.0F, currentSurvey.banners() * 0.15F);
        this.strength = Math.min(2.0F, supply * match * banners);

        this.runEffect(level, currentType, currentSurvey);

        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        QiVisuals.ring(level, center, currentSurvey.radius(), currentSurvey.inkBlend().isEmpty()
            ? paid.blend().color() : currentSurvey.inkBlend().color(), 16);
        this.setChanged();
    }

    /** A formation obeys a cultivator, not a builder; an unlearned circuit simply sits there. */
    private boolean ownerUnderstands(ServerLevel level, FormationType type) {
        if (this.owner == null) return false;
        Player player = level.getPlayerByUUID(this.owner);
        Cultivator cultivator = player == null ? null : Cultivation.of(player);
        if (cultivator == null) {
            // The owner is offline: the circuit keeps running on what it was taught, but weakly.
            return true;
        }
        return cultivator.realm().canRunFormations() && cultivator.knows(type.discovery());
    }

    private void runEffect(ServerLevel level, FormationType type, FormationSurvey survey) {
        double radius = survey.radius();
        double height = 1.0 + survey.banners() * 0.75;
        AABB area = new AABB(this.worldPosition).inflate(radius, height, radius);

        switch (type) {
            case GATHERING -> {
                // Ambient Qi is drawn from the whole footprint, so a wider ring gathers more even
                // though it costs no more to run.
                float ambient = DragonVeins.ambientQi(level, this.worldPosition);
                float gathered = (0.4F + ambient * 2.2F) * this.strength * (float) Math.sqrt(survey.size());
                var phase = DragonVeins.phaseOf(level, this.worldPosition);
                this.storage.insert(gathered * 0.15F, PhaseBlend.of(phase), false);
                QiNet.pushAround(level, this.worldPosition, this.storage, 8.0F * this.strength, 0.95F);
            }
            case REPULSION -> {
                if (this.age % 10 != 0) return;
                List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity instanceof Enemy);
                for (LivingEntity victim : victims) {
                    Vec3 push = victim.position().subtract(Vec3.atCenterOf(this.worldPosition)).normalize()
                        .scale(0.35 * this.strength);
                    victim.push(push.x, 0.1 * this.strength, push.z);
                    victim.hurtMarked = true;
                }
            }
            case CULTIVATION -> {
                if (this.age % 20 != 0) return;
                for (Player player : level.getEntitiesOfClass(Player.class, area)) {
                    if (player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) continue;
                    Cultivation.insight(player, 0.4F * this.strength);
                }
            }
            case PRESERVATION -> {
                if (this.age % 40 != 0) return;
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
                    item.setUnlimitedLifetime();
                }
            }
            case ATTUNEMENT -> {
                // Attunement does nothing on its own; the root ritual reads the circuit's strength.
                if (this.age % 20 == 0) {
                    QiVisuals.echo(level, Vec3.atCenterOf(this.worldPosition).add(0.0, 1.0, 0.0), 3);
                }
            }
        }
    }

    /** Right-clicking the core: what did I actually draw, and is it running? */
    public void inspect(Player player) {
        if (!(this.level instanceof ServerLevel level)) return;
        this.owner = player.getUUID();
        this.survey = FormationSurvey.scan(level, this.worldPosition);
        this.type = FormationType.identify(this.survey);
        this.setChanged();

        FormationSurvey current = this.survey;
        if (this.type == null) {
            Tell.chat(player, Component.translatable("eotp.message.formation_incomplete",
                current.size(), current.closed() ? 1 : 0));
            return;
        }

        Tell.chat(player, Component.translatable("eotp.message.formation_identified",
            Component.translatable(this.type.translationKey()),
            current.size(),
            current.banners(),
            Math.round(this.strength * 100.0F)));

        if (Cultivation.realmOf(player) == Realm.MORTAL) {
            Tell.chat(player, Component.translatable("eotp.message.formation_ignores_you"));
        }
    }

    @Override
    public boolean onQiPulse(QiPulse pulse, Direction from) {
        this.storage.insert(pulse.amount(), pulse.blend(), false);
        return true;
    }

    /** Used by rituals standing on this formation. */
    public boolean consume(float amount) {
        return this.storage.tryConsume(amount);
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("strength", this.strength);
        if (this.owner != null) {
            output.store("owner", UUIDUtil.STRING_CODEC, this.owner);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.strength = input.getFloatOr("strength", 0.0F);
        this.owner = input.read("owner", UUIDUtil.STRING_CODEC).orElse(null);
    }

    /** Finds the formation running under a position, which is how rituals locate their circuit. */
    public static @Nullable FormationCoreBlockEntity findNear(ServerLevel level, BlockPos pos, int radius) {
        for (BlockPos candidate : BlockPos.betweenClosed(
            pos.offset(-radius, -2, -radius), pos.offset(radius, 2, radius))) {
            if (level.getBlockEntity(candidate) instanceof FormationCoreBlockEntity core) {
                return core;
            }
        }
        return null;
    }

    /** True if this entity stands inside the circuit's footprint. */
    public boolean covers(Entity entity) {
        FormationSurvey current = this.survey;
        if (current == null) return false;
        double radius = current.radius() + 1.0;
        return entity.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= radius * radius;
    }
}

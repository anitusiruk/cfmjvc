package com.echoesofthepast.block.landscape;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.block.formation.FormationCoreBlockEntity;
import com.echoesofthepast.block.plant.LingzhiFungusBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.CultivationStore;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.InnerLandscape;
import com.echoesofthepast.cultivation.Principle;
import com.echoesofthepast.cultivation.Realm;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.formation.FormationType;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.registry.EOTPTags;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.world.DragonVeins;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Watches a garden and decides whether it has become a living spiritual ecology.
 *
 * <p>Filling a circle with five coloured blocks does nothing. The stele looks for phases actually
 * being produced, those same phases actually being consumed, turbulence actually being cleaned, and
 * for none of it collapsing into a single dominant phase. Only a place that survives several whole
 * day/night cycles under those rules can be imprinted.
 */
public class LandscapeSteleBlockEntity extends QiDeviceBlockEntity {
    /** How far from the stele the ecology is read. */
    private static final int RADIUS = 12;
    /** Complete day/night cycles a place must survive before it can be imprinted. */
    private static final int CYCLES_REQUIRED = 3;
    /** Distinct phases that must be generated inside the boundary. */
    private static final int PHASES_REQUIRED = 3;
    /** Share of the whole above which one phase is dominating the place. */
    private static final float DOMINANCE_LIMIT = 0.7F;

    private final Map<Phase, Float> produced = new EnumMap<>(Phase.class);
    private final Map<Phase, Float> consumed = new EnumMap<>(Phase.class);
    private float turbulenceCleaned;
    private float diversitySeen;
    private int cyclesSurvived;
    private boolean wasDay;
    private boolean mature;

    public LandscapeSteleBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.LANDSCAPE_STELE.get(), pos, state, 60.0F);
    }

    @Override
    protected int idleParticleInterval() {
        return this.mature ? 40 : 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.age % 40 == 0) {
            this.survey(level);
        }

        boolean day = level.isBrightOutside();
        if (day != this.wasDay) {
            this.wasDay = day;
            // A full cycle is counted at each dawn, which is why a Landscape takes real days.
            if (day) {
                this.completeCycle(level);
            }
        }
    }

    /** Reads the living systems inside the boundary. */
    private void survey(ServerLevel level) {
        Set<Block> distinctBlocks = new HashSet<>();
        boolean sawFire = false;
        boolean sawWater = false;

        for (BlockPos pos : BlockPos.betweenClosed(
            this.worldPosition.offset(-RADIUS, -4, -RADIUS),
            this.worldPosition.offset(RADIUS, 6, RADIUS)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            if (state.is(EOTPTags.Blocks.NATURAL_VARIETY) || state.is(EOTPTags.Blocks.QI_INSULATING)) {
                distinctBlocks.add(state.getBlock());
            }
            if (!state.getFluidState().isEmpty()) {
                sawWater = true;
                distinctBlocks.add(state.getBlock());
            }
            if (state.getLightEmission() > 8) sawFire = true;

            // Storage that is filling counts as production; storage that is draining counts as use.
            QiNode node = QiNet.nodeAt(level, pos);
            if (node != null) {
                QiStorage storage = node.qiStorage(null);
                if (storage != null && !storage.isEmpty()) {
                    Phase dominant = storage.blend().dominant();
                    if (dominant != null) {
                        float amount = storage.fillRatio();
                        this.produced.merge(dominant, amount * 0.25F, Float::sum);
                        if (storage.fillRatio() < 0.9F) {
                            this.consumed.merge(dominant, amount * 0.2F, Float::sum);
                        }
                    }
                }
            }
            if (level.getBlockEntity(pos) instanceof LingzhiFungusBlockEntity) {
                this.turbulenceCleaned += 0.2F;
            }
        }

        if (sawWater) this.produced.merge(Phase.WATER, 0.4F, Float::sum);
        if (sawFire) this.produced.merge(Phase.FIRE, 0.3F, Float::sum);

        float vein = DragonVeins.strength(level, this.worldPosition);
        if (vein > 0.2F) {
            this.produced.merge(DragonVeins.phaseOf(level, this.worldPosition), vein * 0.4F, Float::sum);
        }

        int creatures = level.getEntitiesOfClass(
            LivingEntity.class, new AABB(this.worldPosition).inflate(RADIUS)
        ).size();
        this.diversitySeen = Math.max(this.diversitySeen, distinctBlocks.size() + Math.min(6, creatures));
        this.setChanged();
    }

    /** At each dawn, checks whether the place kept its promises through the whole cycle. */
    private void completeCycle(ServerLevel level) {
        boolean enoughPhases = this.producedPhases().size() >= PHASES_REQUIRED;
        boolean everythingConsumed = this.producedPhases().stream()
            .allMatch(phase -> this.consumed.getOrDefault(phase, 0.0F) > 0.05F);
        boolean balanced = this.dominanceShare() <= DOMINANCE_LIMIT;

        if (enoughPhases && everythingConsumed && balanced) {
            this.cyclesSurvived++;
        } else {
            // A place that stops working loses ground rather than being reset to nothing.
            this.cyclesSurvived = Math.max(0, this.cyclesSurvived - 1);
        }

        boolean nowMature = this.cyclesSurvived >= CYCLES_REQUIRED;
        if (nowMature != this.mature) {
            this.mature = nowMature;
            level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(LandscapeSteleBlock.MATURE, nowMature));
            if (nowMature) {
                QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition).add(0.0, 1.0, 0.0), this.blend());
            }
        }
        this.setChanged();
    }

    private Set<Phase> producedPhases() {
        Set<Phase> phases = EnumSet.noneOf(Phase.class);
        this.produced.forEach((phase, amount) -> {
            if (amount > 0.5F) phases.add(phase);
        });
        return phases;
    }

    private float totalProduced() {
        float total = 0.0F;
        for (float amount : this.produced.values()) total += amount;
        return total;
    }

    private float dominanceShare() {
        float total = this.totalProduced();
        if (total <= 0.0F) return 1.0F;
        float highest = 0.0F;
        for (float amount : this.produced.values()) highest = Math.max(highest, amount);
        return highest / total;
    }

    private com.echoesofthepast.qi.PhaseBlend blend() {
        com.echoesofthepast.qi.PhaseBlend blend = com.echoesofthepast.qi.PhaseBlend.EMPTY;
        for (Map.Entry<Phase, Float> entry : this.produced.entrySet()) {
            blend = blend.with(entry.getKey(), entry.getValue());
        }
        return blend.normalised();
    }

    /** Echo Essence at a mature stele reads the whole garden and writes it into the cultivator. */
    public boolean offer(Player player, ItemStack stack) {
        if (!(this.level instanceof ServerLevel level)) return false;
        if (!stack.is(EOTPItems.ECHO_ESSENCE.get())) return false;

        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return false;
        if (!cultivator.realm().atLeast(Realm.GOLDEN_CORE)) {
            Tell.overlay(player, "eotp.message.landscape_needs_core");
            return true;
        }
        if (!this.mature) {
            this.report(player);
            return true;
        }

        FormationCoreBlockEntity core = FormationCoreBlockEntity.findNear(level, this.worldPosition, 4);
        if (core == null || !core.isRunning(FormationType.ATTUNEMENT)) {
            Tell.overlay(player, "eotp.message.attunement_not_running");
            return true;
        }

        InnerLandscape imprint = this.read();
        InnerLandscape previous = cultivator.path().landscape();
        cultivator.path().setLandscape(imprint);
        // Rewriting who you are shakes a formed core loose for a while.
        if (previous != null) cultivator.destabiliseCore(2400);
        CultivationStore.touch(player);
        stack.shrink(1);

        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition).add(0.0, 1.0, 0.0), imprint.blend());
        Tell.chat(player, Component.translatable("eotp.message.landscape_imprinted", imprint.name()));
        Tell.chat(player, imprint.describe());
        EchoLog.record(level, this.worldPosition, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.landscape", player.getName(), imprint.name()));
        return true;
    }

    /** Turns the recorded history into the imprint the cultivator will carry. */
    private InnerLandscape read() {
        Phase dominant = Phase.EARTH;
        Phase secondary = Phase.WATER;
        float best = -1.0F;
        float second = -1.0F;
        for (Map.Entry<Phase, Float> entry : this.produced.entrySet()) {
            if (entry.getValue() > best) {
                second = best;
                secondary = dominant;
                best = entry.getValue();
                dominant = entry.getKey();
            } else if (entry.getValue() > second) {
                second = entry.getValue();
                secondary = entry.getKey();
            }
        }

        float totalProduced = Math.max(0.001F, this.totalProduced());
        float totalConsumed = 0.0F;
        for (float amount : this.consumed.values()) totalConsumed += amount;

        float cyclical = Math.min(1.0F, totalConsumed / totalProduced);
        float stability = Math.min(1.0F, this.turbulenceCleaned / 40.0F + (1.0F - this.dominanceShare()));
        float diversity = Math.min(1.0F, this.diversitySeen / 24.0F);

        // The strongest thing the place did decides the relationship it teaches.
        Principle relationship;
        if (cyclical > 0.7F) {
            relationship = Principle.RETURN;
        } else if (this.turbulenceCleaned > 20.0F) {
            relationship = Principle.PRESERVATION;
        } else if (dominant == Phase.FIRE || dominant == Phase.METAL) {
            relationship = Principle.TRANSFORMATION;
        } else {
            relationship = Principle.GROWTH;
        }

        return new InnerLandscape(dominant, secondary, relationship, cyclical, stability, diversity, java.util.List.of());
    }

    /** Empty-handed: what the place still owes before it can be imprinted. */
    public void report(Player player) {
        Tell.chat(player, Component.translatable(
            "eotp.message.landscape_state",
            this.cyclesSurvived,
            CYCLES_REQUIRED,
            this.producedPhases().size(),
            PHASES_REQUIRED,
            Math.round(this.dominanceShare() * 100.0F)
        ));
        if (!this.mature) {
            Tell.chat(player, Component.translatable("eotp.message.landscape_rules"));
        }
    }

    public boolean isMature() {
        return this.mature;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("cycles", this.cyclesSurvived);
        output.putBoolean("mature", this.mature);
        output.putBoolean("was_day", this.wasDay);
        output.putFloat("cleaned", this.turbulenceCleaned);
        output.putFloat("diversity", this.diversitySeen);
        ValueOutput producedOut = output.child("produced");
        this.produced.forEach((phase, amount) -> producedOut.putFloat(phase.getSerializedName(), amount));
        ValueOutput consumedOut = output.child("consumed");
        this.consumed.forEach((phase, amount) -> consumedOut.putFloat(phase.getSerializedName(), amount));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.cyclesSurvived = input.getIntOr("cycles", 0);
        this.mature = input.getBooleanOr("mature", false);
        this.wasDay = input.getBooleanOr("was_day", false);
        this.turbulenceCleaned = input.getFloatOr("cleaned", 0.0F);
        this.diversitySeen = input.getFloatOr("diversity", 0.0F);

        this.produced.clear();
        input.child("produced").ifPresent(child -> {
            for (Phase phase : Phase.VALUES) {
                float amount = child.getFloatOr(phase.getSerializedName(), 0.0F);
                if (amount > 0.0F) this.produced.put(phase, amount);
            }
        });
        this.consumed.clear();
        input.child("consumed").ifPresent(child -> {
            for (Phase phase : Phase.VALUES) {
                float amount = child.getFloatOr(phase.getSerializedName(), 0.0F);
                if (amount > 0.0F) this.consumed.put(phase, amount);
            }
        });
    }
}

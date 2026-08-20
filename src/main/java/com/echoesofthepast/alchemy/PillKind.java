package com.echoesofthepast.alchemy;

import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.CultivationStore;
import com.echoesofthepast.cultivation.Cultivator;
import com.echoesofthepast.cultivation.Meridian;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.echoesofthepast.util.Tell;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Pills hit hard and then stop working, which is the point: the right play is to keep one for the
 * moment it matters, not to eat four stacks.
 *
 * <p>Every kind reads the eater's tolerance, so the second pill of the same sort within a session is
 * roughly half as useful as the first, and the fifth barely registers.
 */
public enum PillKind implements StringRepresentable {
    /** Forces a channel further open. The only reliable shortcut past a stubborn meridian. */
    MERIDIAN_OPENING("meridian_opening", PhaseBlend.of(Phase.WOOD, Phase.WATER)) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            Meridian target = null;
            float lowest = Float.MAX_VALUE;
            for (Meridian meridian : Meridian.VALUES) {
                if (cultivator.isOpen(meridian)) continue;
                float progress = cultivator.meridianEffort(meridian) / meridian.effortRequired();
                if (progress < lowest) {
                    lowest = progress;
                    target = meridian;
                }
            }
            if (target == null) {
                Tell.overlay(player, "eotp.message.nothing_left_to_open");
                return;
            }
            Cultivation.practise(player, target, target.effortRequired() * 0.35F * strength);
        }
    },
    /** Settles the mind. What you take before a breakthrough, and what quiets a heart demon. */
    CLEAR_HEART("clear_heart", PhaseBlend.of(Phase.WATER)) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.CLEAR_HEART, (int) (1200 * strength), 0));
            player.removeEffect(EOTPMobEffects.holder(EOTPMobEffects.QI_DEVIATION));
        }
    },
    /** Tempers the body. Real, brief toughness rather than a permanent stat bump. */
    BODY_TEMPERING("body_tempering", PhaseBlend.of(Phase.EARTH, Phase.METAL)) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, (int) (600 * strength), 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, (int) (900 * strength), 1));
        }
    },
    /** Refills personal Qi. The workhorse. */
    QI_RECOVERY("qi_recovery", PhaseBlend.BALANCED) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            cultivator.addQi(cultivator.qiCapacity() * 0.45F * strength, cultivator.root().blend());
            CultivationStore.touch(player);
        }
    },
    /** Extra steps on air for a while, even without the shoes. */
    CLOUDSTEP("cloudstep", PhaseBlend.of(Phase.WATER, Phase.WOOD)) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.CLOUDSTEP, (int) (1800 * strength), 0));
        }
    },
    /** Smooths out a conflicted root for a while, which is how mixed cultivators survive. */
    FIVE_PHASE_HARMONY("five_phase_harmony", PhaseBlend.BALANCED) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            cultivator.addQi(cultivator.qiCapacity() * 0.2F * strength, PhaseBlend.BALANCED);
            player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.CLEAR_HEART, (int) (900 * strength), 1));
            CultivationStore.touch(player);
        }
    },
    /** Forgives one failed breakthrough and steadies a rattled core. */
    BREAKTHROUGH_STABILISING("breakthrough_stabilising", PhaseBlend.of(Phase.EARTH)) {
        @Override
        protected void apply(ServerPlayer player, Cultivator cultivator, float strength) {
            cultivator.forgiveFailure();
            cultivator.settleCore((int) (4000 * strength));
            CultivationStore.touch(player);
        }
    };

    public static final PillKind[] VALUES = values();
    public static final Codec<PillKind> CODEC = StringRepresentable.fromEnum(PillKind::values);

    private final String name;
    private final PhaseBlend recipeBlend;

    PillKind(String name, PhaseBlend recipeBlend) {
        this.name = name;
        this.recipeBlend = recipeBlend;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /** The phase relationship the cauldron has to hold to make this pill well. */
    public PhaseBlend recipeBlend() {
        return this.recipeBlend;
    }

    public String itemName() {
        return this.name + "_pill";
    }

    public String translationKey() {
        return "eotp.pill." + this.name;
    }

    /**
     * Eats the pill. Strength is the pill's grade multiplied by how tired the eater's body is of
     * this particular medicine.
     */
    public void consume(ServerPlayer player, PillQuality quality) {
        Cultivator cultivator = Cultivation.of(player);
        if (cultivator == null) return;

        float effectiveness = cultivator.pillEffectiveness(this.name);
        float strength = quality.potency() * quality.duration() * 0.5F * effectiveness;
        this.apply(player, cultivator, Math.max(0.1F, strength));
        cultivator.notePillTaken(this.name);
        CultivationStore.touch(player);

        if (effectiveness < 0.5F) {
            Tell.overlay(player, Component.translatable("eotp.message.pill_dulled",
                Math.round(effectiveness * 100.0F)));
        }
        // A cracked pill always leaves something behind.
        if (quality == PillQuality.CRACKED) {
            player.addEffect(EOTPMobEffects.loud(EOTPMobEffects.QI_DEVIATION, 200, 0));
        }
    }

    protected abstract void apply(ServerPlayer player, Cultivator cultivator, float strength);
}

package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Effects the mod applies to people rather than to blocks. They are deliberately few: cultivation
 * state lives in the cultivator's own record, not in a stack of potion icons.
 */
public final class EOTPMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EchoesOfThePast.MODID);

    private EOTPMobEffects() {}

    /** Qi running the wrong way after a failed technique: abilities cost more and misfire. */
    public static final RegistryObject<MobEffect> QI_DEVIATION =
        MOB_EFFECTS.register("qi_deviation", () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x6E2B4B));

    /** A settled mind: cultivation is stable and the heart demon has little to work with. */
    public static final RegistryObject<MobEffect> CLEAR_HEART =
        MOB_EFFECTS.register("clear_heart", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x9FD8E0));

    /** Qi vision: dragon veins, formation lines and echoes are visible without a tool in hand. */
    public static final RegistryObject<MobEffect> SPIRIT_SIGHT =
        MOB_EFFECTS.register("spirit_sight", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xB9A6E8));

    /** Extra midair steps, granted by pills as well as by shoes. */
    public static final RegistryObject<MobEffect> CLOUDSTEP =
        MOB_EFFECTS.register("cloudstep", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xE8EEF5));

    /** A meridian deliberately needled shut so its Qi is routed elsewhere. */
    public static final RegistryObject<MobEffect> MERIDIAN_SEALED =
        MOB_EFFECTS.register("meridian_sealed", () -> new SimpleEffect(MobEffectCategory.NEUTRAL, 0x4A4A55));

    public static void register(BusGroup modBus) {
        MOB_EFFECTS.register(modBus);
    }

    /** Builds a quiet effect instance: no particles, no icon clutter, just the state. */
    public static MobEffectInstance quiet(RegistryObject<MobEffect> effect, int duration, int amplifier) {
        return new MobEffectInstance(effect.getHolder().orElseThrow(), duration, amplifier, true, false, true);
    }

    /** Builds a visible effect instance for things the player should notice. */
    public static MobEffectInstance loud(RegistryObject<MobEffect> effect, int duration, int amplifier) {
        return new MobEffectInstance(effect.getHolder().orElseThrow(), duration, amplifier, false, true, true);
    }

    public static Holder<MobEffect> holder(RegistryObject<MobEffect> effect) {
        return effect.getHolder().orElseThrow();
    }

    private static final class SimpleEffect extends MobEffect {
        private SimpleEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}

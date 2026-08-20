package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import net.minecraft.world.effect.MobEffect;
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

    private static final class SimpleEffect extends MobEffect {
        private SimpleEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}
